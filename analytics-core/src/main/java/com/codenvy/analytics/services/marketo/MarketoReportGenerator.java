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
package com.codenvy.analytics.services.marketo;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.NumericValueData;
import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.codenvy.analytics.metrics.users.UsersStatisticsList;
import com.codenvy.analytics.services.acton.ActOn;
import com.codenvy.analytics.services.view.MetricRow;
import com.mongodb.DBObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.codenvy.analytics.Utils.toArray;
import static com.codenvy.analytics.metrics.AbstractMetric.BUILDS;
import static com.codenvy.analytics.metrics.AbstractMetric.DATE;
import static com.codenvy.analytics.metrics.AbstractMetric.DEPLOYS;
import static com.codenvy.analytics.metrics.AbstractMetric.GIGABYTE_RAM_HOURS;
import static com.codenvy.analytics.metrics.AbstractMetric.ID;
import static com.codenvy.analytics.metrics.AbstractMetric.LOGINS;
import static com.codenvy.analytics.metrics.AbstractMetric.PROJECTS;
import static com.codenvy.analytics.metrics.AbstractMetric.RUNS;
import static com.codenvy.analytics.metrics.AbstractMetric.TIME;
import static com.codenvy.analytics.metrics.AbstractMetric.USER;

/**
 * @author Alexander Reshetnyak
 */
@Singleton
public class MarketoReportGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(MarketoReportGenerator.class);

    public static final String PROFILE_COMPLETED   = "Profile Complete";
    public static final String POINTS              = "Product Score";
    public static final String LAST_PRODUCT_LOGIN  = "Last Product Login";
    public static final String SING_UP_DATE        = "Date Sign-Up";
    public static final String NEW_USER            = "New User";
    public static final String ACCOUNT_LOCKDOWN    = "Account Lockdown";
    public static final String CC_ADDED            = "CC Added";
    public static final String ON_PREM_SUB_ADDED   = "On-Prem Sub Added";
    public static final String ON_PREM_SUB_REMOVED = "On-Prem Sub Removed";

    private final Configurator configurator;

    /** Map users_statistics collection columns into the csv file headers. */
    @SuppressWarnings("serial")
    public static final LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>() {{
        put(ID, "Email Address");
        put(BUILDS, "Builds");
        put(DEPLOYS, "Deploys");
        put(PROFILE_COMPLETED, PROFILE_COMPLETED);
        put(PROJECTS, "Projects");
        put(RUNS, "Runs");
        put(TIME, "Time in Product");
        put(LOGINS, "Logins");
        put(LAST_PRODUCT_LOGIN, "Last Product Login");
        put(POINTS, POINTS);
        //put(NEW_USER, NEW_USER);
        put(GIGABYTE_RAM_HOURS, "Gigabyte Hours");
        put(SING_UP_DATE, SING_UP_DATE);
        put(ACCOUNT_LOCKDOWN, ACCOUNT_LOCKDOWN);
        put(CC_ADDED, CC_ADDED);
        put(ON_PREM_SUB_ADDED, ON_PREM_SUB_ADDED);
        put(ON_PREM_SUB_REMOVED, ON_PREM_SUB_REMOVED);
    }};

    @Inject
    public MarketoReportGenerator(Configurator configurator) {
        this.configurator = configurator;
    }

    public void prepareReport(File toFileReport,
                              Context context,
                              Context activeUsersContext,
                              boolean processActiveUsersOnly) throws IOException, ParseException {
        Set<ValueData> activeUsers = processActiveUsersOnly ? getActiveUsersByDatePeriod(activeUsersContext)
                                                            : new HashSet<ValueData>() {
                                                                @Override
                                                                public boolean contains(Object o) {
                                                                    return true;
                                                                }
                                                            };

        Set<ValueData> newUsers = getNewUsers(context);

        final int pageSize = configurator.getInt(MarketoInitializer.PAGE_SIZE, 10000);
        Context.Builder builder = new Context.Builder(context);
        builder.put(Parameters.PER_PAGE, pageSize);
        builder.put(Parameters.SORT, "+_id");
        builder.put(MetricFilter.REGISTERED_USER, 1);
        context = builder.build();

        try (BufferedWriter out = new BufferedWriter(new FileWriter(toFileReport))) {
            writeHeader(out);

            for (int currentPage = 1; ; currentPage++) {
                LOG.info("Proceeding page " + currentPage);

                context = context.cloneAndPut(Parameters.PAGE, currentPage);

                List<ValueData> profiles = getUsersProfiles(context);
                Map<String, Double> usersGbHours = getUsersGbHoursUse(profiles);
                Map<String, Long> usersCreatedDates = getUsersCreatedDates(profiles);

                writeUsersWithStatistics(activeUsers, newUsers, profiles, usersGbHours, usersCreatedDates, out);

                if (profiles.size() < pageSize) {
                    break;
                }
            }
        }
    }

    private void writeUsersWithStatistics(Set<ValueData> activeUsers,
                                          Set<ValueData> createdTodayUsers,
                                          List<ValueData> profiles,
                                          Map<String, Double> usersGbHours,
                                          Map<String, Long> usersCreatedDates,
                                          BufferedWriter out) throws IOException, ParseException {
        for (ValueData object : profiles) {
            Map<String, ValueData> profile = ((MapValueData)object).getAll();

            ValueData user = profile.get(ID);

            // Skip users without email which stored in a field ALIASES.
            if (activeUsers.contains(user)
                && toArray(profile.get(AbstractMetric.ALIASES)).length != 0) {
                String userId = user.getAsString();

                Double userGbHoursValue = usersGbHours.containsKey(userId) ? usersGbHours.get(userId) : 0.0D;
                Long userCreatedDate = usersCreatedDates.get(userId);

                writeUserWithStatistics(out, profile, user, createdTodayUsers.contains(user), userGbHoursValue, userCreatedDate);
            }
        }
    }

    private void writeUserWithStatistics(BufferedWriter out,
                                         Map<String, ValueData> profile,
                                         ValueData user,
                                         boolean isNewUser,
                                         Double userGbHours,
                                         @Nullable Long userCreatedDateLong) throws IOException, ParseException {
        List<ValueData> stat = getUsersStatistics(user.getAsString());
        String lastProductLoginDate = getLastProductLogin(user.getAsString());
        boolean accountLockdown = MarketoReportGeneratorUtils.isUserAccountsLockdown(user.getAsString());
        boolean ccAdded = MarketoReportGeneratorUtils.isUserCreditCardAdded(user.getAsString());

        String userCreatedDate = dateToString(userCreatedDateLong);

        String onPremSubAddedDate = dateToString(MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded(user.getAsString()));
        String onPremSubRemovedDate = dateToString(MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved(user.getAsString()));

        if (stat.isEmpty()) {
            MapValueData valueData = MapValueData.DEFAULT;
            writeStatistics(out,
                            valueData.getAll(),
                            profile,
                            lastProductLoginDate,
                            isNewUser,
                            userGbHours,
                            userCreatedDate,
                            accountLockdown,
                            ccAdded,
                            onPremSubAddedDate,
                            onPremSubRemovedDate);
        } else {
            MapValueData valueData = (MapValueData)stat.get(0);
            writeStatistics(out,
                            valueData.getAll(),
                            profile,
                            lastProductLoginDate,
                            isNewUser,
                            userGbHours,
                            userCreatedDate,
                            accountLockdown,
                            ccAdded,
                            onPremSubAddedDate,
                            onPremSubRemovedDate);
        }
    }

    private String dateToString(@Nullable Long date) {
        return date == null || date == 0 ? "" : new SimpleDateFormat(MetricRow.DEFAULT_DATE_FORMAT).format(date);
    }

    /**
     * @return date of user's last product login from metric USERS_ACTIVITY_LIST,
     * or empty string "" if this metric returns empty result.
     */
    private String getLastProductLogin(String user) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_ID, user);

        LongValueData value = ValueDataUtil.getAsLong(LastLoginTime.INSTANCE, builder.build());
        if (value.equals(LongValueData.DEFAULT)) {
            return "";
        }

        return new SimpleDateFormat(MetricRow.DEFAULT_DATE_FORMAT).format(value.getAsLong());
    }

    private Set<ValueData> getNewUsers(Context context) throws IOException {
        Context.Builder builder = new Context.Builder(context);
        builder.put(Parameters.FROM_DATE, context.getAsString(Parameters.TO_DATE));

        Metric activeUsersList = MetricFactory.getMetric(MetricType.CREATED_USERS_SET);
        SetValueData valueData = (SetValueData)activeUsersList.getValue(builder.build());

        return valueData.getAll();
    }

    private Set<ValueData> getActiveUsersByDatePeriod(Context context) throws ParseException, IOException {
        Metric activeUsersList = MetricFactory.getMetric(MetricType.ACTIVE_USERS_SET);
        SetValueData valueData = (SetValueData)activeUsersList.getValue(context);

        return valueData.getAll();
    }

    private List<ValueData> getUsersProfiles(Context context) throws IOException, ParseException {
        Context.Builder builder = new Context.Builder(context);
        builder.remove(Parameters.FROM_DATE);
        builder.remove(Parameters.TO_DATE);

        Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
        ListValueData valueData = (ListValueData)metric.getValue(builder.build());

        return valueData.getAll();
    }

    private Map<String, Double> getUsersGbHoursUse(List<ValueData> profiles) throws IOException {
        Map<String, Double> usersGbHours = new LinkedHashMap<>(profiles.size());

        Set<String> users = new LinkedHashSet<>(1000);
        for (int i = 0; i < profiles.size(); i++) {
            users.add(((MapValueData)profiles.get(i)).getAll().get(ID).getAsString());

            if (users.size() == 1000 || (i == profiles.size() - 1)) {
                Context.Builder builder = new Context.Builder();
                builder.put(MetricFilter.USER_ID, Utils.getFilterAsString(users));

                Metric metric = MetricFactory.getMetric(MetricType.USERS_GB_HOURS_LIST);
                ListValueData valueData = (ListValueData)metric.getValue(builder.build());

                for (ValueData mvd : valueData.getAll()) {
                    Double value = ((NumericValueData)(((MapValueData)mvd).getAll().get(GIGABYTE_RAM_HOURS))).getAsDouble();
                    usersGbHours.put(((MapValueData)mvd).getAll().get(USER).getAsString(), value);
                }

                users.clear();
            }
        }

        return usersGbHours;
    }

    private Map<String, Long> getUsersCreatedDates(List<ValueData> profiles) throws IOException {
        Map<String, Long> usersCreatedDate = new LinkedHashMap<>(profiles.size());

        Set<String> users = new LinkedHashSet<>(1000);
        for (int i = 0; i < profiles.size(); i++) {
            users.add(((MapValueData)profiles.get(i)).getAll().get(ID).getAsString());

            if (users.size() == 1000 || (i == profiles.size() - 1)) {
                Context.Builder builder = new Context.Builder();
                builder.put(MetricFilter.USER_ID, Utils.getFilterAsString(users));

                Metric metric = MetricFactory.getMetric(MetricType.CREATED_USERS_LIST);
                ListValueData valueData = (ListValueData)metric.getValue(builder.build());

                for (ValueData mvd : valueData.getAll()) {
                    Long value = ((LongValueData)(((MapValueData)mvd).getAll().get(DATE))).getAsLong();
                    usersCreatedDate.put(((MapValueData)mvd).getAll().get(USER).getAsString(), value);
                }

                users.clear();
            }
        }

        return usersCreatedDate;
    }

    private List<ValueData> getUsersStatistics(String user) throws IOException, ParseException {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_ID, user);

        Metric usersStatistics = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);
        ListValueData valueData = (ListValueData)usersStatistics.getValue(builder.build());

        return valueData.getAll();
    }

    private void writeHeader(BufferedWriter out) throws IOException {
        String header = "";
        String delimeter = ",";

        Iterator<String> iterator = headers.keySet().iterator();
        while (iterator.hasNext()) {
            header += headers.get(iterator.next()) + delimeter;
        }

        header = header.substring(0, header.length() - 1);  // remove last delimeter occurence

        out.write(header);
        out.newLine();
    }

    private void writeStatistics(BufferedWriter out,
                                 Map<String, ValueData> stat,
                                 Map<String, ValueData> profile,
                                 String lastProductLoginDate,
                                 boolean isCreatedTodayUser,
                                 Double userGbHoursValue,
                                 String userCreatedDate,
                                 boolean accountLockdown,
                                 boolean ccAdded,
                                 String onPremSubAdded,
                                 String onPremSubRemoved) throws IOException {
        writeString(out, StringValueData.valueOf(toArray(profile.get(AbstractMetric.ALIASES))[0]));
        out.write(",");

        writeInt(out, stat.get(BUILDS));
        out.write(",");

        writeInt(out, stat.get(DEPLOYS));
        out.write(",");

        boolean profileCompleted = ActOn.isProfileCompleted(profile);
        writeNotNullStr(out, Boolean.toString(profileCompleted));
        out.write(",");

        writeInt(out, stat.get(PROJECTS));
        out.write(",");

        writeInt(out, stat.get(UsersStatisticsList.RUNS));
        out.write(",");

        LongValueData time = (LongValueData)stat.get(UsersStatisticsList.TIME);
        if (time == null) {
            writeNotNullStr(out, "0");
        } else {
            writeNotNullStr(out, "" + (time.getAsLong() / (60 * 1000)));  // convert from millisec into minutes
        }
        out.write(",");

        writeInt(out, stat.get(UsersStatisticsList.LOGINS));
        out.write(",");

        writeNotNullStr(out, lastProductLoginDate);
        out.write(",");

        writeInt(out, ActOn.getPoints(stat, profile));
        out.write(",");

        //out.write(isCreatedTodayUser ? "1" :"0");
        //out.write(",");

        writeNotNullStr(out, String.format(MetricRow.DEFAULT_NUMERIC_FORMAT, userGbHoursValue));
        out.write(",");

        writeNotNullStr(out, userCreatedDate);
        out.write(",");

        writeNotNullStr(out, Boolean.toString(accountLockdown));
        out.write(",");

        writeNotNullStr(out, Boolean.toString(ccAdded));
        out.write(",");

        writeNotNullStr(out, onPremSubAdded);
        out.write(",");

        writeNotNullStr(out, onPremSubRemoved);

        out.newLine();
    }

    /** Write string value accordingly to CSV specification. */
    private void writeString(BufferedWriter out, ValueData valueData) throws IOException {
        if (valueData == null) {
            writeNotNullStr(out, "");
        } else {
            writeNotNullStr(out, valueData.getAsString());
        }
    }

    private void writeInt(BufferedWriter out, ValueData valueData) throws IOException {
        if (valueData == null) {
            writeNotNullStr(out, "0");
        } else {
            writeNotNullStr(out, valueData.getAsString());
        }
    }

    private void writeNotNullStr(BufferedWriter out, String str) throws IOException {
        out.write("\"");
        out.write(str.replace("\"", "\"\"")); // quoting
        out.write("\"");
    }

    /**
     * Last user's login time.
     */
    private static class LastLoginTime extends ReadBasedMetric {
        private static final LastLoginTime INSTANCE = new LastLoginTime();

        private LastLoginTime() {
            super(LastLoginTime.class.getSimpleName());
        }

        @Override
        public String getStorageCollectionName() {
            return getStorageCollectionName(MetricType.USERS_LOGGED_IN_TYPES);
        }

        @Override
        public Context applySpecificFilter(Context context) throws IOException {
            Context.Builder builder = new Context.Builder(context);
            builder.put(Parameters.PAGE, 1);
            builder.put(Parameters.PER_PAGE, 1);
            builder.put(Parameters.SORT, "-date");
            return builder.build();
        }

        @Override
        public String[] getTrackedFields() {
            return new String[]{DATE};
        }

        @Override
        public DBObject[] getSpecificDBOperations(Context clauses) {
            return new DBObject[0];
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        @Override
        public String getDescription() {
            return "Last login time";
        }
    }
}
