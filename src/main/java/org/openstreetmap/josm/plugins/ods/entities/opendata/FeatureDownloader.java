package org.openstreetmap.josm.plugins.ods.entities.opendata;

import org.openstreetmap.josm.plugins.ods.Normalisation;
import org.openstreetmap.josm.plugins.ods.io.DownloadResponse;
import org.openstreetmap.josm.plugins.ods.io.Downloader;
import org.openstreetmap.josm.plugins.ods.storage.Repository;

/**
 * @author Gertjan Idema <mail@gertjanidema.nl>
 *
 */
public interface FeatureDownloader extends Downloader{
    public void setResponse(DownloadResponse response);
    public void setNormalisation(Normalisation normalisation);
    public Repository getRepository();
}
