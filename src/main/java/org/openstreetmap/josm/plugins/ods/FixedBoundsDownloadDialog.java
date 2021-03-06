package org.openstreetmap.josm.plugins.ods;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExpertToggleAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.tools.GBC;

/**
 * Dialog box to download a polygon area.
 * 
 * @author Gertjan Idema <mail@gertjanidema.nl>
 *
 */
public class FixedBoundsDownloadDialog extends AbstractDownloadDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static FixedBoundsDownloadDialog instance = null;
    /**
     * Replies the unique instance of this download dialog
     * 
     * @return the unique instance of this download dialog
     */
    static public FixedBoundsDownloadDialog getInstance() {
        if (instance == null) {
                instance = new FixedBoundsDownloadDialog(Main.parent);
        }
        return instance;
    }

    public FixedBoundsDownloadDialog(Component parent) {
        super(parent);
    }

    protected JPanel buildMainPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new GridBagLayout());

        String moduleName = ODS.getModule().getName();
        cbDownloadOSM = new JCheckBox(tr("Download OSM data"));
        cbDownloadOSM.setToolTipText(tr("<html>Select to download OSM data.<br>"
                        + "Unselect to skip downloading of OSM data.</html>"));
        cbDownloadODS = new JCheckBox(tr("Download {0} data", moduleName));
        cbDownloadODS.setToolTipText(tr("<html>Select to download {0}.<br>"
                        + "Unselect to skip downloading of {0} data.</html>", moduleName));

        pnl.add(cbDownloadOSM,
                GBC.std().anchor(GBC.SOUTHWEST).insets(5, 5, 5, 5));
        pnl.add(cbDownloadODS,
                GBC.eol().anchor(GBC.SOUTHWEST).insets(5, 5, 5, 5));

//        pnl.add(sizeCheck, GBC.eol().anchor(GBC.SOUTHEAST).insets(5, 5, 5, 2));

        if (!ExpertToggleAction.isExpert()) {
            JLabel infoLabel = new JLabel(
                    tr("Use left click&drag to select area, arrows or right mouse button to scroll map, wheel or +/- to zoom."));
            pnl.add(infoLabel, GBC.eol().anchor(GBC.SOUTH).insets(0, 0, 0, 0));
        }
        pnl.revalidate();
        pnl.repaint();
        return pnl;
    }

    protected Dimension getDimension() {
        return new Dimension(300, 200);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // No action required
    }

    @Override
    public Bounds getSelectedDownloadArea() {
        return null;
    }
}
