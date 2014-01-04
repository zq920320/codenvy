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
package com.codenvy.analytics.services;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.ScriptType;

import org.apache.pig.data.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class LogChecker extends Feature {

    private static final Logger LOG = LoggerFactory.getLogger(LogChecker.class);

    private static final String AVAILABLE    = "log-checker.available";
    private static final String MAIL_TEXT    = "log-checker.mail.text";
    private static final String MAIL_SUBJECT = "log-checker.mail.subject";
    private static final String MAIL_TO      = "log-checker.mail.to";

    @Override
    public boolean isAvailable() {
        return Configurator.getBoolean(AVAILABLE);
    }

    @Override
    protected Map<String, String> initializeDefaultContext() throws ParseException {
        return Utils.initializeContext(Parameters.TimeUnit.DAY);
    }

    @Override
    protected void putParametersInContext(Map<String, String> context) {
        Parameters.USER.put(context, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(context, Parameters.WS_TYPES.ANY.name());
    }

    @Override
    protected void doExecute(Map<String, String> context) throws IOException {
        LOG.info("LogChecker is started");
        long start = System.currentTimeMillis();

        try {
            File reportFile = getReport(context);

            Calendar toDate = Utils.getToDate(context);
            String date = new SimpleDateFormat("yyyy-MM-dd").format(toDate.getTime());

            sendReport(reportFile, date);
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            LOG.info("LogChecker is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    private File getReport(Map<String, String> context) throws IOException {
        File reportFile = new File(Configurator.getTmpDir(), "report.txt");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(reportFile))) {
            writeReport(ScriptType.CHECK_LOGS_1, context, out);
            writeReport(ScriptType.CHECK_LOGS_2, context, out);
        }

        return reportFile;
    }

    private void writeReport(ScriptType scriptType,
                             Map<String, String> context,
                             BufferedWriter out) throws IOException {

        Iterator<Tuple> iterator = PigServer.executeAndReturn(scriptType, context);
        while (iterator.hasNext()) {
            out.write(iterator.next().toString());
            out.newLine();
        }
    }

    private void sendReport(File reportFile, String date) throws IOException {
        MailService.Builder builder = new MailService.Builder();
        builder.attach(reportFile);
        builder.setSubject(Configurator.getString(MAIL_SUBJECT).replace("[date]", date));
        builder.setText(Configurator.getString(MAIL_TEXT));
        builder.setTo(Configurator.getString(MAIL_TO));
        MailService mailService = builder.build();

        mailService.send();
    }
}
