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
package com.codenvy.analytics.services.reports;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.MailService;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.services.Feature;
import com.codenvy.analytics.services.configuration.ParametersConfiguration;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;
import com.codenvy.analytics.services.view.CSVReportPersister;
import com.codenvy.analytics.services.view.ViewBuilder;
import com.codenvy.analytics.services.view.ViewData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class ReportSender extends Feature {

    private static final Logger LOG = LoggerFactory.getLogger(ReportSender.class);

    private static final String AVAILABLE     = "analytics.reports.available";
    private static final String CONFIGURATION = "analytics.reports";
    private static final String MAIL_TEXT     = "analytics.reports.mail_text";
    private static final String MAIL_SUBJECT  = "analytics.reports.mail_subject";

    private final RecipientsHolder     recipientsHolder;
    private final ViewBuilder          viewBuilder;
    private final Configurator         configurator;
    private final CSVReportPersister   csvReportPersister;
    private final ReportsConfiguration configuration;

    @Inject
    public ReportSender(CSVReportPersister csvReportPersister,
                        Configurator configurator,
                        RecipientsHolder recipientsHolder,
                        ViewBuilder viewBuilder,
                        XmlConfigurationManager confManager) throws IOException {
        this.csvReportPersister = csvReportPersister;
        this.configurator = configurator;
        this.recipientsHolder = recipientsHolder;
        this.viewBuilder = viewBuilder;
        this.configuration =
                confManager.loadConfiguration(ReportsConfiguration.class, configurator.getString(CONFIGURATION));
    }

    @Override
    public boolean isAvailable() {
        return configurator.getBoolean(AVAILABLE);
    }

    protected void doExecute(Context context) throws IOException,
                                                     ParseException,
                                                     ClassNotFoundException,
                                                     NoSuchMethodException,
                                                     InstantiationException,
                                                     IllegalAccessException,
                                                     InvocationTargetException {
        LOG.info("ReportSender is started");
        long start = System.currentTimeMillis();

        context = context.cloneAndPut(Parameters.REPORT_DATE, context.getAsString(Parameters.TO_DATE));

        try {
            for (ReportConfiguration reportConfiguration : configuration.getReports()) {
                for (FrequencyConfiguration frequencyConfiguration : reportConfiguration.getFrequencies()) {
                    for (AbstractFrequencyConfiguration frequency : frequencyConfiguration.frequencies()) {
                        if (frequency.isAppropriateDateToSendReport(context)) {
                            Context newContext = frequency.initContext(context);
                            RecipientsConfiguration recipients = reportConfiguration.getRecipients();

                            sendReport(newContext,
                                       frequency,
                                       recipients);
                        }
                    }
                }
            }
        } finally {
            LOG.info("ReportSender is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    protected void sendReport(Context context,
                              AbstractFrequencyConfiguration frequency,
                              RecipientsConfiguration recipients) throws IOException,
                                                                         ParseException,
                                                                         ClassNotFoundException,
                                                                         NoSuchMethodException,
                                                                         InvocationTargetException,
                                                                         InstantiationException,
                                                                         IllegalAccessException {

        String subject = configurator.getString(MAIL_SUBJECT).replace("[period]", frequency.getTimeUnit().name());

        for (String recipient : recipients.getRecipients()) {
            for (String email : recipientsHolder.getEmails(recipient, context)) {
                MailService.Builder builder = new MailService.Builder();
                builder.setText(configurator.getString(MAIL_TEXT));
                builder.setSubject(subject);
                builder.setTo(email);

                List<File> reports = getReports(context, email, frequency);
                builder.attach(reports);
                builder.build().send();

                LOG.info("Reports " + reports.toString() + " were send to " + email);
            }
        }
    }

    private List<File> getReports(Context context,
                                  String recipient,
                                  AbstractFrequencyConfiguration frequency) throws IOException,
                                                                                   ParseException,
                                                                                   ClassNotFoundException,
                                                                                   NoSuchMethodException,
                                                                                   IllegalAccessException,
                                                                                   InvocationTargetException,
                                                                                   InstantiationException {

        List<File> reports = new ArrayList<>();
        ViewsConfiguration viewsConfiguration = frequency.getViews();

        for (String view : viewsConfiguration.getViews()) {
            context = context.cloneAndPut(Parameters.RECIPIENT, recipient);

            ContextModifier contextModifier = getContextModifier(frequency);
            context = contextModifier.update(context);

            ViewData viewData = viewBuilder.getViewData(view, context);
            String viewId = recipient + File.separator + view;

            File report = csvReportPersister.storeData(viewId, viewData, context);
            reports.add(report);
        }

        return reports;
    }

    protected ContextModifier getContextModifier(AbstractFrequencyConfiguration frequency)
            throws ClassNotFoundException,
                   NoSuchMethodException,
                   InstantiationException,
                   IllegalAccessException,
                   InvocationTargetException {

        ContextModifierConfiguration contextModifierConf = frequency.getContextModifier();
        ParametersConfiguration paramsConf = contextModifierConf.getParametersConfiguration();


        Class<?> clazz = Class.forName(contextModifierConf.getClazz());
        Constructor<?> constructor = clazz.getConstructor(List.class);

        return (ContextModifier)constructor.newInstance(paramsConf == null ? Collections.emptyList() : paramsConf.getParameters());
    }
}
