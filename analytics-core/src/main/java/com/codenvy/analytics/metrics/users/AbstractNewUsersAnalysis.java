/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static com.codenvy.analytics.Utils.getFilterAsString;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsSet;

/** @author Anatoliy Bazko */
@RolesAllowed({"system/admin", "system/manager"})
public abstract class AbstractNewUsersAnalysis extends CalculatedMetric implements Expandable {

    public AbstractNewUsersAnalysis(MetricType metricType, MetricType basedMetric) {
        super(metricType, new MetricType[]{basedMetric});
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Context context) throws IOException {
        Context filter = constructFilter(context);
        return getBasedMetric().getValue(filter);
    }


    /** {@inheritDoc} */
    @Override
    public ValueData getExpandedValue(Context context) throws IOException {
        Context filter = constructFilter(context);
        return ((Expandable)getBasedMetric()).getExpandedValue(filter);
    }

    /** {@inheritDoc} */
    @Override
    public String getExpandedField() {
        return ((Expandable)getBasedMetric()).getExpandedField();
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    protected Context constructFilter(Context context) throws IOException {
        Context.Builder builder = new Context.Builder(context);

        String userFilter = getNewUsers(context);
        setNewUsersAsFilter(builder, userFilter);

        setSpecificFilter(builder);

        return builder.build();
    }

    protected String getNewUsers(Context context) throws IOException {
        SetValueData svd = getAsSet(getCreatedUsersMetric(), context);
        Object[] newUsers = svd.getAll().toArray();
        return getFilterAsString(new HashSet<>(Arrays.asList(newUsers)));
    }

    protected void setNewUsersAsFilter(Context.Builder builder, String userFilter) {
        builder.put(MetricFilter.USER, userFilter);
    }

    protected Metric getBasedMetric() {
        return basedMetric[0];
    }

    protected Metric getCreatedUsersMetric() {
        return MetricFactory.getMetric(MetricType.CREATED_USERS_SET);
    }

    abstract protected void setSpecificFilter(Context.Builder builder);
}


