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


package com.codenvy.analytics.services.acton;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.MailService;
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.users.AbstractUsersProfile;
import com.codenvy.analytics.metrics.users.UsersStatisticsList;
import com.codenvy.analytics.services.Feature;

import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@Singleton
public class ActOn extends Feature {
    private static final Logger LOG = LoggerFactory.getLogger(ActOn.class);

    public static final  String FILE_NAME = "ideuserupdate.csv";
    private static final String PAGE_SIZE = "analytics.acton.page_size";

    private static final String AVAILABLE = "analytics.acton.available";

    private static final String MAIL_TEXT    = "analytics.acton.mail_text";
    private static final String MAIL_SUBJECT = "analytics.acton.mail_subject";
    private static final String MAIL_TO      = "analytics.acton.mail_to";

    private static final String FTP_PASSWORD    = "analytics.acton.ftp_password";
    private static final String FTP_LOGIN       = "analytics.acton.ftp_login";
    private static final String FTP_SERVER      = "analytics.acton.ftp_server";
    private static final String FTP_PORT        = "analytics.acton.ftp_port";
    private static final String FTP_TIMEOUT     = "analytics.acton.ftp_timeout";
    private static final String FTP_MAX_EFFORTS = "analytics.acton.ftp_maxEfforts";
    private static final String FTP_AUTH        = "analytics.acton.ftp_auth";

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public static final String ACTIVE            = "active";
    public static final String PROFILE_COMPLETED = "profileCompleted";
    public static final String POINTS            = "points";

    /** Map users_statistics collection columns into the csv file headers. */
    @SuppressWarnings("serial")
    public static final LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>() {{
        put(AbstractMetric.ID, "email");
        put(AbstractMetric.USER_FIRST_NAME, "firstName");
        put(AbstractMetric.USER_LAST_NAME, "lastName");
        put(AbstractMetric.USER_PHONE, "phone");
        put(AbstractMetric.USER_COMPANY, "company");
        put(AbstractMetric.CREATION_DATE, AbstractMetric.CREATION_DATE);
        put(UsersStatisticsList.PROJECTS, "projects");
        put(UsersStatisticsList.BUILDS, "builts");
        put(UsersStatisticsList.RUNS, "runs");
        put(UsersStatisticsList.DEPLOYS, "deployments");
        put(UsersStatisticsList.TIME, "spentTime");
        put(ACTIVE, ACTIVE);
        put(UsersStatisticsList.INVITES, UsersStatisticsList.INVITES);
        put(UsersStatisticsList.FACTORIES, UsersStatisticsList.FACTORIES);
        put(UsersStatisticsList.DEBUGS, UsersStatisticsList.DEBUGS);
        put(UsersStatisticsList.LOGINS, UsersStatisticsList.LOGINS);
        put(UsersStatisticsList.BUILD_TIME, "build-time");
        put(UsersStatisticsList.RUN_TIME, "run-time");
        put(PROFILE_COMPLETED, PROFILE_COMPLETED);
        put(UsersStatisticsList.PAAS_DEPLOYS, "paas-deploys");
        put(POINTS, POINTS);
    }};

    private final Configurator configurator;

    @Inject
    public ActOn(Configurator configurator) {
        this.configurator = configurator;
    }

    @Override
    public boolean isAvailable() {
        return configurator.getBoolean(AVAILABLE);
    }

    @Override
    protected void doExecute(Context context) throws IOException, ParseException {
        LOG.info("ActOn is started");
        long start = System.currentTimeMillis();

        try {
            File file = prepareFile(context);

            transferToFtp(file);
            sendNotificationMail();
        } finally {
            LOG.info("ActOn is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    protected void sendNotificationMail() throws IOException {
        MailService.Builder builder = new MailService.Builder();
        builder.setSubject(configurator.getString(MAIL_SUBJECT));
        builder.setText(configurator.getString(MAIL_TEXT));
        builder.setTo(configurator.getString(MAIL_TO));
        MailService mailService = builder.build();

        mailService.send();
    }

    /** Sends file directly to FTP server. */
    private void transferToFtp(File file) throws IOException {
        for (int i = 0; i < configurator.getInt(FTP_MAX_EFFORTS); i++) {
            FTPSClient ftp = new FTPSClient(configurator.getString(FTP_AUTH), false);

            try {
                doOpenConnection(ftp);
                doTransfer(file, ftp);
                doCloseConnection(ftp);

                break; // file transferred successfully

            } catch (SocketTimeoutException e) {
                LOG.error(e.getMessage());

            } catch (IOException e) {
                if (ftp.isConnected()) {
                    ftp.disconnect();
                }

                throw e;
            }
        }
    }

    private void doCloseConnection(FTPSClient ftp) throws IOException {
        ftp.logout();
        ftp.disconnect();
    }

    private void doOpenConnection(FTPSClient ftp) throws IOException {
        ftp.setDefaultTimeout(configurator.getInt(FTP_TIMEOUT));
        ftp.connect(configurator.getString(FTP_SERVER), configurator.getInt(FTP_PORT));

        ftp.setSendBufferSize(65536);
        ftp.setBufferSize(65536);

        if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
            throw new IOException("FTP connection failed");
        }

        if (!ftp.login(configurator.getString(FTP_LOGIN), configurator.getString(FTP_PASSWORD))) {
            ftp.logout();
            throw new IOException("FTP login failed");
        }

        ftp.enterLocalPassiveMode();
        ftp.execPBSZ(0);
        ftp.execPROT("P");
        ftp.setFileType(FTPSClient.ASCII_FILE_TYPE);
    }

    private void doTransfer(File file, FTPSClient ftp) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            if (!ftp.storeFile(file.getName(), in)) {
                throw new IOException("File " + file.getName() + " was not transferred to the server");
            }
        }
    }

    protected File prepareFile(Context context) throws IOException, ParseException {
        File file = new File(configurator.getTmpDir(), FILE_NAME);

        int pageSize = configurator.getInt(PAGE_SIZE, 10000);

        Set<ValueData> activeUsers = getActiveUsersLastMonth(context);

        context = context.cloneAndPut(Parameters.PER_PAGE, pageSize);

        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            writeHeader(out);

            for (int currentPage = 1; ; currentPage++) {
                context = context.cloneAndPut(Parameters.PAGE, currentPage++);

                List<ValueData> usersStatistics = getUsersStatistics(context);
                writeUsersWithStatistics(activeUsers, usersStatistics, out);

                if (usersStatistics.size() < pageSize) {
                    break;
                }
            }
        }

        return file;
    }


    private void writeUsersWithStatistics(Set<ValueData> activeUsers,
                                          List<ValueData> usersStatistics,
                                          BufferedWriter out) throws IOException {

        for (ValueData object : usersStatistics) {
            Map<String, ValueData> stat = ((MapValueData)object).getAll();
            ValueData userEmail = stat.get(UsersStatisticsList.USER);

            boolean isActive = activeUsers.contains(userEmail);

            Context.Builder builder = new Context.Builder();
            builder.put(MetricFilter.USER, userEmail.getAsString());

            Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
            ListValueData list = ValueDataUtil.getAsList(metric, builder.build());
            MapValueData profile = list.size() == 0 ? MapValueData.DEFAULT : (MapValueData)list.getAll().get(0);

            if (profile != null) {
                writeStatistics(out, stat, profile.getAll(), isActive);
            }
        }
    }

    private Set<ValueData> getActiveUsersLastMonth(Context context) throws ParseException, IOException {
        Calendar calendar = context.getAsDate(Parameters.TO_DATE);
        calendar.add(Calendar.DAY_OF_MONTH, -29);

        Context.Builder builder = new Context.Builder(context);
        builder.put(Parameters.FROM_DATE, calendar);

        Metric activeUsersList = MetricFactory.getMetric(MetricType.ACTIVE_USERS_SET);
        SetValueData valueData = (SetValueData)activeUsersList.getValue(builder.build());

        return valueData.getAll();
    }

    private List<ValueData> getUsersStatistics(Context context) throws IOException, ParseException {
        Context.Builder builder = new Context.Builder(context);
        builder.putDefaultValue(Parameters.FROM_DATE);
        builder.put(MetricFilter.USER, Parameters.USER_TYPES.REGISTERED.name());

        Metric usersStatistics = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);
        ListValueData valueData = (ListValueData)usersStatistics.getValue(builder.build());

        return valueData.getAll();
    }

    private void writeStatistics(BufferedWriter out,
                                 Map<String, ValueData> stat,
                                 Map<String, ValueData> profile,
                                 boolean isActive) throws IOException {

        writeString(out, profile.get(AbstractMetric.ID));
        out.write(",");

        writeString(out, profile.get(AbstractUsersProfile.USER_FIRST_NAME));
        out.write(",");

        writeString(out, profile.get(AbstractUsersProfile.USER_LAST_NAME));
        out.write(",");

        writeString(out, profile.get(AbstractUsersProfile.USER_PHONE));
        out.write(",");

        writeString(out, profile.get(AbstractUsersProfile.USER_COMPANY));
        out.write(",");

        LongValueData valueData = (LongValueData)profile.get(AbstractUsersProfile.CREATION_DATE);
        if (valueData == null) {
            writeString(out, StringValueData.DEFAULT);
        } else {
            String creationDate = df.format(new Date(valueData.getAsLong()));
            writeString(out, StringValueData.valueOf(creationDate));
        }
        out.write(",");

        writeInt(out, stat.get(UsersStatisticsList.PROJECTS));
        out.write(",");

        writeInt(out, stat.get(UsersStatisticsList.BUILDS));
        out.write(",");

        writeInt(out, stat.get(UsersStatisticsList.RUNS));
        out.write(",");

        writeInt(out, stat.get(UsersStatisticsList.DEPLOYS));
        out.write(",");

        LongValueData time = (LongValueData)stat.get(UsersStatisticsList.TIME);
        if (time == null) {
            writeNotNullStr(out, "0");
        } else {
            writeNotNullStr(out, "" + (time.getAsLong() / (60 * 1000)));  // convert from millisec into minutes
        }
        out.write(",");

        writeNotNullStr(out, Boolean.toString(isActive));
        out.write(",");

        writeInt(out, stat.get(UsersStatisticsList.INVITES));
        out.write(",");

        writeInt(out, stat.get(UsersStatisticsList.FACTORIES));
        out.write(",");

        writeInt(out, stat.get(UsersStatisticsList.DEBUGS));
        out.write(",");

        writeInt(out, stat.get(UsersStatisticsList.LOGINS));
        out.write(",");

        LongValueData buildTime = (LongValueData)stat.get(UsersStatisticsList.BUILD_TIME);
        if (buildTime == null) {
            writeNotNullStr(out, "0");
        } else {
            writeNotNullStr(out, "" + (buildTime.getAsLong() / 1000));  // convert from millisec into secs
        }
        out.write(",");

        LongValueData runTime = (LongValueData)stat.get(UsersStatisticsList.RUN_TIME);
        if (runTime == null) {
            writeNotNullStr(out, "0");
        } else {
            writeNotNullStr(out, "" + (runTime.getAsLong() / 1000));  // convert from millisec into secs
        }
        out.write(",");

        boolean profileCompleted = isProfileCompleted(profile);
        writeNotNullStr(out, Boolean.toString(profileCompleted));
        out.write(",");

        writeInt(out, stat.get(UsersStatisticsList.PAAS_DEPLOYS));
        out.write(",");

        writeInt(out, getPoints(stat, profile));

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

    /**
     * Get total Marketing Qualified Leads (MQL) Score from Product due to example {@link http://jsfiddle.net/ecavazos/64g9b/}.
     *
     * @param profile
     * @param statistics
     */
    private ValueData getPoints(Map<String, ValueData> statistics, Map<String, ValueData> profile) {
        long total = 0;

        if (statistics.size() > 0) {
            int logins = new Integer(statistics.get(UsersStatisticsList.LOGINS).toString());
            int projects = new Integer(statistics.get(UsersStatisticsList.PROJECTS).toString());
            int builds = new Integer(statistics.get(UsersStatisticsList.BUILDS).toString());
            int runs = new Integer(statistics.get(UsersStatisticsList.RUNS).toString());
            int debugs = new Integer(statistics.get(UsersStatisticsList.DEBUGS).toString());
            int deploys = new Integer(statistics.get(UsersStatisticsList.DEPLOYS).toString());
            int paasDeploys = new Integer(statistics.get(UsersStatisticsList.PAAS_DEPLOYS).toString());
            int factories = new Integer(statistics.get(UsersStatisticsList.FACTORIES).toString());
            int invitations = new Integer(statistics.get(UsersStatisticsList.INVITES).toString());

            boolean profileCompleted = isProfileCompleted(profile);
            int time = getTimeInHours(statistics, UsersStatisticsList.TIME);
            int buildTime = getTimeInHours(statistics, UsersStatisticsList.BUILD_TIME);
            int runTime = getTimeInHours(statistics, UsersStatisticsList.RUN_TIME);

            /** compute MQL Score from Product **/
            total += logins * 2;
            total += projects * 2;
            total += builds * 2;
            total += runs * 2;
            total += debugs * 2;
            total += deploys * 2;
            total += paasDeploys * 10;
            total += factories * 10;
            total += invitations * 10;

            // compute Metric Measurement Points
            total += (logins > 5) ? 5 : 0;
            total += (projects > 5) ? 5 : 0;
            total += (paasDeploys > 5) ? 10 : 0;
            total += (profileCompleted) ? 5 : 0;
            total += (time > 40) ? 50 : 0;
            total += (buildTime > 3) ? 50 : 0;
            total += (runTime > 3) ? 50 : 0;
        }

        return ValueDataFactory.createValueData(total);
    }

    private int getTimeInHours(Map<String, ValueData> statistics, String fieldName) {
        return Math.round(new Integer(statistics.get(fieldName).toString()) / (360 * 1000));
    }

    private boolean isProfileCompleted(Map<String, ValueData> profile) {
        return profile.containsKey(AbstractMetric.ID)
               && profile.containsKey(AbstractMetric.USER_FIRST_NAME)
               && profile.containsKey(AbstractMetric.USER_LAST_NAME)
               && profile.containsKey(AbstractMetric.USER_JOB)
               && profile.containsKey(AbstractMetric.USER_PHONE)
               && profile.containsKey(AbstractMetric.ID)
               && !profile.get(AbstractMetric.ID).getAsString().isEmpty()
               && !profile.get(AbstractMetric.USER_FIRST_NAME).getAsString().isEmpty()
               && !profile.get(AbstractMetric.USER_LAST_NAME).getAsString().isEmpty()
               && !profile.get(AbstractMetric.USER_COMPANY).getAsString().isEmpty()
               && !profile.get(AbstractMetric.USER_JOB).getAsString().isEmpty()
               && !profile.get(AbstractMetric.USER_PHONE).getAsString().isEmpty();
    }
}
