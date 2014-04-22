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
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.projects.ProjectPaases;
import com.codenvy.analytics.metrics.projects.ProjectTypes;
import com.codenvy.analytics.persistent.CollectionsManagement;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.EventConfiguration;
import com.codenvy.analytics.pig.scripts.EventsHolder;
import com.codenvy.analytics.pig.scripts.Parameter;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.udf.EventValidation;
import com.codenvy.analytics.services.Feature;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

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
import java.util.regex.Pattern;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@Singleton
public class LogChecker extends Feature {

    private static final Logger LOG = LoggerFactory.getLogger(LogChecker.class);

    private static final String AVAILABLE    = "analytics.log-checker.available";
    private static final String MAIL_TEXT    = "analytics.log-checker.mail_text";
    private static final String MAIL_SUBJECT = "analytics.log-checker.mail_subject";
    private static final String MAIL_TO      = "analytics.log-checker.mail_to";

    private final Configurator          configurator;
    private final EventsHolder          eventsHolder;
    private final CollectionsManagement collectionsManagement;

    @Inject
    public LogChecker(Configurator configurator, EventsHolder eventsHolder, CollectionsManagement collectionsManagement) {
        this.configurator = configurator;
        this.eventsHolder = eventsHolder;
        this.collectionsManagement = collectionsManagement;
    }

    @Override
    public boolean isAvailable() {
        return configurator.getBoolean(AVAILABLE);
    }

    @Override
    protected void doExecute(Context context) throws IOException, ParseException {
        LOG.info("LogChecker is started");
        long start = System.currentTimeMillis();

        try {
            File reportFile = getReport(context);

            Calendar toDate = context.getAsDate(Parameters.TO_DATE);
            String date = new SimpleDateFormat("yyyy-MM-dd").format(toDate.getTime());

            sendReport(reportFile, date);
        } finally {
            LOG.info("LogChecker is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    protected File getReport(Context context) throws IOException, ParseException {
        File reportFile = new File(configurator.getTmpDir(), "report.txt");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(reportFile))) {
            doLogChecker(context, out);
            doEventChecker(context, out);
        }

        return reportFile;
    }

    public void doEventChecker(Context context, BufferedWriter out) throws IOException, ParseException {
        DBCollection collection = collectionsManagement.getOrCreate(MetricType.USERS_ACTIVITY_LIST.toString().toLowerCase());

        for (EventConfiguration eventConf : eventsHolder.getAvailableEvents()) {
            String event = eventConf.getName();
            if (!isEventExist(collection, context, event)) {
                out.write("Event doesn't exist: " + event);
                out.newLine();

                continue;
            }

            for (Parameter param : eventConf.getParameters().getParams()) {
                String name = param.getName();

                if (param.getAllowedValues() != null) {
                    doCheckEventWithParameters(collection, context, event, name, param.getAllowedValues().split(","), out);
                } else if (name.equals(EventValidation.PAAS)) {
                    doCheckEventWithParameters(collection, context, event, name, ProjectPaases.PAASES, out);
                } else if (name.equals(EventValidation.TYPE)) {
                    doCheckEventWithParameters(collection, context, event, name, ProjectTypes.TYPES, out);
                }
            }
        }
    }

    private void doCheckEventWithParameters(DBCollection collection,
                                            Context context,
                                            String event,
                                            String param,
                                            String[] values,
                                            BufferedWriter out) throws IOException, ParseException {
        for (String value : values) {
            if (!isEventExist(collection, context, event, param, value)) {
                out.write(String.format("Event '%s' with parameter '%s' and value '%s' doesn't exist", event, param, value));
                out.newLine();
            }
        }
    }

    private boolean isEventExist(DBCollection collection, Context context, String event) throws ParseException {
        DBObject dbObject = Utils.setDateFilter(context);
        dbObject.put(AbstractMetric.EVENT, event);

        return collection.findOne(dbObject) != null;
    }

    private boolean isEventExist(DBCollection collection, Context context, String event, String param, String value) throws ParseException {
        DBObject dbObject = Utils.setDateFilter(context);
        dbObject.put(AbstractMetric.EVENT, event);
        dbObject.put(param.toLowerCase(), Pattern.compile(value, Pattern.CASE_INSENSITIVE));

        return collection.findOne(dbObject) != null;
    }

    protected void doLogChecker(Context context, BufferedWriter out) throws IOException, ParseException {
        PigServer pigServer = Injector.getInstance(PigServer.class);
        try {
            Iterator<Tuple> iterator = pigServer.executeAndReturn(ScriptType.LOG_CHECKER, context);
            while (iterator.hasNext()) {
                out.write(iterator.next().toString());
                out.newLine();
            }
        } finally {
            if (pigServer != null) {
                pigServer.shutdown();
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
