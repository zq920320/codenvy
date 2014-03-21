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
package com.codenvy.analytics.services.logchecker;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.MailService;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.services.Feature;

import org.apache.pig.data.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@Singleton
public class LogChecker extends Feature {

    private static final Logger LOG = LoggerFactory.getLogger(LogChecker.class);

    private static final String AVAILABLE    = "analytics.log-checker.available";
    private static final String MAIL_TEXT    = "analytics.log-checker.mail_text";
    private static final String MAIL_SUBJECT = "analytics.log-checker.mail_subject";
    private static final String MAIL_TO      = "analytics.log-checker.mail_to";

    private final Configurator configurator;

    @Inject
    public LogChecker(Configurator configurator) {
        this.configurator = configurator;
    }

    @Override
    public boolean isAvailable() {
        return configurator.getBoolean(AVAILABLE);
    }

    @Override
    protected void putParametersInContext(Context.Builder builder) {
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());
    }

    @Override
    protected void doExecute(Context context) throws IOException {
        LOG.info("logchecker is started");
        long start = System.currentTimeMillis();

        try {
            File reportFile = getReport(context);

            Calendar toDate = context.getAsDate(Parameters.TO_DATE);
            String date = new SimpleDateFormat("yyyy-MM-dd").format(toDate.getTime());

            sendReport(reportFile, date);
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            LOG.info("logchecker is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    private File getReport(Context context) throws IOException, ParseException {
        File reportFile = new File(configurator.getTmpDir(), "report.txt");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(reportFile))) {
            writeReport(ScriptType.CHECK_LOGS_1, context, out);
        }

        return reportFile;
    }

    private void writeReport(ScriptType scriptType,
                             Context context,
                             BufferedWriter out) throws IOException, ParseException {

        PigServer pigServer = Injector.getInstance(PigServer.class);
        try {
            Iterator<Tuple> iterator = pigServer.executeAndReturn(scriptType, context);
            while (iterator.hasNext()) {
                out.write(iterator.next().toString());
                out.newLine();
            }
        } finally {
            if (pigServer != null) {
                pigServer.close();
            }
        }
    }

    private void sendReport(File reportFile, String date) throws IOException {
        MailService.Builder builder = new MailService.Builder();
        builder.attach(reportFile);
        builder.setSubject(configurator.getString(MAIL_SUBJECT).replace("[date]", date));
        builder.setText(configurator.getString(MAIL_TEXT));
        builder.setTo(configurator.getString(MAIL_TO));
        MailService mailService = builder.build();

        mailService.send();
    }
}
