package org.openstreetmap.josm.plugins.ods.entities;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

import org.openstreetmap.josm.plugins.ods.io.DownloadResponse;
import org.openstreetmap.josm.plugins.ods.issues.Issue;
import org.openstreetmap.josm.plugins.ods.matching.Match;
import org.openstreetmap.josm.plugins.ods.primitives.ManagedPrimitive;

import com.vividsolutions.jts.geom.Geometry;

/**
 * ODS entities represent entities like buildings, address nodes,
 * or streets. They are the interface between imported features and 
 * Josm primitives.
 * Using these entities gives the possibility to build object relations
 * from geometric relations.
 *   
 * @author gertjan
 *
 */
public interface Entity {
    public void setDownloadResponse(DownloadResponse response);
    public DownloadResponse getDownloadResponse();
    public void setSource(String source);
    public String getSource();
    public void setSourceDate(LocalDate date);
    public LocalDate getSourceDate();
    public void setIncomplete(boolean incomplete);
    public boolean isIncomplete();
    public void setStatus(EntityStatus status);
    public EntityStatus getStatus();
    public void setPrimaryId(Object id);
    public Object getPrimaryId();
    public void setReferenceId(Object id);
    public Object getReferenceId();
    public Long getPrimitiveId();
    public Geometry getGeometry();

    public void setGeometry(Geometry geometry);
    public Class<? extends Entity> getBaseType();
    
    public Match<? extends Entity> getMatch();
    
    public <E extends Entity> void setMatch(Match<E> match);
    /**
    * Get the OSM primitive from which this entity was constructed,
    * or that was/were constructed from this entity.
    *
    */
    public ManagedPrimitive getPrimitive();

    public void setPrimitive(ManagedPrimitive primitive);

    /**
     * The tags that are not associated with any of the entity's properties.
     */
    public void setOtherTags(Map<String, String> tags);
    public Map<String, String> getOtherTags();
    
    public Collection<? extends Issue> getIssues();
    public void addIssue(Issue issue);
    public boolean hasIssues();
    public Issue getIssue(Class<? extends Issue> type);
}
