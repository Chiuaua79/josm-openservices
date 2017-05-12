package org.openstreetmap.josm.plugins.ods.domains.addresses.processing;

import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.ods.AbstractLayerManager;
import org.openstreetmap.josm.plugins.ods.LayerManager;
import org.openstreetmap.josm.plugins.ods.OdsModule;
import org.openstreetmap.josm.plugins.ods.domains.addresses.AddressNode;
import org.openstreetmap.josm.plugins.ods.domains.buildings.Building;
import org.openstreetmap.josm.plugins.ods.primitives.ManagedNode;
import org.openstreetmap.josm.plugins.ods.primitives.ManagedPrimitive;
import org.openstreetmap.josm.plugins.ods.processing.OsmEntityRelationManager;

/**
 * <p>Try to find matching address nodes for every Building.
 * The geometry of the AddressNode will be used to do the matching</p>
 * <p>If a match is found, the building parameter of the addressNode will be set to the related address
 * and the addressNode will be added to the related addresses list of the building.</p>
 * <p>If no matching building was found, The unmatched addressNode will
 * be forwarded to the unmatchedAddressNodeConsumer if available;
 *
 * @author gertjan
 *
 */
public class Osm_Building_AddressNode_RelationManager implements OsmEntityRelationManager {
    private final OdsModule module;
    private DataSet dataSet;

    public Osm_Building_AddressNode_RelationManager(OdsModule module) {
        super();
        this.module = module;
    }

    /**
     * Create connections
     */
    public void createConnections() {
        LayerManager layerManager = module.getOsmLayerManager();
        layerManager.getRepository().getAll(Building.class)
        .forEach(this::createConnections);
    }

    /**
     * Find all matching addresses for a building.
     *
     * @param building
     */
    public void createConnections(Building building) {
        ManagedPrimitive mPrimitive = building.getPrimitive();
        BBox bbox = building.getPrimitive().getBBox();
        for (Node node : getDataSet().searchNodes(bbox)) {
            ManagedNode mNode = (ManagedNode) getLayerManager().getManagedPrimitive(node);
            if (mNode != null && mPrimitive.contains(mNode)) {
                if (mNode.getEntity() instanceof AddressNode) {
                    AddressNode addressNode = (AddressNode) mNode.getEntity();
                    addressNode.addBuilding(building);
                    building.getAddressNodes().add(addressNode);
                }
            }
        }
    }

    private DataSet getDataSet() {
        if (dataSet == null) {
            dataSet = getLayerManager().getOsmDataLayer().data;
        }
        return dataSet;
    }

    private AbstractLayerManager getLayerManager() {
        return module.getOsmLayerManager();
    }
}