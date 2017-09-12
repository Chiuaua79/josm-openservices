package org.openstreetmap.josm.plugins.ods;

import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.plugins.ods.entities.EntityPrimitiveBuilder;
import org.openstreetmap.josm.plugins.ods.entities.EntityType;
import org.openstreetmap.josm.plugins.ods.entities.osm.OsmEntityBuilder;
import org.openstreetmap.josm.plugins.ods.exceptions.OdsException;
import org.openstreetmap.josm.plugins.ods.io.Host;
import org.openstreetmap.josm.plugins.ods.processing.OsmEntityRelationManager;

/**
 * Provide the configuration for an Ods Module.
 *
 * @author Gertjan Idema <mail@gertjanidema.nl>
 *
 */
public interface OdsModuleConfiguration {

    Collection<? extends EntityType> getEntityTypes();

    /**
     * @return A collection of (uninitialized) hosts required by this module. The list will
     * be used to initialize the hosts when the module is activated
     */
    Collection<Host> getHosts();

    /**
     * @return A list of (uninitialized) featureSources required by this module. The list will
     * be used to initialize the featureSources when the module is activated
     */
    List<? extends OdsFeatureSource> getFeatureSources();

    /**
     * @return A collection of (uninitialized) dataSources required by this module. The list will
     * be used to initialize the dataSources when the module is activated
     */
    Collection<OdsDataSource> getDataSources();

    OdsDataSource getDataSource(String name) throws OdsException;

    List<Class<? extends OsmEntityBuilder>> getOsmEntityBuilders();

    List<Class<? extends OsmEntityRelationManager>> getOsmRelationManagers();

    Collection<Class<? extends EntityPrimitiveBuilder<?>>> getPrimitiveBuilders();
}