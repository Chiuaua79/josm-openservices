package org.openstreetmap.josm.plugins.ods.domains.addresses.processing;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.ods.LayerManager;
import org.openstreetmap.josm.plugins.ods.OdsModule;
import org.openstreetmap.josm.plugins.ods.domains.addresses.Address;
import org.openstreetmap.josm.plugins.ods.domains.addresses.AddressNode;
import org.openstreetmap.josm.plugins.ods.domains.addresses.AddressNodeGroup;
import org.openstreetmap.josm.plugins.ods.domains.buildings.Building;
import org.openstreetmap.josm.plugins.ods.domains.buildings.HousingUnit;
import org.openstreetmap.josm.plugins.ods.io.OdsProcessor;

/**
 * This processor finds overlapping nodes in the data and distributes them, so
 * they are no longer overlapping.
 * The HousingUnitToBuildingConnector and AddressToBuildingConnector must run
 * before this class, so we can distribute over the line pointing to the
 * center of the building.
 *
 * @author Gertjan Idema
 *
 */
public class AddressNodeDistributor implements OdsProcessor {
    private final OdsModule module = OdsProcessor.getModule();
    private Comparator<? super AddressNode> comparator = new DefaultNodeComparator();

    public AddressNodeDistributor() {
        super();
    }

    public void setComparator(Comparator<? super AddressNode> comparator) {
        this.comparator = comparator;
    }

    @Override
    public void run() {
        LayerManager layerManager = module.getOpenDataLayerManager();
        layerManager.getRepository().getAll(Building.class)
        .forEach(this::distributeNodes);
    }

    public void distributeNodes(Building building) {
        for (AddressNodeGroup group : buildGroups(building).values()) {
            if (group.getAddressNodes().size() > 1) {
                distribute(group, false);
            }
        }
    }

    /**
     * Analyze all new address nodes and group them by Geometry (Point)
     *
     * @param newEntities
     */
    private static Map<LatLon, AddressNodeGroup> buildGroups(Building building) {
        Map<LatLon, AddressNodeGroup> groups = new HashMap<>();
        for (HousingUnit housingUnit : building.getHousingUnits()) {
            for (AddressNode addressNode : housingUnit.getAddressNodes()) {
                LatLon coord = addressNode.getPrimitive().getCenter();
                AddressNodeGroup group = groups.get(coord);
                if (group == null) {
                    group = new AddressNodeGroup(addressNode);
                    groups.put(coord, group);
                } else {
                    group.addAddressNode(addressNode);
                }
            }
        }
        return groups;
    }

    private void distribute(AddressNodeGroup group, boolean withUndo) {
        List<AddressNode> nodes = group.getAddressNodes();
        Collections.sort(nodes, comparator);
        //        if (group.getBuilding().getGeometry().isEmpty()) {
        //            // Happens rarely,
        //            // for now return to prevent null pointer Exception
        //            return;
        //        }
        LatLon center = group.getBuilding().getPrimitive().getCenter();
        double angle = group.getCoords().bearing(center);
        double dx = Math.sin(angle) * 2e-7;
        double dy = Math.cos(angle) * 2e-7;
        double x = group.getCoords().getX();
        double y = group.getCoords().getY();
        for (AddressNode node : nodes) {
            LatLon coord = new LatLon(y, x);
            node.getPrimitive().getNode().setCoor(coord);
            x = x + dx;
            y = y + dy;
        }
    }

    private static class DefaultNodeComparator implements Comparator<AddressNode> {
        private final Comparator<Address> addressComparator = new DefaultAddressComparator();

        public DefaultNodeComparator() {
            // Default constructor
        }

        @Override
        public int compare(AddressNode o1, AddressNode o2) {
            return addressComparator.compare(o1.getAddress(), o2.getAddress());
        }
    }

    private static class DefaultAddressComparator implements Comparator<Address> {
        static Comparator<String> stringComparator = new NullSafeCaseInsensitiveComparator();

        public DefaultAddressComparator() {
            // TODO Auto-generated constructor stub
        }

        @Override
        public int compare(Address a1, Address a2) {
            int result = stringComparator.compare(a1.getCityName(), a2.getCityName());
            if (result != 0) return result;
            result = stringComparator.compare(a1.getPostcode(), a2.getPostcode());
            if (result != 0) return result;
            result = stringComparator.compare(a1.getStreetName(), a2.getStreetName());
            if (result != 0) return result;
            result = Integer.compare(a1.getHouseNumber(), a2.getHouseNumber());
            if (result != 0) return result;
            if (a1.getHouseLetter() == null || a2.getHouseLetter() == null) {
                if (a2.getHouseNumber() != null) return -1;
                if (a1.getHouseNumber() != null) return 1;
            }
            else {
                result = Character.compare(a1.getHouseLetter(), a2.getHouseLetter());
            }
            if (result != 0) return result;
            return stringComparator.compare(a1.getHouseNumberExtra(), a2.getHouseNumberExtra());
        }
    }

    private static class NullSafeCaseInsensitiveComparator implements Comparator<String> {

        public NullSafeCaseInsensitiveComparator() {
            //
        }

        @Override
        public int compare(String s1, String s2) {
            if (s1 == null && s2 == null) return 0;
            if (s1 == null) return -1;
            if (s2 == null) return 1;
            return Objects.compare(s2, s2, String.CASE_INSENSITIVE_ORDER);
        }

    }
}