package org.openstreetmap.josm.plugins.ods.matching.update;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.command.AddPrimitivesCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveData;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.ods.LayerManager;
import org.openstreetmap.josm.plugins.ods.Matcher;
import org.openstreetmap.josm.plugins.ods.ODS;
import org.openstreetmap.josm.plugins.ods.OdsModule;
import org.openstreetmap.josm.plugins.ods.entities.Entity;
import org.openstreetmap.josm.plugins.ods.entities.actual.AddressNode;
import org.openstreetmap.josm.plugins.ods.entities.actual.Building;
import org.openstreetmap.josm.plugins.ods.entities.osm.OsmEntitiesBuilder;
import org.openstreetmap.josm.plugins.ods.osm.BuildingAligner;
import org.openstreetmap.josm.plugins.ods.osm.OsmNeighbourFinder;
import org.openstreetmap.josm.plugins.ods.primitives.ManagedPrimitive;

/**
 * The importer imports objects from the OpenData layer to the Osm layer.
 * 
 * TODO Use AddPrimitivesCommand
 * 
 * @author Gertjan Idema <mail@gertjanidema.nl>
 *
 */
public class OdsImporter {
    private final OdsModule module;
    // TODO Make the importfilter(s) configurable
    private final ImportFilter importFilter = new DefaultImportFilter();
    // TODO Move buildingAligner out of this class in favour of a
    // Observer pattern
    private final BuildingAligner buildingAligner;
    
    public OdsImporter(OdsModule module) {
        super();
        this.module = module;
        this.buildingAligner=new BuildingAligner(module, 
                module.getOsmLayerManager());
    }

    public void doImport(Collection<OsmPrimitive> primitives) {
        LayerManager layerManager = module.getOpenDataLayerManager();
        Set<Entity> entitiesToImport = new HashSet<>();
        for (OsmPrimitive primitive : primitives) {
            ManagedPrimitive<?> managedPrimitive = layerManager.getManagedPrimitive(primitive);
            if (managedPrimitive != null) {
                Entity entity = managedPrimitive.getEntity();
                if (entity != null && entity.getMatch() == null 
                        && importFilter.test(entity)) {
                    entitiesToImport.add(entity);
                }
            }
//            for (OsmPrimitive referrer : primitive.getReferrers()) {
//                if (referrer.getType().equals(OsmPrimitiveType.RELATION)) {
//                    Entity referrerEntity = layerManager.getEntity(referrer);
//                    if (referrerEntity != null && referrerEntity.getMatch() == null 
//                          && importFilter.test(referrerEntity)) {
//                        entitiesToImport.add(referrerEntity);
//                    }
//                }
//            }
        }
        importEntities(entitiesToImport);
    }
    
    private void importEntities(Set<Entity> entitiesToImport) {
        Set<ManagedPrimitive<?>> primitivesToImport = new HashSet<>();
        PrimitiveDataBuilder builder = new PrimitiveDataBuilder();
        for (Entity entity : entitiesToImport) {
            ManagedPrimitive<?> primitive = entity.getPrimitive();
//            if (primitive.g.getType().equals(OsmPrimitiveType.RELATION)) {
//                Relation relation = (Relation) primitive;
//                for (OsmPrimitive member : relation.getMemberPrimitives()) {
//                    primitivesToImport.add(member);
//                    builder.addPrimitive(member);
//                }
//            }
            if (primitive != null) {
                primitivesToImport.add(primitive);
                builder.addPrimitive(primitive);
            }
        }
        AddPrimitivesCommand cmd = new AddPrimitivesCommand(builder.primitiveData, null,
            module.getOsmLayerManager().getOsmDataLayer());
        cmd.executeCommand();
        Collection<? extends OsmPrimitive> importedPrimitives = cmd.getParticipatingPrimitives();
        removeOdsTags(importedPrimitives);
        buildImportedEntities(importedPrimitives);
        OsmNeighbourFinder neighbourFinder = new OsmNeighbourFinder(module);
//        for (OsmPrimitive osm : importedPrimitives) {
//            neighbourFinder.findNeighbours(osm);
//        }
        updateMatching();
    }
    
    private void updateMatching() {
        Matcher<?> matcher = module.getMatcherManager().getMatcher(Building.class);
        matcher.run();
        matcher = module.getMatcherManager().getMatcher(AddressNode.class);
        matcher.run();
    }

    /**
     * Remove the ODS tags from the selected Osm primitives
     * 
     * @param osmData
     */
    private void removeOdsTags(Collection<? extends OsmPrimitive> primitives) {
        for (OsmPrimitive primitive : primitives) {
            for (String key : primitive.keySet()) {
                if (key.startsWith(ODS.KEY.BASE)) {
                    primitive.put(key, null);
                }
            }
        }
    }

    /**
     * Build entities for the newly imported primitives.
     * We could have created these entities from the OpenData entities instead. But by building them
     * from the Osm primitives, we make sure that all entities in the Osm layer are built the same way,
     * making them consistent with each other.
     * 
     * @param importedPrimitives
     */
    private void buildImportedEntities(
            Collection<? extends OsmPrimitive> importedPrimitives) {
        OsmEntitiesBuilder entitiesBuilder = module.getOsmLayerManager().getEntitiesBuilder();
        entitiesBuilder.build(importedPrimitives);
    }
    
    private class PrimitiveDataBuilder {
        private List<PrimitiveData> primitiveData = new LinkedList<>();
        
        public void addPrimitive(ManagedPrimitive<? extends OsmPrimitive> managedPrimitive) {
            OsmPrimitive primitive = managedPrimitive.getPrimitive();
            primitiveData.add(primitive.save());
            if (primitive.getType() == OsmPrimitiveType.WAY) {
                for (Node node :((Way)primitive).getNodes()) {
                    addPrimitive(node);
                }
            }
            else if (primitive.getType() == OsmPrimitiveType.RELATION) {
                for (OsmPrimitive osm : ((Relation)primitive).getMemberPrimitives()) {
                    addPrimitive(osm);
                }
            }
        }
        
        public void addPrimitive(OsmPrimitive primitive) {
            primitiveData.add(primitive.save());
            if (primitive.getType() == OsmPrimitiveType.WAY) {
                for (Node node :((Way)primitive).getNodes()) {
                    addPrimitive(node);
                }
            }
            else if (primitive.getType() == OsmPrimitiveType.RELATION) {
                for (OsmPrimitive osm : ((Relation)primitive).getMemberPrimitives()) {
                    addPrimitive(osm);
                }
            }
        }
    }
}
