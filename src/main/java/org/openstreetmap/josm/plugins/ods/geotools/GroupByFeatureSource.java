package org.openstreetmap.josm.plugins.ods.geotools;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureListener;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

public class GroupByFeatureSource extends OdsFeatureSource<GroupByFeatureCollection> {
    private final SimpleFeatureSource wrappedSource;
    private final SimpleFeatureType featureType;
    private final LinkedList<FeatureListener> listeners = new LinkedList<>();
    private final GroupByQuery query;
    
    public GroupByFeatureSource(Name newName, SimpleFeatureSource wrappedSource, GroupByQuery query) {
        super(wrappedSource);
        this.query = query;
        this.wrappedSource = wrappedSource;
        // TODO use Hints to retrieve FeatureTypeFactory
        this.featureType = wrappedSource.getSchema();
    }

    @Override
    public Name getName() {
        return wrappedSource.getName();
    }

    @Override
    public ResourceInfo getInfo() {
        return wrappedSource.getInfo();
    }

    @Override
    public DataAccess<SimpleFeatureType, SimpleFeature> getDataStore() {
        return wrappedSource.getDataStore();
    }

    @Override
    public QueryCapabilities getQueryCapabilities() {
        return wrappedSource.getQueryCapabilities();
    }

    @Override
    public void addFeatureListener(FeatureListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeFeatureListener(FeatureListener listener) {
        listeners.remove(listener);
    }

    @Override
    public SimpleFeatureType getSchema() {
        return featureType;
    }

    @Override
    public ReferencedEnvelope getBounds() throws IOException {
        return wrappedSource.getBounds();
    }

    @Override
    public ReferencedEnvelope getBounds(Query q) throws IOException {
        return wrappedSource.getBounds();
    }

    @Override
    public int getCount(Query q) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Key> getSupportedHints() {
        return wrappedSource.getSupportedHints();
    }

    @Override
    protected OdsFeatureCollection getFeatureCollection(SimpleFeatureCollection wrappedFeatureCollection) {
        return new GroupByFeatureCollection(wrappedFeatureCollection, query);
    }

//    @Override
//    public SimpleFeatureCollection getFeatures() throws IOException {
//        SimpleFeatureCollection wrappedFeatures = wrappedSource.getFeatures();
//        return getFeatures(wrappedFeatures);
//    }
//
//    @Override
//    public SimpleFeatureCollection getFeatures(Filter filter) throws IOException {
//        SimpleFeatureCollection wrappedFeatures = wrappedSource.getFeatures(filter);
//        return getFeatures(wrappedFeatures);
//    }
//
//    @Override
//    public SimpleFeatureCollection getFeatures(Query query) throws IOException {
//        SimpleFeatureCollection wrappedFeatures = wrappedSource.getFeatures(query);
//        return getFeatures(wrappedFeatures);
//    }

}
