package org.openstreetmap.josm.plugins.ods.osm;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.ods.osm.alignment.NodeIterator;

/**
 * The SmallSegmentRemer removes segments shorter than a given tolerance.
 * TODO Use angles to determine which node we should keep
 * 
 * @author gertjan
 *
 */
public class SmallSegmentRemover {
 
    public static void removeSmallSegments(Way way, Double tolerance, boolean undoable) {
        NodeIterator it = new NodeIterator(way, 0, false);
        Double distanceSq = tolerance * tolerance;
        while (it.hasNextNode()) {
            EastNorth enStart = it.peek().getEastNorth();
            EastNorth enEnd = it.peekNext().getEastNorth();
            if (enStart.distanceSq(enEnd) <= distanceSq) {
                it.collapseSegment();
            }
            it.next();
        }
        it.close(undoable);
    }
}
