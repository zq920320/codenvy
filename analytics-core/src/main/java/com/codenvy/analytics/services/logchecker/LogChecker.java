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
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.ide_usage.AbstractIdeUsage;
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

    private final Configurator configurator;
    private final EventsHolder eventsHolder;
    private final DBCollection collection;

    @Inject
    public LogChecker(Configurator configurator, EventsHolder eventsHolder, CollectionsManagement collectionsManagement) {
        this.configurator = configurator;
        this.eventsHolder = eventsHolder;
        this.collection = collectionsManagement.getOrCreate(MetricType.USERS_ACTIVITY_LIST.toString().toLowerCase());
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
        for (EventConfiguration eventConf : eventsHolder.getAvailableEvents()) {
            String event = eventConf.getName();
            if (!isEventExist(context, event)) {
                writeLine("Event doesn't exist: " + event, out);
                continue;
            } else if (event.equals("ide-usage")) {
                if (!isEventExist(context, event, AbstractMetric.ACTION, AbstractIdeUsage.AUTOCOMPLETING)) {
                    writeLine("Event 'ide-usage' doesn't exist for action: " + AbstractIdeUsage.AUTOCOMPLETING, out);
                }
            }

            for (Parameter param : eventConf.getParameters().getParams()) {
                String name = param.getName();

                if (param.getAllowedValues() != null) {
                    doCheckEventWithParameters(context, event, name, param.getAllowedValues().split(","), out);

                } else if (name.equals(EventValidation.PAAS)) {
                    doCheckEventWithParameters(context, event, name, ProjectPaases.PAASES, out);

                } else if (name.equals(EventValidation.TYPE)) {
                    doCheckEventWithParameters(context, event, name, ProjectTypes.TYPES, out);

                } else if (name.equalsIgnoreCase(AbstractMetric.WS)) {
                    if (!isEventExist(context, event, AbstractMetric.WS, ReadBasedMetric.PERSISTENT_WS)) {
                        writeLine("Event doesn't exist for persistent workspaces: " + event, out);
                    }

                    if (!isEventExist(context, event, AbstractMetric.WS, ReadBasedMetric.TEMPORARY_WS)) {
                        writeLine("Event doesn't exist for temporary workspaces: " + event, out);
                    }

                } else if (name.equalsIgnoreCase(AbstractMetric.USER)) {
                    if (!isEventExist(context, event, AbstractMetric.USER, ReadBasedMetric.REGISTERED_USER)) {
                        writeLine("Event doesn't exist for registered users: " + event, out);
                    }

                    if (!isEventExist(context, event, AbstractMetric.USER, ReadBasedMetric.ANONYMOUS_USER)) {
                        writeLine("Event doesn't exist for anonymous users: " + event, out);
                    }
                }
            }
        }
    }


    private void doCheckEventWithParameters(Context context,
                                            String event,
                                            String param,
                                            String[] values,
                                            BufferedWriter out) throws IOException, ParseException {
        for (String value : values) {
            if (!isEventExist(context, event, param, value)) {
                writeLine(String.format("Event '%s' with parameter '%s' and value '%s' doesn't exist", event, param, value), out);
            }
        }
    }

    private boolean isEventExist(Context context, String event) throws ParseException {
        DBObject dbObject = Utils.setDateFilter(context);
        dbObject.put(AbstractMetric.EVENT, event);

        return collection.findOne(dbObject) != null;
    }

    private boolean isEventExist(Context context,
                                 String event,
                                 String param,
                                 Object value) throws ParseException {
        DBObject dbObject = Utils.setDateFilter(context);
        dbObject.put(AbstractMetric.EVENT, event);

        if (value instanceof String) {
            dbObject.put(param.toLowerCase(), Pattern.compile((String)value, Pattern.CASE_INSENSITIVE));
        } else {
            dbObject.put(param.toLowerCase(), value);
        }

        return collection.findOne(dbObject) != null;
    }

    private void writeLine(String line, BufferedWriter out) throws IOException {
        out.write(line);
        out.newLine();
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
