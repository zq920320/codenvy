/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.services.metrics;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.persistent.CollectionsManagement;
import com.codenvy.analytics.services.Feature;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Alexander Reshetnyak
 */
@Singleton
public class DataComputation extends Feature {

    private static final Logger LOG = LoggerFactory.getLogger(DataComputation.class);

    public static final  String METRICS   = "analytics.data-computation.metrics";
    private static final int    PAGE_SIZE = 10000;

    private final CollectionsManagement collectionsManagement;
    private final String[]              metrics;

    @Inject
    public DataComputation(Configurator configurator, CollectionsManagement collectionsManagement)
            throws IOException {
        this.collectionsManagement = collectionsManagement;
        this.metrics = configurator.getArray(METRICS);
    }

    public DataComputation(String[] metrics, CollectionsManagement collectionsManagement)
            throws IOException {
        this.collectionsManagement = collectionsManagement;
        this.metrics = metrics;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    protected void putParametersInContext(Context.Builder builder) {
    }

    @Override
    protected void doExecute(Context context) throws IOException {
        for (String metricName : metrics) {
            ReadBasedMetric metric = (ReadBasedMetric)MetricFactory.getMetric(metricName);

            if (!MetricFactory.exists(metric.getName() +ReadBasedMetric.PRECOMPUTED)) {
                LOG.error("The metric " + metric.getName() + ReadBasedMetric.PRECOMPUTED  + " dos not exists");

            } else {
                Metric precomputedMetric = MetricFactory.getMetric(metric.getName() +ReadBasedMetric.PRECOMPUTED);
                String collectionName = metric.getName() + ReadBasedMetric.PRECOMPUTED;

                LOG.info("DataComputation is started for " + metric.getName());
                long start = System.currentTimeMillis();

                try {
                    collectionsManagement.drop(collectionName);

                    Context.Builder builder = new Context.Builder();

                    if (precomputedMetric instanceof PrecomputedDataSupportable) {
                        builder.putAll(((PrecomputedDataSupportable)precomputedMetric).prepare());
                    }

                    builder.put(Parameters.FROM_DATE, Parameters.FROM_DATE.getDefaultValue());
                    builder.put(Parameters.TO_DATE, context.getAsString(Parameters.TO_DATE));
                    builder.put(Parameters.PER_PAGE, PAGE_SIZE);

                    ListValueData valueData;
                    int pageNumber = 0;

                    do {
                        builder.put(Parameters.PAGE, ++pageNumber);

                        valueData = ValueDataUtil.getAsList(metric, builder.build());

                        write(collectionsManagement.get(collectionName), valueData.getAll());
                    } while (valueData.getAll().size() == PAGE_SIZE);

                    collectionsManagement.ensureIndexes(collectionName);
                } finally {
                    LOG.info("DataComputation is finished in " + (System.currentTimeMillis() - start) / 1000 +
                             " sec. for " + metric.getName());
                }
            }
        }
    }

    private void write(DBCollection dbCollection, List<ValueData> items) throws IOException {
        for (ValueData row : items) {
            DBObject dbObject = new BasicDBObject();
            dbObject.put(AbstractMetric.ID, UUID.randomUUID().toString());

            for (Map.Entry<String, ValueData> entry : ((MapValueData)row).getAll().entrySet()) {
                ValueData value = entry.getValue();

                if (value instanceof LongValueData) {
                    dbObject.put(entry.getKey(), ((LongValueData)value).getAsLong());
                } else if (value instanceof DoubleValueData) {
                    dbObject.put(entry.getKey(), ((DoubleValueData)value).getAsDouble());
                } else {
                    dbObject.put(entry.getKey(), value.getAsString());
                }
            }

            dbCollection.insert(dbObject);
        }
    }
}
