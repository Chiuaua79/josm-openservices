package org.openstreetmap.josm.plugins.ods;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.ods.entities.GeoRepository;
import org.openstreetmap.josm.plugins.ods.osm.ManagedNodeSet;
import org.openstreetmap.josm.plugins.ods.primitives.ManagedPrimitive;

public class TestLayerManager implements LayerManager, LayerChangeListener {
    private final OsmDataLayer osmDataLayer;
    private final Map<OsmPrimitive, ManagedPrimitive<?>> primitiveMap = new HashMap<>();
    private final GeoRepository repository = new GeoRepository();
    
    public TestLayerManager(OsmDataLayer osmDataLayer) {
        super();
        this.osmDataLayer = osmDataLayer;
    }

    
    @Override
    public GeoRepository getRepository() {
        return repository;
    }


    @Override
    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void layerAdded(Layer newLayer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void layerRemoved(Layer oldLayer) {
        // TODO Auto-generated method stub

    }

    @Override
    public OsmDataLayer getOsmDataLayer() {
        return osmDataLayer;
    }

    @Override
    public boolean isOsm() {
        return true;
    }

    @Override
    public void deActivate() {
        // TODO Auto-generated method stub
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
    }

    @Override
    public void register(OsmPrimitive primitive,
            ManagedPrimitive<?> managedPrimitive) {
        primitiveMap.put(primitive, managedPrimitive);
    }

    @Override
    public ManagedPrimitive<?> getManagedPrimitive(OsmPrimitive primitive) {
        return primitiveMap.get(primitive);
    }


    @Override
    public ManagedNodeSet getManagedNodes() {
        // TODO Auto-generated method stub
        return null;
    }
    
    
}
