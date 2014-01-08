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
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.services.configuration.ConfigurationManagerException;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;
import com.codenvy.analytics.services.reports.*;
import com.codenvy.analytics.services.view.CSVReportPersister;
import com.codenvy.analytics.services.view.ViewBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ReportSender extends Feature {

    private static final Logger LOG = LoggerFactory.getLogger(ReportSender.class);

    private static final String AVAILABLE     = "reports.available";
    private static final String CONFIGURATION = "reports.configuration";
    private static final String MAIL_TEXT     = "reports.mail.text";
    private static final String MAIL_SUBJECT  = "reports.mail.subject";

    private final RecipientsHolder recipientsHolder;
    private final ViewBuilder      viewBuilder;

    public ReportSender() {
        this.recipientsHolder = new RecipientsHolder();
        this.viewBuilder = new ViewBuilder();
    }

    @Override
    protected Map<String, String> initializeDefaultContext() throws ParseException {
        return Utils.initializeContext(Parameters.TimeUnit.DAY);
    }

    @Override
    protected void putParametersInContext(Map<String, String> context) {
    }

    @Override
    public boolean isAvailable() {
        return Configurator.getBoolean(AVAILABLE);
    }

    protected void doExecute(Map<String, String> context) throws IOException,
                                                                 ParseException,
                                                                 ClassNotFoundException,
                                                                 NoSuchMethodException,
                                                                 InstantiationException,
                                                                 IllegalAccessException,
                                                                 InvocationTargetException {
        LOG.info("ReportSender is started");
        long start = System.currentTimeMillis();

        try {
            ReportsConfiguration configuration = readConfiguration();

            for (ReportConfiguration reportConfiguration : configuration.getReports()) {
                for (FrequencyConfiguration frequencyConfiguration : reportConfiguration.getFrequencies()) {
                    for (AbstractFrequencyConfiguration frequency : frequencyConfiguration.frequencies()) {
                        if (frequency != null && isAppropriateDayToSendReport(frequency, context)) {
                            sendReport(context,
                                       frequency,
                                       reportConfiguration.getRecipients());
                        }
                    }
                }
            }
        } finally {
            LOG.info("ReportSender is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    protected void sendReport(Map<String, String> context,
                              AbstractFrequencyConfiguration frequency,
                              RecipientsConfiguration recipients) throws IOException,
                                                                         ParseException,
                                                                         ClassNotFoundException,
                                                                         NoSuchMethodException,
                                                                         InvocationTargetException,
                                                                         InstantiationException,
                                                                         IllegalAccessException {

        String subject = Configurator.getString(MAIL_SUBJECT).replace("[period]", getPeriod(frequency));

        for (String recipient : recipients.getRecipients()) {
            for (String email : recipientsHolder.getEmails(recipient)) {
                MailService.Builder builder = new MailService.Builder();
                builder.setText(Configurator.getString(MAIL_TEXT));
                builder.setSubject(subject);
                builder.setTo(email);

                List<File> reports = getReports(context, email, frequency);
                for (File report : reports) {
                    builder.attach(report);
                }

                MailService mailService = builder.build();
                mailService.send();

                remove(reports);
            }
        }
    }

    private List<File> getReports(Map<String, String> context,
                                  String recipient,
                                  AbstractFrequencyConfiguration frequency) throws IOException,
                                                                                   ParseException,
                                                                                   ClassNotFoundException,
                                                                                   NoSuchMethodException,
                                                                                   IllegalAccessException,
                                                                                   InvocationTargetException,
                                                                                   InstantiationException {
        context = updateContext(context, recipient, frequency);

        ViewsConfiguration viewsConfiguration = frequency.getViews();
        List<File> reports = new ArrayList<>(viewsConfiguration.getViews().size());

        for (String view : viewsConfiguration.getViews()) {
            Map<String, List<List<ValueData>>> viewData = viewBuilder.getViewData(view, context);

            File report = new File(Configurator.getTmpDir(), view + ".csv");
            CSVReportPersister.storeData(report, viewData);

            reports.add(report);
        }

        return reports;
    }

    private Map<String, String> updateContext(Map<String, String> context,
                                              String recipient,
                                              AbstractFrequencyConfiguration frequency) throws ClassNotFoundException,
                                                                                               InstantiationException,
                                                                                               IllegalAccessException,
                                                                                               InvocationTargetException,
                                                                                               NoSuchMethodException {
        String clazzName = frequency.getContextModifier().getClazz();
        Class<?> clazz = Class.forName(clazzName);
        ContextModifier contextModifier = (ContextModifier)clazz.getConstructor().newInstance();

        return contextModifier.update(context);
    }

    private void remove(List<File> reports) {
        for (File report : reports) {
            if (!report.delete()) {
                LOG.warn("File can't be deleted " + report.getPath());
            }
        }
    }

    private String getPeriod(AbstractFrequencyConfiguration frequency) {
        if (frequency instanceof DailyFrequencyConfiguration) {
            return "Daily";

        } else if (frequency instanceof WeeklyFrequencyConfiguration) {
            return "Weekly";

        } else if (frequency instanceof MonthlyFrequencyConfiguration) {
            return "Monthly";

        }

        return "";
    }

    protected boolean isAppropriateDayToSendReport(AbstractFrequencyConfiguration frequencyConfiguration,
                                                   Map<String, String> context) throws ParseException {
        Calendar toDate = Utils.getToDate(context);

        if (frequencyConfiguration instanceof DailyFrequencyConfiguration) {
            return true;

        } else if (frequencyConfiguration instanceof WeeklyFrequencyConfiguration) {
            return toDate.get(Calendar.DAY_OF_WEEK) == toDate.getActualMinimum(Calendar.DAY_OF_WEEK);

        } else if (frequencyConfiguration instanceof MonthlyFrequencyConfiguration) {
            return toDate.get(Calendar.DAY_OF_MONTH) == toDate.getActualMinimum(Calendar.DAY_OF_MONTH);

        }

        return false;
    }

    private ReportsConfiguration readConfiguration() throws ConfigurationManagerException {
        XmlConfigurationManager<ReportsConfiguration> configurationManager =
                new XmlConfigurationManager<>(ReportsConfiguration.class, Configurator.getString(CONFIGURATION));

        return configurationManager.loadConfiguration();
    }
}
