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
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.users.AbstractUsersProfile;
import com.codenvy.analytics.persistent.DataLoader;
import com.codenvy.analytics.persistent.MongoDataStorage;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * It is supposed to load calculated value {@link com.codenvy.analytics.datamodel.ValueData} from the storage.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class ReadBasedMetric extends AbstractMetric {

    /** The field name in collection containing the date of events. */
    public static final String DATE                = "date";
    public static final String EXCLUDE_SIGN        = "~";
    public static final String SEPARATOR           = ",";
    public static final long   DAY_IN_MILLISECONDS = 86400000L;


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
    public ValueData getValue(Map<String, String> context) throws IOException {
        return dataLoader.loadValue(this, context);
    }

    // --------------------------------------------- storage related methods -------------

    /**
     * @return the fields are interested in by given metric. In other words, they are valuable for given metric. It
     * might returns empty array to read all available fields
     */
    public abstract String[] getTrackedFields();

    public String getStorageCollectionName() {
        return getName().toLowerCase();
    }

    public String getStorageCollectionName(MetricType metricType) {
        return MetricFactory.getMetric(metricType).getName().toLowerCase();
    }

    public String getStorageCollectionName(String metricName) {
        return metricName.toLowerCase();
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
                values = getUsersInCompanies(filter.get(clauses));
                match.put(MetricFilter.USER.name().toLowerCase(), new BasicDBObject("$in", values));

            } else if (filter == MetricFilter.DOMAIN) {
                String[] domains = filter.get(clauses).split(SEPARATOR);
                match.put(MetricFilter.USER.name().toLowerCase(), getUsersInDomains(domains));

            } else {
                values = filter.get(clauses).split(SEPARATOR);
                match.put(filter.name().toLowerCase(), processExclusiveValues(values, filter.isNumericType()));
            }
        }

        return new BasicDBObject("$match", match);
    }

    private BasicDBObject processExclusiveValues(String[] values, boolean isNumericType)
            throws IOException, ParseException {

        StringBuilder exclusiveValues = new StringBuilder();
        StringBuilder inclusiveValues = new StringBuilder();

        for (String value : values) {
            if (value.startsWith(EXCLUDE_SIGN)) {
                if (exclusiveValues.length() != 0) {
                    exclusiveValues.append(SEPARATOR);
                }
                exclusiveValues.append(value.substring(1));

            } else {
                if (inclusiveValues.length() != 0) {
                    inclusiveValues.append(SEPARATOR);
                }
                inclusiveValues.append(value);
            }
        }

        if (inclusiveValues.length() != 0) {
            values = inclusiveValues.toString().split(SEPARATOR);
            return new BasicDBObject("$in", isNumericType ? convertToNumericFormat(values) : values);
        } else {
            values = exclusiveValues.toString().split(SEPARATOR);
            return new BasicDBObject("$nin", isNumericType ? convertToNumericFormat(values) : values);
        }
    }

    private long[] convertToNumericFormat(String[] values) {
        long[] result = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = Long.parseLong(values[i]);
        }

        return result;
    }

    /**
     * The date field contains the date of the event. The only exceptions related to user's profile
     * metrics.
     *
     * @see com.codenvy.analytics.metrics.users.AbstractUsersProfile
     */
    private void setDateFilter(Map<String, String> clauses, BasicDBObject match) throws ParseException {
        DBObject dateFilter = new BasicDBObject();
        match.put(DATE, dateFilter);

        dateFilter.put("$gte", Parameters.FROM_DATE.exists(clauses)
                               ? Utils.getFromDate(clauses).getTimeInMillis()
                               : 0);
        dateFilter.put("$lt", Parameters.TO_DATE.exists(clauses)
                              ? Utils.getToDate(clauses).getTimeInMillis() + DAY_IN_MILLISECONDS
                              : Long.MAX_VALUE);
    }

    private Pattern getUsersInDomains(String[] domains) {
        StringBuilder builder = new StringBuilder();
        for (String domain : domains) {
            if (builder.length() != 0) {
                builder.append("|");
            }

            builder.append(".*");
            if (!domain.startsWith("@")) {
                builder.append("@");
            }
            builder.append(domain);
        }

        return Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
    }

    private String[] getUsersInCompanies(String company) throws IOException {
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
        return unionDBOperations(getSpecificDBOperations(clauses),
                                 getPaginationDBOperations(clauses));
    }

    /** Provides basic DB operations: sorting and pagination. */
    private DBObject[] getPaginationDBOperations(Map<String, String> clauses) {
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

    public DBObject[] unionDBOperations(DBObject[] dbOp1, DBObject[] dbOp2) {
        DBObject[] result = new DBObject[dbOp1.length + dbOp2.length];

        System.arraycopy(dbOp1, 0, result, 0, dbOp1.length);
        System.arraycopy(dbOp2, 0, result, dbOp1.length, dbOp2.length);

        return result;
    }

    /** @return DB operations specific for given metric */
    public abstract DBObject[] getSpecificDBOperations(Map<String, String> clauses);

}

