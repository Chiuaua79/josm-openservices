package org.openstreetmap.josm.plugins.ods.builtenvironment;

import org.openstreetmap.josm.plugins.ods.OdsWorkingSet;
import org.openstreetmap.josm.plugins.ods.entities.EntitySet;

public class BuiltEnvironmentWorkingSetAnalyzer {
    private OdsWorkingSet workingSet;
    
    public BuiltEnvironmentWorkingSetAnalyzer() {
        // TODO Auto-generated constructor stub
    }

    public void setWorkingSetLayer(OdsWorkingSet workingSet) {
        this.workingSet = workingSet;
    }
    
    public void analyze() {
        EntitySet osmEntities = workingSet.getInternalDataLayer().getEntitySet();
        EntitySet importedEntities = workingSet.getExternalDataLayer().getEntitySet();
        
    }
}
