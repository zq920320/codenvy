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
import com.codenvy.analytics.services.configuration.ParameterConfiguration;
import com.codenvy.analytics.services.configuration.ParametersConfiguration;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.inject.Singleton;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@Singleton
public class RecipientsHolder {

    private static final Logger LOG           = LoggerFactory.getLogger(RecipientsHolder.class);
    private static final String CONFIGURATION = "reports.recipients.configuration";

    private final Map<String, Set<String>> emails;

    public RecipientsHolder(XmlConfigurationManager<RecipientsHolderConfiguration> configurationManager) {
        try {
            RecipientsHolderConfiguration configuration = configurationManager.loadConfiguration();
            this.emails = extractEmails(configuration.getGroups());
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | InstantiationException | IOException e) {
            LOG.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    public RecipientsHolder() {
        this(new XmlConfigurationManager<>(RecipientsHolderConfiguration.class,
                                           Configurator.getString(CONFIGURATION)));
    }

    public Set<String> getEmails(String groupName) {
        return emails.containsKey(groupName) ? emails.get(groupName)
                                             : Collections.<String>emptySet();
    }

    protected Map<String, Set<String>> extractEmails(List<GroupConfiguration> groups) throws ClassNotFoundException,
                                                                                             NoSuchMethodException,
                                                                                             IllegalAccessException,
                                                                                             InvocationTargetException,
                                                                                             InstantiationException,
                                                                                             IOException {
        Map<String, Set<String>> results = new HashMap<>();

        for (GroupConfiguration groupConfiguration : groups) {
            InitializerConfiguration initializer = groupConfiguration.getInitializer();

            String clazzName = initializer.getClazz();
            ParametersConfiguration parametersConfiguration = initializer.getParametersConfiguration();
            List<ParameterConfiguration> parameters = parametersConfiguration.getParameters();

            Class<?> clazz = Class.forName(clazzName);
            Constructor<?> constructor = clazz.getConstructor(List.class);

            RecipientGroup recipientGroup = (RecipientGroup)constructor.newInstance(parameters);
            results.put(groupConfiguration.getName(), recipientGroup.getEmails());
        }

        return results;
    }
}
