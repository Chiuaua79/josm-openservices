package org.openstreetmap.josm.plugins.ods.domains.places;

import org.openstreetmap.josm.plugins.ods.entities.AbstractEntity;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class CityImpl extends AbstractEntity implements City {
    private String name;
    private MultiPolygon multiPolygon;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

//    @Override
//    public void setGeometry(Geometry geometry) {
//        switch (geometry.getGeometryType()) {
//        case "MultiPolygon":
//            multiPolygon = (MultiPolygon) geometry;
//            break;
//        case "Polygon":
//            multiPolygon = geometry.getFactory().createMultiPolygon(
//                new Polygon[] {(Polygon) geometry});
//            break;
//        default:
//            // TODO intercept this exception or accept null?
//        }
//    }

//    @Override
//    public MultiPolygon getGeometry() {
//        return multiPolygon;
//    }

    @Override
    public boolean isIncomplete() {
        return false;
    }

    @Override
    public Class<City> getBaseType() {
        return City.class;
    }
}