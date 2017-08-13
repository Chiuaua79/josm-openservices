package org.openstreetmap.josm.plugins.ods.osm.alignment;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.ods.domains.buildings.BuildingEntityType;
import org.openstreetmap.josm.plugins.ods.osm.alignment.ModifiableWay.UndoMode;

/**
 * Align an Osm way to its surroundings.
 * The ways will be aligned according to a given tolerance.
 * The isRelevantWay predicate is used to determine if a certain way should be
 * aligned if it is near.
 * A point in the second ring that is within 'tolerance' distance from a point
 * from the first ring, will get the coordinates of that point.
 * A lineSegment from either of the rings, that is within 'tolerance'
 * distance from a point on the other ring, will be split by adding that
 * point in between the start and end point of that segment.
 *
 * @author gertjan
 *
 */
public class OsmWayAligner {
    private final Predicate<Way> isRelevantWay;
    private final Predicate<Node> isRelevantNode;
    private final Set<Way> ways;
    private final Set<Way> neighbourWays = new HashSet<>();
    private final DataSet dataset;
    private final  UndoMode undoMode = UndoMode.DETAILED;
    //    private NodeMerger nodeMerger;
    private final List<Issue> issues = new LinkedList<>();
    private final Set<Node> specialNodes = new HashSet<>();
    //    private Set<Node> connectedNodes = new HashSet<>();
    //    private Map<OsmPrimitive, ConnectedBuilding> connectedBuildings = new HashMap<>();
    private final Set<OsmPrimitive> otherConnections = new HashSet<>();
    private final NodeDWithin dWithin;
    private ModifiableWay modWay;

    public OsmWayAligner(Collection<Way> ways,
            NodeDWithin dWithin, Predicate<Way> isRelevantWay) {
        //        this.nodeMerger = nodeMerger;
        this.ways = new HashSet<>(ways);
        this.dataset = ways.iterator().next().getDataSet();
        this.dWithin = dWithin;
        this.isRelevantWay = isRelevantWay;
        this.isRelevantNode = new IsRelevantNode(isRelevantWay);
    }

    public void run() {
        dataset.beginUpdate();
        try {
            for (Way way : ways) {
                align(way);
            }
            findNeighbourWays();
            for (Way way : neighbourWays) {
                align(way);
            }
        } finally {
            dataset.endUpdate();
        }
    }

    private void findNeighbourWays() {
        Set<Way> nearbyWays = new HashSet<>();
        for (Way way : ways) {
            BBox bbox = dWithin.getBBox(way);
            nearbyWays.addAll(dataset.searchWays(bbox));
        }
        nearbyWays.removeAll(ways);
        nearbyWays.removeIf(isRelevantWay.negate());
        this.neighbourWays.addAll(nearbyWays);
    }

    private void align(Way way) {
        if (!way.isClosed() || way.isIncomplete()) {
            issues.add(Issue.UnclosedWay);
            return;
        }
        analyzeConnections(way);
        if (hasIssues()) return;

        modWay = new ModifiableWay(way, undoMode);
        while (modWay.hasNextNode()) {
            WaySegment segment = modWay.getCurrentSegment();
            List<Node> nearByNodes = dWithin.nearByNodes(segment);
            boolean wayLengthChanged = false;
            if (!nearByNodes.isEmpty()) {
                if (nearByNodes.size() > 1) {
                    sortNodes(nearByNodes, segment);
                }
                int cachedWayLength = way.getNodesCount();
                for (Node nearByNode : nearByNodes) {
                    if (!nearByNode.isDeleted() && isRelevantNode.test(nearByNode)) {
                        modWay.align(nearByNode, dWithin);
                    }
                }
                if (cachedWayLength != way.getNodesCount()) {
                    wayLengthChanged = true;
                }
            }
            if (!wayLengthChanged) {
                modWay.next();
            }
        }
    }

    /**
     * Sort a list of nodes in the direction of a segment.
     * The x coordinates are used for comparison, unless the segment is vertical, in which case the Y coordinate will be used.
     *
     * @param nodes The nodes to sort
     * @param segment The segment
     * @return
     */
    private void sortNodes(List<Node> nodes, WaySegment segment) {
        Comparator<Node> comparator = new DirectionalNodeComparator(segment);
        nodes.sort(comparator);
    }

    private class DirectionalNodeComparator implements Comparator<Node> {
        private final Comparator<Double> lonComparator;
        private final Comparator<Double> latComparator;

        public DirectionalNodeComparator(WaySegment segment) {
            int lonSignum = Integer.signum(Double.compare(segment.getNode2().getCoor().lon(), segment.getNode1().getCoor().lon()));
            lonComparator = new SignumDoubleComparator(lonSignum);
            int latSignum = Integer.signum(Double.compare(segment.getNode2().getCoor().lat(), segment.getNode1().getCoor().lat()));
            latComparator = new SignumDoubleComparator(latSignum);
        }

        @Override
        public int compare(Node n1, Node n2) {
            int result = lonComparator.compare(n1.getCoor().lon(), n2.getCoor().lon());
            if (result == 0) {
                result = latComparator.compare(n1.getCoor().lat(), n2.getCoor().lat());
            }
            return result;
        }

        private class SignumDoubleComparator implements Comparator<Double> {
            final int signum;

            public SignumDoubleComparator(int signum) {
                this.signum = signum;
            }

            @Override
            public int compare(Double d1, Double d2) {
                return signum * Double.compare(d1, d2);
            }
        }
    }

    private void analyzeConnections(Way way) {
        for (Node node: way.getNodes()) {
            // Check for tagged nodes
            if (node.hasKeys()) {
                specialNodes.add(node);
            }
            //            if (!node.getReferrers().isEmpty()) {
            //                connectedNodes.add(node);
            //            }
            for (OsmPrimitive referrer : node.getReferrers()) {
                if (referrer == way) continue;
                if (!BuildingEntityType.isBuildingWay(referrer)) {
                    //                    if (! connectedBuildings.containsKey(referrer)) {
                    //                        ConnectedBuilding cb = new ConnectedBuilding(node, referrer);
                    //                        connectedBuildings.put(referrer, cb);
                    //                    }
                    //                }
                    //                else {
                    otherConnections.add(referrer);
                }
            }
        }
        if (!specialNodes.isEmpty()) {
            issues.add(Issue.SpecialNodes);
        }
        if (!otherConnections.isEmpty()) {
            issues.add(Issue.OtherConnections);
        }
    }

    private boolean hasIssues() {
        return !issues.isEmpty();
    }

    private static class IsRelevantNode implements Predicate<Node> {
        private final Predicate<Way> isRelevantWay;

        public IsRelevantNode(Predicate<Way> isRelevantWay) {
            super();
            this.isRelevantWay = isRelevantWay;
        }

        @Override
        public boolean test(Node node) {
            for (OsmPrimitive osm : node.getReferrers()) {
                if (osm.getType() == OsmPrimitiveType.WAY) {
                    if (isRelevantWay.test((Way)osm)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    enum Issue {
        UnclosedWay,
        SpecialNodes,
        OtherConnections
    }
}
