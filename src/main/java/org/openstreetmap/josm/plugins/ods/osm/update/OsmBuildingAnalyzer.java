package org.openstreetmap.josm.plugins.ods.osm.update;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.QuadBuckets;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.ods.entities.actual.Building;
import org.openstreetmap.josm.plugins.ods.matching.Match;
import org.openstreetmap.josm.plugins.ods.osm.update.PoolNode.SpecialReferrers;

/**
 * This class is part of the building update process.
 * It's concern is to analyse the buildings on the OSM layer that are about
 * to be updated with a new geometry. 
 * 
 * @author Gertjan Idema <mail@gertjanidema.nl>
 *
 */
public class OsmBuildingAnalyzer {
    private final List<Match<Building>> buildingMatches;
    private final NodePool nodePool = new NodePool();
    private final Set<OsmPrimitive> includedPrimitives = new HashSet<>();
    private final Set<Match<Building>> excludedMatches =new HashSet<>();
    private final Set<Node> reuseableNodes = new HashSet<>();
    private final Set<Node> taggedNodes = new HashSet<>();
    private final QuadBuckets<Node> reuseableNodeIndex = new QuadBuckets<>();

//    private final Set<Node> connectedBuildingNodes = new HashSet<>();
//    private final Set<Node> connectedOtherNodes = new HashSet<>();
    
    public OsmBuildingAnalyzer(List<Match<Building>> buildingMatches) {
        super();
        this.buildingMatches = buildingMatches;
    }

    public NodePool getNodePool() {
        return nodePool;
    }

    public void analyze() {
        // Collect the building primitives in a set, so we can look them up
        for (Match<Building> match : buildingMatches) {
            assert match.isSimple();
            includedPrimitives.add(match.getOsmEntity().getPrimitive());
        }
        for (Match<Building> match : buildingMatches) {
            // Focus on Ways. We'll tackle relations later
            OsmPrimitive osm = match.getOsmEntity().getPrimitive();
            if (osm.getDisplayType().equals(OsmPrimitiveType.CLOSEDWAY)) {
                analyseNodes((Way)osm, match);
            }
            else {
                excludedMatches.add(match);
            }
        }
        for (Match<Building> match : excludedMatches) {
            OsmPrimitive osm = match.getOsmEntity().getPrimitive();
            if (osm.getDisplayType().equals(OsmPrimitiveType.CLOSEDWAY)) {
                for (Node node : ((Way)osm).getNodes()) {
                    reuseableNodes.remove(node);
                }
            }
        }
        // Create a geo index for the reusable nodes
        reuseableNodeIndex.addAll(reuseableNodes);
    }

    // Analyse the nodes in the way
    private void analyseNodes(Way way, Match<Building> match) {
        for (Node node : way.getNodes()) {
            if (!nodePool.contains(node)) {
                SpecialReferrers specialReferrers = getNodeSpecialReferrers(node);
                nodePool.addNode(node, specialReferrers);
            }
        }
    }
    
    /**
     * Check if the node has special referrers.
     * A referrers is considered to be special if it is not a building
     * that is about to be updated.
     *
     * @param node
     */
    private SpecialReferrers getNodeSpecialReferrers(Node node) {
        boolean isBuilding = false;
        boolean other = false;
        for (OsmPrimitive referrer : node.getReferrers()) {
            if (!includedPrimitives.contains(referrer)) {
                if (BuildingEntityType.isBuilding(referrer)) {
                    isBuilding = true;
                }
                else {
                    other = true;
                }
            }
        }
        if (isBuilding) {
            if (other) return SpecialReferrers.BOTH;
            return SpecialReferrers.BUILDING;
        }
        if (other) {
            return SpecialReferrers.OTHER;
        }
        return SpecialReferrers.NONE;
    }
    
    public Collection<?> getExcludedMatches() {
        return excludedMatches;
    }
}
