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
package com.codenvy.analytics.services.marketo;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.users.UsersStatisticsList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static com.codenvy.analytics.Utils.toArray;
import static com.codenvy.analytics.metrics.AbstractMetric.*;

/**
 * @author Alexander Reshetnyak
 */
@Singleton
public class MarketoReportGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(MarketoReportGenerator.class);

    public static final String PROFILE_COMPLETED = "Profile Complete";

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
    }};

    @Inject
    public MarketoReportGenerator(Configurator configurator) {
        this.configurator = configurator;
    }

    public void prepareReport(File toFileReport,
                              Context context,
                              Context activeUsersContext,
                              boolean processActiveUsersOnly)
            throws IOException, ParseException {

        Set<ValueData> activeUsers = processActiveUsersOnly ? getActiveUsersByDatePeriod(activeUsersContext)
                                                            : new HashSet<ValueData>() {
                                                                @Override
                                                                public boolean contains(Object o) {
                                                                    return true;
                                                                }
                                                            };

        final int pageSize = configurator.getInt(MarketoInitializer.PAGE_SIZE, 10000);
        context = context.cloneAndPut(Parameters.PER_PAGE, pageSize);

        try (BufferedWriter out = new BufferedWriter(new FileWriter(toFileReport))) {
            writeHeader(out);

            for (int currentPage = 1; ; currentPage++) {
                LOG.info("Proceeding page " + currentPage);

                context = context.cloneAndPut(Parameters.PAGE, currentPage);

                List<ValueData> profiles = getUsersProfiles(context);
                writeUsersWithStatistics(activeUsers, profiles, out);

                if (profiles.size() < pageSize) {
                    break;
                }
            }
        }
    }

    private void writeUsersWithStatistics(Set<ValueData> activeUsers,
                                          List<ValueData> profiles,
                                          BufferedWriter out) throws IOException, ParseException {
        for (ValueData object : profiles) {
            Map<String, ValueData> profile = ((MapValueData)object).getAll();

            ValueData user = profile.get(ID);

            // Skip users without email which stored in a field ALIASES.
            if (activeUsers.contains(user)
                && toArray(profile.get(AbstractMetric.ALIASES)).length != 0) {
                writeUserWithStatistics(out, profile, user);
            }
        }
    }

    private void writeUserWithStatistics(BufferedWriter out,
                                         Map<String, ValueData> profile,
                                         ValueData user) throws IOException, ParseException {
        List<ValueData> stat = getUsersStatistics(user.getAsString());
        if (stat.isEmpty()) {
            MapValueData valueData = MapValueData.DEFAULT;
            writeStatistics(out, valueData.getAll(), profile);
        } else {
            MapValueData valueData = (MapValueData)stat.get(0);
            writeStatistics(out, valueData.getAll(), profile);
        }
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

    private List<ValueData> getUsersStatistics(String user) throws IOException, ParseException {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, user);

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
                                 Map<String, ValueData> profile) throws IOException {
        writeString(out, StringValueData.valueOf(toArray(profile.get(AbstractMetric.ALIASES))[0]));
        out.write(",");

        writeInt(out, stat.get(BUILDS));
        out.write(",");

        writeInt(out, stat.get(DEPLOYS));
        out.write(",");

        boolean profileCompleted = isProfileCompleted(profile);
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

    private boolean isProfileCompleted(Map<String, ValueData> profile) {
        return profile.containsKey(ID)
               && profile.containsKey(AbstractMetric.USER_FIRST_NAME)
               && profile.containsKey(AbstractMetric.USER_LAST_NAME)
               && profile.containsKey(AbstractMetric.USER_COMPANY)
               && profile.containsKey(AbstractMetric.USER_JOB)
               && profile.containsKey(AbstractMetric.USER_PHONE)
               && !profile.get(ID).getAsString().isEmpty()
               && !profile.get(AbstractMetric.USER_FIRST_NAME).getAsString().isEmpty()
               && !profile.get(AbstractMetric.USER_LAST_NAME).getAsString().isEmpty()
               && !profile.get(AbstractMetric.USER_COMPANY).getAsString().isEmpty()
               && !profile.get(AbstractMetric.USER_JOB).getAsString().isEmpty()
               && !profile.get(AbstractMetric.USER_PHONE).getAsString().isEmpty();
    }
}
