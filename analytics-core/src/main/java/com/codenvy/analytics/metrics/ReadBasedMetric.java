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
        this.dataLoader = MongoDataStorage.createdDataLoader();
    }

    public ReadBasedMetric(MetricType metricType) {
        this(metricType.toString());
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        return dataLoader.loadValue(this, context);
    }

    // --------------------------------------------- storage related methods -------------

    /**
     * For most metrics data can be stored in two collections. The fist collection contains summarized data per day to
     * retrieve for performance purpose. The second one contains detailed data. In case of using filters, only this
     * table will be used.
     *
     * @return true if data of the metric are stored in multiple tables
     */
    public abstract boolean isSupportMultipleTables();

    /**
     * @return the fields are interested in by given metric. In other words, they are valuable for given metric. It
     *         might returns empty array to read all available fields
     */
    public abstract String[] getTrackedFields();

    /**
     * For most metrics data can be stored in two collections. The name of first one is basically derived from the name
     * of metric itself. The name of second one can be obtained by adding specific suffix to the end.
     * {@link com.codenvy.analytics.storage.MongoDataLoader#EXT_COLLECTION_NAME_SUFFIX}.
     *
     * @return the name of collection. if metric supports multiple tables, the name of the second collection will be
     *         evaluated automatically.
     */
    public String getStorageTableBaseName() {
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
        setDateFilter(clauses, match);

        for (MetricFilter filter : Utils.getFilters(clauses)) {
            String[] values;
            if (filter == MetricFilter.USER_COMPANY) {
                values = getUsersInCompany(filter.get(clauses));
                match.put(MetricFilter.USER.name().toLowerCase(), new BasicDBObject("$in", values));
            } else {
                values = filter.get(clauses).split(",");
                match.put(filter.name().toLowerCase(), new BasicDBObject("$in", values));
            }
        }

        return new BasicDBObject("$match", match);
    }

    /**
     * The _id field as a rule contains the date of the event. The only exceptions related to user's profile
     * metrics.
     *
     * @see AbstractUsersProfile
     */
    private void setDateFilter(Map<String, String> clauses, BasicDBObject match) throws ParseException {
        DBObject idFilter = new BasicDBObject();
        match.put("_id", idFilter);

        idFilter.put("$gte", Parameters.FROM_DATE.exists(clauses)
                             ? Utils.getFromDate(clauses).getTimeInMillis()
                             : 0);
        idFilter.put("$lt", Parameters.TO_DATE.exists(clauses)
                            ? Utils.getToDate(clauses).getTimeInMillis() + DAY_IN_MILLISECONDS
                            : Long.MAX_VALUE);
    }

    private String[] getUsersInCompany(String company) throws IOException {
        Map<String, String> context = Utils.newContext();
        MetricFilter.USER_COMPANY.put(context, company);

        Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
        List<ValueData> users = ((ListValueData)metric.getValue(context)).getAll();

        String[] result = new String[users.size()];

        for (int i = 0; i < users.size(); i++) {
            MapValueData user = (MapValueData)users.get(i);
            Map<String, ValueData> profile = user.getAll();

            result[i] = profile.get(AbstractUsersProfile.USER_EMAIL).getAsString();
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
    public final DBObject[] getDBOperations(Map<String, String> clauses) {
        return unionDBOperations(getBasicDBOperations(clauses),
                                 getSpecificDBOperations(clauses));
    }

    /** Provides basic DB operations: sorting and pagination. */
    private DBObject[] getBasicDBOperations(Map<String, String> clauses) {
        boolean sortExists = Parameters.SORT.exists(clauses);
        boolean pageExists = Parameters.PAGE.exists(clauses);

        DBObject[] dbOp = new DBObject[(sortExists ? 1 : 0) + (pageExists ? 2 : 0)];

        if (sortExists) {
            String sortCondition = Parameters.SORT.get(clauses);

            String field = sortCondition.substring(1);
            boolean asc = sortCondition.substring(0, 1).equals("+");

            dbOp[0] = new BasicDBObject("$sort", new BasicDBObject(field, asc ? 1 : -1));
        }

        if (pageExists) {
            long page = Long.parseLong(Parameters.PAGE.get(clauses));
            long perPage = Long.parseLong(Parameters.PER_PAGE.get(clauses));

            dbOp[sortExists ? 1 : 0] = new BasicDBObject("$skip", (page - 1) * perPage);
            dbOp[sortExists ? 2 : 1] = new BasicDBObject("$limit", perPage);
        }

        return dbOp;
    }

    protected DBObject[] unionDBOperations(DBObject[] dbOp1, DBObject[] dbOp2) {
        DBObject[] result = new DBObject[dbOp1.length + dbOp2.length];

        System.arraycopy(dbOp1, 0, result, 0, dbOp1.length);
        System.arraycopy(dbOp2, 0, result, dbOp1.length, dbOp2.length);

        return result;
    }

    /** @return DB operations specific for given metric */
    protected abstract DBObject[] getSpecificDBOperations(Map<String, String> clauses);

}

