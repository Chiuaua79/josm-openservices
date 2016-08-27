package org.openstreetmap.josm.plugins.ods.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDateTime;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.ods.OdsModule;
import org.openstreetmap.josm.plugins.ods.io.DownloadRequest;
import org.openstreetmap.josm.plugins.ods.io.MainDownloader;
import org.openstreetmap.josm.plugins.ods.jts.Boundary;
import org.openstreetmap.josm.tools.ImageProvider;
import org.xml.sax.SAXException;

public class OdsDownloadAction extends OdsAction {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    MainDownloader downloader;
    LocalDateTime startDate;
    private boolean cancelled = false;
    Boundary boundary;
    boolean downloadOsm;
    boolean downloadOpenData;
    private SlippyMapDownloadDialog slippyDialog;
    private FixedBoundsDownloadDialog fixedDialog;
    
    public OdsDownloadAction(OdsModule module) {
        super(module, "Download", ImageProvider.get("download"));
        slippyDialog = new SlippyMapDownloadDialog(module);
        fixedDialog = new FixedBoundsDownloadDialog(module);
        this.downloader = module.getDownloader();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        run();
    }
    
    public void run() {
        cancelled = false;
        boundary = getBoundary();
        startDate = LocalDateTime.now();
        if (!cancelled) {
            DownloadTask task = new DownloadTask();
            Main.worker.submit(task);
        }
    }

    private Boundary getBoundary() {
        Boundary bounds = getPolygonBoundary();
        boolean selectArea = (bounds == null);
        AbstractDownloadDialog dialog;
        if (selectArea) {
            dialog = slippyDialog;
        }
        else {
            dialog = fixedDialog;
        }
        dialog.restoreSettings();
        dialog.setVisible(true);
        if (dialog.isCanceled()) {
            cancelled = true;
            return null;
        }
        dialog.rememberSettings();
        downloadOsm = dialog.cbDownloadOSM.isSelected();
        downloadOpenData = dialog.cbDownloadODS.isSelected();
        if (selectArea) {
            bounds = new Boundary(dialog.getSelectedDownloadArea());
        }
        return bounds;
    }
    
    private Boundary getPolygonBoundary() {
        if (Main.map == null) {
            return null;
        }
        Layer activeLayer = Main.getLayerManager().getActiveLayer();
        // Make sure the active layer is an Osm datalayer
        if (!(activeLayer instanceof OsmDataLayer)) {
            return null;
        }
        // Make sure the active layer is not a managed ODS layer
        if (getModule().getLayerManager(activeLayer) != null) {
            return null;
        }
        // Make sure only one object was selected
        OsmDataLayer layer = (OsmDataLayer) activeLayer;
        if (layer.data.getAllSelected().size() != 1) {
            return null;
        }
        OsmPrimitive primitive = layer.data.getAllSelected().iterator().next();
        if (primitive.getDisplayType() == OsmPrimitiveType.CLOSEDWAY) {
            return new Boundary((Way)primitive);
        }
        return null;
    }
    
    private class DownloadTask extends PleaseWaitRunnable {
        
        public DownloadTask() {
            super(tr("Downloading data"));
        }

        @Override
        protected void cancel() {
            downloader.cancel();
        }

        @Override
        protected void realRun() throws SAXException, IOException,
                OsmTransferException {
            DownloadRequest request = new DownloadRequest(startDate, boundary,
                downloadOsm, downloadOpenData);
            downloader.run(getProgressMonitor(), request);
        }

        @Override
        protected void finish() {
            if (downloadOpenData) {
                Main.getLayerManager().setActiveLayer(getModule().getOpenDataLayerManager().getOsmDataLayer());
            }
            else {
                Main.getLayerManager().setActiveLayer(getModule().getOsmLayerManager().getOsmDataLayer());
            }
        }
    }
}
