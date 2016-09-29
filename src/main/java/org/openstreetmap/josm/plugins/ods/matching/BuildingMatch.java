package org.openstreetmap.josm.plugins.ods.matching;

import static org.openstreetmap.josm.plugins.ods.entities.EntityStatus.CONSTRUCTION;
import static org.openstreetmap.josm.plugins.ods.entities.EntityStatus.IN_USE;
import static org.openstreetmap.josm.plugins.ods.entities.EntityStatus.PLANNED;

import java.util.Objects;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.ods.entities.EntityStatus;
import org.openstreetmap.josm.plugins.ods.entities.actual.Building;

public class BuildingMatch extends MatchImpl<Building> {
    /**
     * A double value indicating the match between the areas of the 2 buildings.
     * 
     */
    private MatchStatus areaMatch;
    private MatchStatus centroidMatch;
    private MatchStatus startDateMatch;
    private MatchStatus statusMatch;
    
    public BuildingMatch(Building osmBuilding, Building openDataBuilding) {
        super(osmBuilding, openDataBuilding);
    }
    
    @Override
    public void analyze() {
        areaMatch = compareAreas();
        centroidMatch = compareCentroids();
        startDateMatch = compareStartDates();
        statusMatch = compareStatuses();
    }
    
    private MatchStatus compareStartDates() {
        if (Objects.equals(getOsmEntity().getStartDate(), getOpenDataEntity().getStartDate())) {
            return MatchStatus.MATCH;
        }
        return MatchStatus.NO_MATCH;
    }

    private MatchStatus compareStatuses() {
        EntityStatus osmStatus = getOsmEntity().getStatus();
        EntityStatus odStatus = getOpenDataEntity().getStatus();
        if (osmStatus.equals(odStatus)) {
            return MatchStatus.MATCH;
        }
        if (odStatus.equals(PLANNED) && osmStatus.equals(CONSTRUCTION)) {
            return MatchStatus.COMPARABLE;
        }
        if (odStatus.equals(CONSTRUCTION) && osmStatus.equals(IN_USE)) {
            return MatchStatus.COMPARABLE;
        }
        return MatchStatus.NO_MATCH;
    }
    
    private MatchStatus compareAreas() {
//        double osmArea = getOsmEntity().getGeometry().getArea();
        double osmArea = getOsmEntity().getPrimitive().getArea();
//        double odArea = getOpenDataEntity().getGeometry().getArea();
        double odArea = getOpenDataEntity().getPrimitive().getArea();
        if (osmArea == 0.0 || odArea == 0.0) {
            areaMatch = MatchStatus.NO_MATCH;
        }
        double match = (osmArea - odArea) / osmArea;
        if (match == 0.0) {
            return MatchStatus.MATCH;
        }
        if (Math.abs(match) < 0.01) {
            return MatchStatus.COMPARABLE;
        }
        return MatchStatus.NO_MATCH;
    }
    
    private MatchStatus compareCentroids() {
        LatLon osmCentroid = getOsmEntity().getPrimitive().getCenter();
        LatLon odCentroid = getOpenDataEntity().getPrimitive().getCenter();
        double centroidDistance = osmCentroid.distance(odCentroid);
        if (centroidDistance == 0) {
            return MatchStatus.MATCH;
        }
        if (centroidDistance < 1e-6) {
            return MatchStatus.COMPARABLE;
        }
        return MatchStatus.NO_MATCH;
    }

    @Override
    public MatchStatus getGeometryMatch() {
        return MatchStatus.combine(areaMatch, centroidMatch);
    }

    @Override
    public MatchStatus getStatusMatch() {
        return statusMatch;
    }
    
    @Override
    public MatchStatus getAttributeMatch() {
        return startDateMatch;
    }
}