package org.openstreetmap.josm.plugins.ods.geotools;

import java.util.LinkedList;
import java.util.List;

import org.geotools.data.Query;
import org.geotools.filter.FilterFactoryImpl;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.openstreetmap.josm.tools.I18n;

/**
 * Extension to Query that supports a group by functionality.
 * The current implementation is a bit clumsy. The actual grouping is done in
 * a feature iterator.
 * Using a Filter would allow for a much neater solution.
 * 
 * @author Gertjan Idema <mail@gertjanidema.nl>
 *
 */
public class GroupByQuery extends Query {
    // TODO Use Hints to discover the default filterFactory 
    private final GtFeatureSource featureSource;
    private final FilterFactory ff = new FilterFactoryImpl();
    private final List<String> groupBy;
    private SortBy[] sortByArr = null;

    public GroupByQuery(GtFeatureSource featureSource, List<String> propertyNames, List<String> groupBy) {
        super(featureSource.getFeatureName(), Filter.INCLUDE, propertyNames.toArray(new String[0]));
        this.featureSource = featureSource;
        this.groupBy = groupBy;
    }
    
    public void initialize() throws InvalidQueryException {
        checkAttributes();
    }

    public List<String> getGroupBy() {
        return groupBy;
    }

    @Override
    public SortBy[] getSortBy() {
        if (sortByArr == null) {
            List<SortBy> sortList = new LinkedList<>();
            for (String attName : groupBy) {
                SortBy sb = ff.sort(attName, SortOrder.ASCENDING);
                if (sb != null) {
                    sortList.add(sb);
                }
            }
            sortByArr = sortList.toArray(new SortBy[0]);
        }
        return sortByArr;
    }
    
    private void checkAttributes() throws InvalidQueryException {
        FeatureType featureType = featureSource.getFeatureType();
        List<String> unknownAttributes = new LinkedList<>();
        for (String attribute : groupBy) {
            if (featureType.getDescriptor(attribute) == null) {
                unknownAttributes.add(attribute);
            }
        }
        if (unknownAttributes.size() > 0) {
            StringBuilder sb = new StringBuilder(I18n.tr(
                    "One or more query attributes are unkown for feature ''{0}'': ", featureType.getName()));
            sb.append(String.join(", ", unknownAttributes));
            throw new InvalidQueryException(sb.toString());
        }
    }


}
