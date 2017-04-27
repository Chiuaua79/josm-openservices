package org.openstreetmap.josm.plugins.ods.domains.buildings;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.plugins.ods.domains.addresses.Address;
import org.openstreetmap.josm.plugins.ods.domains.addresses.AddressNode;
import org.openstreetmap.josm.plugins.ods.domains.addresses.Addressable;
import org.openstreetmap.josm.plugins.ods.domains.places.City;
import org.openstreetmap.josm.plugins.ods.entities.AbstractEntity;

public class BuildingImpl extends AbstractEntity implements Building {
    private Address address;
    private List<HousingUnit> housingUnits = new LinkedList<>();
    private Set<AddressNode> addressNodes = new HashSet<>();
    private BuildingType buildingType = BuildingType.UNCLASSIFIED;
    private City city;
    
    @Override
    public Class<Building> getBaseType() {
        return Building.class;
    }

    @Override
    public BuildingType getBuildingType() {
        return buildingType;
    }

    @Override
    public void setBuildingType(BuildingType buildingType) {
        this.buildingType = buildingType;
//        ManagedPrimitive mPrimitive = this.getPrimitive();
//        if (mPrimitive != null) {
//            mPrimitive.putAll(buildingType.getTags());
//        }
    }

    @Override
    public Building getBuilding() {
        return this;
    }

    @Override
    public Set<Object> getBuildingIds() {
        return Collections.singleton(getReferenceId());
    }

    @Override
    public City getCity() {
        return city;
    }

    @Override
    public void setAddress(Address address) {
        this.address = address;
    }
    
    @Override
    public Address getAddress() {
        return address;
    }

    @Override
    public void addHousingUnit(HousingUnit housingUnit) {
        housingUnits.add(housingUnit);
        addressNodes.addAll(housingUnit.getAddressNodes());
    }

    @Override
    public List<HousingUnit> getHousingUnits() {
        return housingUnits;
    }

    @Override
    public Set<AddressNode> getAddressNodes() {
        return addressNodes;
    }
    
    @Override
    public Set<? extends Addressable> getAddressables() {
        if (getAddress() == null) {
            return getAddressNodes();
        }
        Set<Addressable> result = new HashSet<>(getAddressNodes());
        result.add(this);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Building ");
        sb.append(getReferenceId() == null ? "without id" : getReferenceId());
        if (address != null) {
            sb.append("\n").append(address.getFullHouseNumber());
        }
        for (AddressNode a : getAddressNodes()) {
            sb.append("\n").append(a.toString());
        }
        return sb.toString();
    }
}