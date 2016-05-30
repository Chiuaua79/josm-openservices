package org.openstreetmap.josm.plugins.ods.matching;

import java.util.function.Consumer;

import org.openstreetmap.josm.plugins.ods.OdsModule;
import org.openstreetmap.josm.plugins.ods.entities.actual.AddressNode;

/**
 * <p>Try to find a matching building for every AddressNode passed to the AddressNode
 * consumer. The geometry of the AddressNode will be used to do the matching</p>
 * <p>If a match is found, the building parameter of the addressNode will be set to the related address
 * and the addressNode will be added to the related addresses list of the building.</p>
 * <p>If no matching building was found, The unmatched addressNode will
 * be forwarded to the unmatchedAddressNodeConsumer if available;
 * 
 * @author gertjan
 *
 */
public class OsmAddressNodeToBuildingMatcher {
    private OdsModule module;
    private Consumer<AddressNode> unmatchedAddressNodeHandler;
    
    public OsmAddressNodeToBuildingMatcher(OdsModule module) {
        super();
        this.module = module;
    }

    public void setUnmatchedAddressNodeHandler(
            Consumer<AddressNode> unmatchedAddressNodeHandler) {
        this.unmatchedAddressNodeHandler = unmatchedAddressNodeHandler;
    }

    /**
     * Find a matching building for an address.
     * TODO use the geometry index to find the building
     * 
     * @param addressNode
     */
    public void match(AddressNode addressNode) {
        // TODO reimplement this functionality
//        OsmBuildingStore buildingStore = (OsmBuildingStore)module
//                .getOsmLayerManager().getEntityStore(Building.class);
//        GeoIndex<Building> geoIndex = buildingStore.getGeoIndex();
//        if (addressNode.getBuilding() == null) {
//            List<Building> buildings = geoIndex.intersection(addressNode.getGeometry());
//            if (buildings.size() != 1) {
//                reportUnmatched(addressNode);
//                return;
//            }
//            Building building = buildings.get(0);
//            addressNode.setBuilding(building);
//            building.getAddressNodes().add(addressNode);
//        }
    }
    
//    /**
//     * Find a matching building for an address.
//     * Iterate over buildings to find the building
//     * 
//     * @param addressNode
//     */
//    public void match(AddressNode addressNode) {
//        OsmBuildingStore buildings = (OsmBuildingStore)module
//                .getOsmLayerManager().getEntityStore(Building.class);
//        if (addressNode.getBuilding() == null) {
//            Iterator<Building> iterator = buildings.iterator();
//            boolean found = false;
//            while (iterator.hasNext() && !found) {
//                Building building = iterator.next();
//                if (building.getGeometry().covers(addressNode.getGeometry())) {
//                    addressNode.setBuilding(building);
//                    building.getAddressNodes().add(addressNode);
//                    found = true;
//                }
//            }
//            if (!found) {
//                reportUnmatched(addressNode);
//            }
//        }
//    }
    
    private void reportUnmatched(AddressNode addressNode) {
        if (unmatchedAddressNodeHandler != null) {
            unmatchedAddressNodeHandler.accept(addressNode);
        }
    }
}
