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
import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.NumericValueData;
import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataFactory;
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
import java.util.HashMap;
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
    public static final String ALIAS               = "alias";

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

        // Map with statistics of users of the same alias: <alias> -> Set<user_id>
        Map<String, LinkedHashSet<String>> duplicatedUsers = getDuplicatedUsers();

        if (processActiveUsersOnly) {
            // Add not included duplicated user in active users
            activeUsers = addUserIdsDuplicatedUsers(activeUsers, duplicatedUsers);
        }

        // Map with statistics of users of the same alias: <alias> -> cumulativeMarketoRow
        Map<String, Map<String, ValueData>> duplicatedUsersCumulativeMarketoRow = new HashMap<>();

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

                writeUsersWithStatistics(activeUsers, newUsers, profiles, usersGbHours, usersCreatedDates, duplicatedUsers, duplicatedUsersCumulativeMarketoRow, out);

                if (profiles.size() < pageSize) {
                    break;
                }
            }

            //Write statistics of users of the same alias.
            for ( Map<String, ValueData> row : duplicatedUsersCumulativeMarketoRow.values()) {
                writeStatistics(out, row);
            }
        }
    }

    private Set<ValueData> addUserIdsDuplicatedUsers(Set<ValueData> activeUsers, Map<String, LinkedHashSet<String>> duplicatedUsers) {
        Set<StringValueData> unScopedUserIds = new LinkedHashSet<>();

        for (Map.Entry<String, LinkedHashSet<String>> entry : duplicatedUsers.entrySet()) {
            for (String userId : entry.getValue()) {
                if (!activeUsers.contains(StringValueData.valueOf(userId))) {
                    unScopedUserIds.add(StringValueData.valueOf(userId));
                }
            }
        }

        Set<ValueData> result = new LinkedHashSet<>();
        result.addAll(activeUsers);
        result.addAll(unScopedUserIds);

        return result;
    }

    private Map<String, LinkedHashSet<String>> getDuplicatedUsers() throws IOException, ParseException {
        Map<String, LinkedHashSet<String>> map = new HashMap<>();

        final int pageSize = configurator.getInt(MarketoInitializer.PAGE_SIZE, 10000);
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.PER_PAGE, pageSize);
        builder.put(Parameters.SORT, "+date");
        builder.put(MetricFilter.REGISTERED_USER, 1);
        Context context = builder.build();

        for (int currentPage = 1; ; currentPage++) {
            context = context.cloneAndPut(Parameters.PAGE, currentPage);

            List<ValueData> profiles = getUsersProfiles(context);

            for (ValueData valueData : profiles) {
                Map<String, ValueData> profile = ((MapValueData)valueData).getAll();

                String userId = profile.get(ID).getAsString();
                String alias = toArray(profile.get(AbstractMetric.ALIASES))[0];

                if (map.containsKey(alias)) {
                    map.get(alias).add(userId);
                } else {
                    LinkedHashSet<String> ids = new LinkedHashSet<>();
                    ids.add(userId);

                    map.put(alias, ids);
                }
            }

            if (profiles.size() < pageSize) {
                break;
            }
        }

        Map<String, LinkedHashSet<String>> result = new HashMap<>();

        for(Map.Entry<String, LinkedHashSet<String>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    private void writeUsersWithStatistics(Set<ValueData> activeUsers,
                                          Set<ValueData> createdTodayUsers,
                                          List<ValueData> profiles,
                                          Map<String, Double> usersGbHours,
                                          Map<String, Long> usersCreatedDates,
                                          Map<String, LinkedHashSet<String>> duplicatedUsers,
                                          Map<String, Map<String, ValueData>> duplicatedUsersCumulativeMarketoRow,
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

                writeUserWithStatistics(out, profile, user, createdTodayUsers.contains(user), userGbHoursValue, userCreatedDate, duplicatedUsers,
                                        duplicatedUsersCumulativeMarketoRow);
            }
        }
    }

    private void writeUserWithStatistics(BufferedWriter out,
                                         Map<String, ValueData> profile,
                                         ValueData user,
                                         boolean isNewUser,
                                         Double userGbHours,
                                         @Nullable Long userCreatedDateLong,
                                         Map<String, LinkedHashSet<String>> duplicatedUsers,
                                         Map<String, Map<String, ValueData>> duplicatedUsersCumulativeMarketoRow) throws IOException, ParseException {
        List<ValueData> stat = getUsersStatistics(user.getAsString());
        String lastProductLoginDate = getLastProductLogin(user.getAsString());
        boolean accountLockdown = MarketoReportGeneratorUtils.isUserAccountsLockdown(user.getAsString());
        boolean ccAdded = MarketoReportGeneratorUtils.isUserCreditCardAdded(user.getAsString());

        String userCreatedDate = dateToString(userCreatedDateLong);

        String onPremSubAddedDate = dateToString(MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded(user.getAsString()));
        String onPremSubRemovedDate = dateToString(MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved(user.getAsString()));

        MapValueData statValueData;
        if (stat.isEmpty()) {
            statValueData = MapValueData.DEFAULT;
        } else {
            statValueData = (MapValueData)stat.get(0);
        }

        Map<String, ValueData> row = createRowData(statValueData.getAll(),
                                                   profile,
                                                   lastProductLoginDate,
                                                   isNewUser,
                                                   userGbHours,
                                                   userCreatedDate,
                                                   accountLockdown,
                                                   ccAdded,
                                                   onPremSubAddedDate,
                                                   onPremSubRemovedDate);

        String alias = toArray(profile.get(AbstractMetric.ALIASES))[0];
        if (duplicatedUsers.containsKey(alias)) {
            accumulateDuplicatedUsersMarketoRow(row, duplicatedUsersCumulativeMarketoRow, isLatestUser(profile.get(AbstractMetric.ID).getAsString(),
                                                                                                       duplicatedUsers.get(alias)));
        } else {
            writeStatistics(out, row);
        }
    }

    private boolean isLatestUser(String userId, LinkedHashSet<String> duplicatedUser ) throws RuntimeException {
        if (!duplicatedUser.contains(userId)) {
            throw new RuntimeException("Users id " + userId + " dose not  contain it duplicated user set " + duplicatedUser.toString());
        }

        return duplicatedUser.toArray()[duplicatedUser.size()-1].equals(userId);
    }

    private Map<String, ValueData> createRowData(Map<String, ValueData> userStatistics,
                                                 Map<String, ValueData> profile,
                                                 String lastProductLoginDate,
                                                 boolean isCreatedTodayUser,
                                                 Double userGbHoursValue,
                                                 String userCreatedDate,
                                                 boolean accountLockdown,
                                                 boolean ccAdded,
                                                 String onPremSubAdded,
                                                 String onPremSubRemoved) {
        Map<String, ValueData> marketoRow = new LinkedHashMap<>();
        marketoRow.put(ALIAS, StringValueData.valueOf(toArray(profile.get(AbstractMetric.ALIASES))[0]));
        marketoRow.put(BUILDS, getLongValue(BUILDS, userStatistics));
        marketoRow.put(DEPLOYS, getLongValue(DEPLOYS, userStatistics));

        boolean profileCompleted = isProfileCompleted(profile);
        marketoRow.put(PROFILE_COMPLETED, StringValueData.valueOf(Boolean.toString(profileCompleted)));

        marketoRow.put(PROJECTS, getLongValue(PROJECTS, userStatistics));
        marketoRow.put(RUNS, getLongValue(RUNS, userStatistics));

        marketoRow.put(TIME, getLongValue(TIME, userStatistics));
        marketoRow.put(LOGINS, getLongValue(LOGINS, userStatistics));
        marketoRow.put(LAST_PRODUCT_LOGIN, StringValueData.valueOf(lastProductLoginDate));
        marketoRow.put(POINTS, getPoints(userStatistics, profile));
        //marketoRow.put(NEW_USER, StringValueData.valueOf(isCreatedTodayUser ? "1" : "0"));
        marketoRow.put(GIGABYTE_RAM_HOURS, DoubleValueData.valueOf(userGbHoursValue));
        marketoRow.put(SING_UP_DATE, StringValueData.valueOf(userCreatedDate));
        marketoRow.put(ACCOUNT_LOCKDOWN, StringValueData.valueOf(Boolean.toString(accountLockdown)));
        marketoRow.put(CC_ADDED, StringValueData.valueOf(Boolean.toString(ccAdded)));
        marketoRow.put(ON_PREM_SUB_ADDED, StringValueData.valueOf(onPremSubAdded));
        marketoRow.put(ON_PREM_SUB_REMOVED, StringValueData.valueOf(onPremSubRemoved));

        return marketoRow;
    }

    private void accumulateDuplicatedUsersMarketoRow(Map<String, ValueData> marketoRow,
                                                     Map<String, Map<String, ValueData>> duplicatedUsersCumulativeMarketoRow,
                                                     boolean isLatestUser) throws ParseException {
        Map<String, ValueData> lastUserMarketoRow = duplicatedUsersCumulativeMarketoRow.get(marketoRow.get(ALIAS).getAsString());
        if (lastUserMarketoRow == null) {
            duplicatedUsersCumulativeMarketoRow.put(marketoRow.get(ALIAS).getAsString(), marketoRow);
        } else {
            // accumulate data
            lastUserMarketoRow.put(BUILDS, lastUserMarketoRow.get(BUILDS).add(marketoRow.get(BUILDS)));
            lastUserMarketoRow.put(DEPLOYS, lastUserMarketoRow.get(DEPLOYS).add(marketoRow.get(DEPLOYS)));
            lastUserMarketoRow.put(PROJECTS, lastUserMarketoRow.get(PROJECTS).add(marketoRow.get(PROJECTS)));
            lastUserMarketoRow.put(RUNS, lastUserMarketoRow.get(RUNS).add(marketoRow.get(RUNS)));
            lastUserMarketoRow.put(TIME, lastUserMarketoRow.get(TIME).add(marketoRow.get(TIME)));
            lastUserMarketoRow.put(LOGINS, lastUserMarketoRow.get(LOGINS).add(marketoRow.get(LOGINS)));
            lastUserMarketoRow.put(POINTS, lastUserMarketoRow.get(POINTS).add(marketoRow.get(POINTS)));
            lastUserMarketoRow.put(GIGABYTE_RAM_HOURS, lastUserMarketoRow.get(GIGABYTE_RAM_HOURS).add(marketoRow.get(GIGABYTE_RAM_HOURS)));

            if (isLatestUser) {
                lastUserMarketoRow.put(PROFILE_COMPLETED, marketoRow.get(PROFILE_COMPLETED));
                //lastUserMarketoRow.put(NEW_USER, marketoRow.get(NEW_USER));
                lastUserMarketoRow.put(LAST_PRODUCT_LOGIN, marketoRow.get(LAST_PRODUCT_LOGIN));
                lastUserMarketoRow.put(SING_UP_DATE, marketoRow.get(SING_UP_DATE));
                lastUserMarketoRow.put(ACCOUNT_LOCKDOWN, marketoRow.get(ACCOUNT_LOCKDOWN));
                lastUserMarketoRow.put(CC_ADDED, marketoRow.get(CC_ADDED));
                lastUserMarketoRow.put(ON_PREM_SUB_ADDED, marketoRow.get(ON_PREM_SUB_ADDED));
                lastUserMarketoRow.put(ON_PREM_SUB_REMOVED, marketoRow.get(ON_PREM_SUB_REMOVED));
            }
        }
    }

    private ValueData getLongValue(String name,  Map<String, ValueData> userStatistics) {
        return userStatistics.containsKey(name) ? userStatistics.get(name) : LongValueData.DEFAULT;
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

    private void writeStatistics(BufferedWriter out, Map<String, ValueData> row) throws IOException {

        int fieldsWrite = 0;
        for (Map.Entry<String, ValueData> data : row.entrySet()) {
            String key = data.getKey();
            ValueData value = data.getValue();

            if (key.equals(TIME)) {
                if (value == null) {
                    writeNotNullStr(out, "0");
                } else {
                    LongValueData time = (LongValueData) value;
                    writeNotNullStr(out, "" + (time.getAsLong() / (60 * 1000)));  // convert from millisec into minutes
                }

            } else if (key.equals(GIGABYTE_RAM_HOURS)) {
                DoubleValueData userGbHoursValue = (DoubleValueData) value;
                out.write(String.format(MetricRow.DEFAULT_NUMERIC_FORMAT, userGbHoursValue.getAsDouble()));

            } else if (value instanceof LongValueData) {
                writeInt(out, value);

            } else {
                writeString(out, value);
            }

            if (++fieldsWrite != row.size()) {
                out.write(",");
            }

        }

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

    protected boolean isProfileCompleted(Map<String, ValueData> profile) {
        return profile.containsKey(AbstractMetric.ID)
               && profile.containsKey(AbstractMetric.USER_FIRST_NAME)
               && profile.containsKey(AbstractMetric.USER_LAST_NAME)
               && profile.containsKey(AbstractMetric.USER_COMPANY)
               && profile.containsKey(AbstractMetric.USER_JOB)
               && profile.containsKey(AbstractMetric.USER_PHONE)
               && !profile.get(AbstractMetric.ID).getAsString().isEmpty()
               && !profile.get(AbstractMetric.USER_FIRST_NAME).getAsString().isEmpty()
               && !profile.get(AbstractMetric.USER_LAST_NAME).getAsString().isEmpty()
               && !profile.get(AbstractMetric.USER_COMPANY).getAsString().isEmpty()
               && !profile.get(AbstractMetric.USER_JOB).getAsString().isEmpty()
               && !profile.get(AbstractMetric.USER_PHONE).getAsString().isEmpty();
    }

    /**
     * Get total Marketing Qualified Leads (MQL) Score from Product due to example {@link http://jsfiddle.net/ecavazos/64g9b/}.
     *
     * @param profile
     * @param statistics
     */
    public ValueData getPoints(Map<String, ValueData> statistics, Map<String, ValueData> profile) {
        long total = 0;

        if (statistics.size() > 0) {
            int logins = new Integer(statistics.get(UsersStatisticsList.LOGINS).toString());
            int projects = new Integer(statistics.get(UsersStatisticsList.PROJECTS).toString());
            int builds = new Integer(statistics.get(UsersStatisticsList.BUILDS).toString());
            int runs = new Integer(statistics.get(UsersStatisticsList.RUNS).toString());
            int debugs = new Integer(statistics.get(UsersStatisticsList.DEBUGS).toString());
            int deploys = new Integer(statistics.get(UsersStatisticsList.DEPLOYS).toString());
            int factories = new Integer(statistics.get(UsersStatisticsList.FACTORIES).toString());
            int invitations = new Integer(statistics.get(UsersStatisticsList.INVITES).toString());

            boolean profileCompleted = isProfileCompleted(profile);
            long time = getTimeInHours(statistics, UsersStatisticsList.TIME);
            long buildTime = getTimeInHours(statistics, UsersStatisticsList.BUILD_TIME);
            long runTime = getTimeInHours(statistics, UsersStatisticsList.RUN_TIME);

            /** compute MQL Score from Product **/
            total += logins * 2;
            total += projects * 2;
            total += builds * 2;
            total += runs * 2;
            total += debugs * 2;
            total += deploys * 10;
            total += factories * 10;
            total += invitations * 10;

            // compute Metric Measurement Points
            total += (logins > 5) ? 5 : 0;
            total += (projects > 5) ? 5 : 0;
            total += (deploys > 5) ? 10 : 0;
            total += (profileCompleted) ? 5 : 0;
            total += (time > 40) ? 50 : 0;
            total += (buildTime > 3) ? 50 : 0;
            total += (runTime > 3) ? 50 : 0;
        }

        return ValueDataFactory.createValueData(total);
    }

    private long getTimeInHours(Map<String, ValueData> statistics, String fieldName) {
        return Math.round(new Long(statistics.get(fieldName).toString()) / (360 * 1000));
    }

    /**
     * Last user's login time.
     */
    private static class LastLoginTime extends ReadBasedMetric {
        private static final LastLoginTime INSTANCE = new LastLoginTime();

        private LastLoginTime() {
            super(LastLoginTime.class.getSimpleName());
        }

        /** {@inheritDoc} */
        @Override
        public String getStorageCollectionName() {
            return getStorageCollectionName(MetricType.USERS_LOGGED_IN_TYPES);
        }

        /** {@inheritDoc} */
        @Override
        public Context applySpecificFilter(Context context) throws IOException {
            Context.Builder builder = new Context.Builder(context);
            builder.put(Parameters.PAGE, 1);
            builder.put(Parameters.PER_PAGE, 1);
            builder.put(Parameters.SORT, "-date");
            return builder.build();
        }

        /** {@inheritDoc} */
        @Override
        public String[] getTrackedFields() {
            return new String[]{DATE};
        }

        /** {@inheritDoc} */
        @Override
        public DBObject[] getSpecificDBOperations(Context clauses) {
            return new DBObject[0];
        }

        /** {@inheritDoc} */
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        /** {@inheritDoc} */
        @Override
        public String getDescription() {
            return "Last login time";
        }
    }
}
