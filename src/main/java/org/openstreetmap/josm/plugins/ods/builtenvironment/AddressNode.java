package org.openstreetmap.josm.plugins.ods.builtenvironment;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.plugins.ods.entities.Entity;

import com.vividsolutions.jts.geom.Point;

public interface AddressNode extends Entity {
	public final static String TYPE = "ods:address";

    public Address getAddress();

//	public Block getBlock();
	
	public Object getBuildingRef();

    public void setBuilding(Building building);

    public Building getBuilding();

    public void setGeometry(Point point);
    
    public Command updateGeometry(Point point);

    public Point getGeometry();
}
