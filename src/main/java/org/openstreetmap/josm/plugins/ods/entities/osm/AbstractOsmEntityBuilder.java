package org.openstreetmap.josm.plugins.ods.entities.osm;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.function.Predicate;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.ods.OdsModule;
import org.openstreetmap.josm.plugins.ods.entities.Entity;
import org.openstreetmap.josm.plugins.ods.entities.Repository;
import org.openstreetmap.josm.plugins.ods.jts.GeoUtil;
import org.openstreetmap.josm.plugins.ods.matching.Match;
import org.openstreetmap.josm.plugins.ods.primitives.ManagedPrimitive;

public abstract class AbstractOsmEntityBuilder<T extends Entity> implements OsmEntityBuilder {
    private OsmLayerManager layerManager;
//    private Class<T> baseType;
    private Predicate<OsmPrimitive> recognizer;
    private Repository repository;
    private GeoUtil geoUtil;
    
    public AbstractOsmEntityBuilder(OdsModule module, Class<T> baseType, Predicate<OsmPrimitive> recognizer) {
        super();
        this.geoUtil = module.getGeoUtil();
        this.layerManager = module.getOsmLayerManager();
        this.repository = layerManager.getRepository();
        this.recognizer = recognizer;
//        this.baseType = baseType;
    }

    
    
//    @Override
//    public Class<T> getEntityClass() {
//        return baseType;
//    }

    // TODO do we still need this method?
//    @Override
//    public void initialize() {
//        // No action required
//    }

    @Override
    public OsmLayerManager getLayerManager() {
        return layerManager;
    }

    @Override
    public boolean recognizes(OsmPrimitive primitive) {
        return recognizer.test(primitive);
    }

    public GeoUtil getGeoUtil() {
        return geoUtil;
    }
    
    protected void register(ManagedPrimitive primitive, T entity) {
        entity.setPrimitive(primitive);
        repository.add(entity);
        primitive.setEntity(entity);
    }
    
    /*
     * Check if the primitive is incomplete.
     * Or any of it's members in case of a relation
     */
    public static boolean isIncomplete(OsmPrimitive primitive) {
        if (OsmPrimitiveType.RELATION != primitive.getType()) {
            return primitive.isIncomplete();
        }
        if (primitive.isIncomplete()) return true;
        for (OsmPrimitive member : ((Relation)primitive).getMemberPrimitives()) {
            if (isIncomplete(member)) return true;
        }
        return false;
    }

    @Override
    public void updateTags(OsmPrimitive primitive, Map<String, String> newTags) {
        ManagedPrimitive ods = getLayerManager().getManagedPrimitive(primitive);
        if (ods == null) return;
        @SuppressWarnings("unchecked")
        T entity = (T) ods.getEntity();
        if (entity == null) {
            return;
        }
        updateTags(entity, newTags);
        Match<?> match = entity.getMatch();
        if (match != null && match.isSimple()) {
            match.analyze();
            match.updateMatchTags();
        }
    }

    @Override
    public void updateGeometry(Way way) {
        ManagedPrimitive ods = getLayerManager().getManagedPrimitive(way);
        if (ods == null) return;
        ods.setPrimitive(way);
        @SuppressWarnings("unchecked")
        T entity = (T) ods.getEntity();
        if (entity == null) {
            return;
        }
//        updateGeometry(entity, way);
        Match<?> match = entity.getMatch();
        if (match != null && match.isSimple()) {
            match.analyze();
            match.updateMatchTags();
        }
    }

//    abstract void updateGeometry(T entity, ManagedPrimitive<?> ods);

    public void updateTags(T entity, Map<String, String> tags) {
        parseKeys(entity, tags);
    }

    protected void parseKeys(T entity, Map<String, String> tags) {
        entity.setReferenceId(parseReferenceId(tags));
        entity.setSource(tags.get("source"));
        String sourceDate = tags.get("source:date");
        if (sourceDate != null) {
            try {
                entity.setSourceDate(LocalDate.parse(sourceDate));
            } catch (DateTimeParseException e) {
                entity.setSourceDate(null);
            }
        }
    }

    protected abstract Object parseReferenceId(Map<String, String> tags);

    @SuppressWarnings("static-method")
    protected void normalizeTags(ManagedPrimitive primitive) {
        return;
    }

    protected abstract void updateGeometry(T addressNode,
            OsmPrimitive primitive);
}
