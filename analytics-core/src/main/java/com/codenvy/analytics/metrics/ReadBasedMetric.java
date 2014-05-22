/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.metrics;

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.persistent.DataLoader;
import com.codenvy.analytics.persistent.MongoDataStorage;
import com.mongodb.DBObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.codenvy.analytics.Utils.*;

/**
 * It is supposed to load calculated value {@link com.codenvy.analytics.datamodel.ValueData} from the storage.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class   ReadBasedMetric extends AbstractMetric {

    private static final Logger LOG         = LoggerFactory.getLogger(ReadBasedMetric.class);
    public static final  String PRECOMPUTED = "_precomputed";

    public final DataLoader dataLoader;

    public ReadBasedMetric(String metricName) {
        super(metricName);

        MongoDataStorage mongoDataStorage = Injector.getInstance(MongoDataStorage.class);
        this.dataLoader = mongoDataStorage.createdDataLoader();
    }

    public ReadBasedMetric(MetricType metricType) {
        this(metricType.toString());
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        long start = System.currentTimeMillis();

        try {
            context = omitFilters(context);
            validateRestrictions(context);

            if (canReadPrecomputedData(context)) {
                Metric metric = MetricFactory.getMetric(getName() + PRECOMPUTED);

                Context.Builder builder = new Context.Builder(context);
                builder.remove(Parameters.FROM_DATE);
                builder.remove(Parameters.TO_DATE);
                return metric.getValue(builder.build());
            } else {
                ValueData valueData = dataLoader.loadValue(this, context);
                return postComputation(valueData, context);
            }
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Metric computation " + getName() + " is finished with context " + context + " in " +
                          ((System.currentTimeMillis() - start) / 1000) + " sec.");
            }
        }
    }

    /**
     * Returns an expanded list of documents used to calculate numeric value returned by getValue() method.
     *
     *
     * @param context
     *         the execution context, for the most cases it isn't needed to modify it. It is used as a parameter to get
     *         value of other metrics
     * @throws IOException
     *         if any errors are occurred
     */
    public ValueData getExpandedValue(Context context) throws IOException {
        if (!(this instanceof Expandable)) {
            throw new IllegalStateException(getName() + " is expected to be expandable");
        }

        long start = System.currentTimeMillis();

        try {
            context = omitFilters(context);
            validateRestrictions(context);

            return dataLoader.loadExpandedValue(this, context);
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Expended metric computation " + getName() + " is finished with context " + context + " in " +
                          ((System.currentTimeMillis() - start) / 1000) + " sec.");
            }
        }
    }

    private Context omitFilters(Context context) {
        if (getClass().isAnnotationPresent(OmitFilters.class)) {
            Context.Builder builder = new Context.Builder(context);
            for (MetricFilter filter : getClass().getAnnotation(OmitFilters.class).value()) {
                builder.remove(filter);
            }

            return builder.build();
        }

        return context;
    }

    /**
     * Validates restriction before data loading.
     *
     * @param context
     *         the execution context
     */
    private void validateRestrictions(Context context) {
        if (getClass().isAnnotationPresent(RequiredFilter.class)) {
            MetricFilter requiredFilter = getClass().getAnnotation(RequiredFilter.class).value();
            if (!context.exists(requiredFilter)) {
                throw new MetricRestrictionException("Parameter " + requiredFilter + " required to be passed to get the value of the metric");
            }
        }

        String allowedUsers = context.getAsString(Parameters.ORIGINAL_USER);
        String allowedWorkspaces = context.getAsString(Parameters.ORIGINAL_WS);

        String ws = context.getAsString(MetricFilter.WS);
        Object user = context.get(MetricFilter.USER);

        if (isAnonymousUser(user) && isTemporaryWorkspace(ws)) {
            throw new MetricRestrictionException("Security violation. Probably user hasn't access to the data");
        } else if (isAnonymousUser(user)) {
            if (!isAllowedEntities(ws, allowedWorkspaces)) {
                throw new MetricRestrictionException("Security violation. Probably user hasn't access to the data");
            }
        } else if (isTemporaryWorkspace(ws)) {
            if (!isAllowedEntities(user, allowedUsers)) {
                throw new MetricRestrictionException("Security violation. Probably user hasn't access to the data");
            }
        } else {
            if (!isAllowedEntities(user, allowedUsers) || !isAllowedEntities(ws, allowedWorkspaces)) {
                throw new MetricRestrictionException("Security violation. Probably user hasn't access to the data");
            }
        }
    }

    /**
     * Provides ability to modify the result by adding new fields or changing existed ones.
     */
    public ValueData postComputation(ValueData valueData, Context clauses) throws IOException {
        return valueData;
    }

    /**
     * Allows to modify context before evaluation if necessary.
     */
    public Context applySpecificFilter(Context context) throws IOException {
        return context;
    }

    private boolean canReadPrecomputedData(Context context) {
        String precomputedMetricName = getName() + PRECOMPUTED;
        return !context.exists(Parameters.DATA_COMPUTATION_PROCESS)
               && MetricFactory.exists(precomputedMetricName)
               && ((PrecomputedMetric)MetricFactory.getMetric(precomputedMetricName)).canReadPrecomputedData(context)
               && context.getFilters().isEmpty()
               && (!context.exists(Parameters.FROM_DATE) || context.isDefaultValue(Parameters.FROM_DATE))
               && (!context.exists(Parameters.TO_DATE) || context.isDefaultValue(Parameters.TO_DATE));
    }

    public String getStorageCollectionName() {
        return getStorageCollectionName(getName());
    }

    protected String getStorageCollectionName(MetricType metricType) {
        return getStorageCollectionName(metricType.toString());
    }

    protected String getStorageCollectionName(String metricName) {
        return metricName.toLowerCase();
    }

    /**
     * @return the fields are interested in by given metric. In other words, they are valuable for given metric. It
     * might returns empty array to read all available fields
     */
    public abstract String[] getTrackedFields();

    /** @return DB operations specific for given metric */
    public abstract DBObject[] getSpecificDBOperations(Context clauses);
}
