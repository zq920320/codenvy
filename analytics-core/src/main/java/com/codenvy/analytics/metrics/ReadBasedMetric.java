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

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.storage.DataLoader;
import com.codenvy.analytics.storage.MongoDataStorage;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * It is supposed to load calculated value {@link com.codenvy.analytics.datamodel.ValueData} from the storage.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class ReadBasedMetric extends AbstractMetric {

    private static final long DAY_IN_MILLISECONDS = 86400000L;

    protected final DataLoader dataLoader;

    public ReadBasedMetric(String metricName) {
        super(metricName);
        this.dataLoader = MongoDataStorage.getDataLoader();
    }

    public ReadBasedMetric(MetricType metricType) {
        this(metricType.toString());
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        return dataLoader.loadValue(this, context);
    }

    // --------------------------------------------- storage related methods -------------

    /** Returns the name of collection in term of mongoDB to read data from. */
    public String getStorageTable() {
        return getName().toLowerCase();
    }

    /**
     * Returns 'matcher' in term of MongoDB. Basically, it can be treated as 'WHERE' clause in SQL queries.
     * See mongoDB related documentation for more details.
     *
     * @param clauses
     *         the execution context
     * @return {@link DBObject}
     */
    public DBObject getFilter(Map<String, String> clauses) throws IOException, ParseException {
        BasicDBObject match = new BasicDBObject();

        DBObject range = new BasicDBObject();
        range.put("$gte", Utils.getFromDate(clauses).getTimeInMillis());
        range.put("$lt", Utils.getToDate(clauses).getTimeInMillis() + DAY_IN_MILLISECONDS);
        match.put("_id", range);

        for (MetricFilter filter : Utils.getFilters(clauses)) {
            String[] values;
            if (filter == MetricFilter.COMPANY) {
                values = getUsersInCompany(filter.get(clauses));
            } else {
                values = filter.get(clauses).split(",");
            }
            match.put(filter.name().toLowerCase(), new BasicDBObject("$in", values));
        }

        return new BasicDBObject("$match", match);
    }

    private String[] getUsersInCompany(String company) throws IOException {
        Map<String, String> context = Utils.newContext();
        MetricFilter.COMPANY.put(context, company);

        List<ValueData> users =
                ((ListValueData)MetricFactory.getMetric(MetricType.USERS_PROFILES).getValue(context)).getAll();
        String[] result = new String[users.size()];

        for (int i = 0; i < users.size(); i++) {
            MapValueData user = (MapValueData)users.get(i);
            Map<String, ValueData> profile = user.getAll();

            result[i] = profile.get(UsersProfiles.USER_PROFILE_ATTR).getAsString();
        }

        return result;
    }

    /**
     * Returns the sequences of operations upon data have been retrieved out of storage.
     * See mongoDB documentation for more information.
     *
     * @param clauses
     *         the execution context
     * @return {@link DBObject}
     */
    public DBObject[] getDBOperations(Map<String, String> clauses) {
        return new DBObject[0];
    }
}

