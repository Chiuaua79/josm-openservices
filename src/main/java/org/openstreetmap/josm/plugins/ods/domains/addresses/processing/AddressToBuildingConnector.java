package org.openstreetmap.josm.plugins.ods.domains.addresses.processing;

import java.util.Iterator;
import java.util.function.Consumer;

import org.openstreetmap.josm.plugins.ods.LayerManager;
import org.openstreetmap.josm.plugins.ods.OdsModule;
import org.openstreetmap.josm.plugins.ods.domains.addresses.AddressNode;
import org.openstreetmap.josm.plugins.ods.domains.buildings.Building;
import org.openstreetmap.josm.plugins.ods.io.OdsProcessor;
import org.openstreetmap.josm.plugins.ods.storage.GeoRepository;
import org.openstreetmap.josm.plugins.ods.storage.Repository;

import com.vividsolutions.jts.geom.Geometry;


/**
 * <p>Try to find a matching building for every AddressNode passed to the AddressNode
 * consumer. The geometry of the address node will be used to do the matching.</p>
 * <p>If a match is found, the building parameter of the addressNode will be set to the related address
 * and the addressNode will be added to the related addresses list of the building.</p>
 * <p>If no matching building was found,the faulty addressNode will
 * be forwarded to the unmatchedAddressNode consumer if available;
 *
 * @author gertjan
 *
 */
public class AddressToBuildingConnector implements OdsProcessor {
    private final OdsModule module = OdsProcessor.getModule();
    private Consumer<AddressNode> unmatchedAddressNodeHandler;

    public AddressToBuildingConnector() {
        super();
    }

    public void setUnmatchedHousingUnitHandler(
            Consumer<AddressNode> unmatchedAddressNodeHandler) {
        this.unmatchedAddressNodeHandler = unmatchedAddressNodeHandler;
    }

    @Override
    public void run() {
        LayerManager layerManager = module.getOpenDataLayerManager();
        layerManager.getRepository().getAll(AddressNode.class)
        .forEach(this::match);
    }

    /**
     * Find a matching building for an address node.
     *
     * @param addressNode
     */
    public void match(AddressNode addressNode) {
        Repository repository = module.getOpenDataLayerManager().getRepository();
        if (addressNode.getBuilding() == null) {
            Geometry geometry = addressNode.getGeometry();
            if (geometry != null && repository instanceof GeoRepository) {
                Iterator<Building> matchedbuildings = ((GeoRepository)repository).queryIntersection(Building.class, "geometry", geometry).iterator();
                if (matchedbuildings.hasNext()) {
                    Building building = matchedbuildings.next();
                    addressNode.addBuilding(building);
                    building.getAddressNodes().add(addressNode);
                }
                else {
                    reportUnmatched(addressNode);
                }
            }
            else {
                reportUnmatched(addressNode);
            }
        }
    }

    private void reportUnmatched(AddressNode addressNode) {
        if (unmatchedAddressNodeHandler != null) {
            unmatchedAddressNodeHandler.accept(addressNode);
        }
    }
}
