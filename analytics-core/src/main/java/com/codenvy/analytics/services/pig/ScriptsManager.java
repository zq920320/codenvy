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
package com.codenvy.analytics.services.pig;

import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class ScriptsManager {

    private static final String CONFIGURATION = "scripts.xml";

    private final PigRunnerConfiguration configuration;

    @Inject
    public ScriptsManager(XmlConfigurationManager confManager) throws IOException {
        this.configuration = confManager.loadConfiguration(PigRunnerConfiguration.class, CONFIGURATION);
    }

    public Collection<ScriptConfiguration> getAllScripts() {
        return configuration.getScripts();
    }

    public ScriptConfiguration getScript(ScriptType scriptType) {
        for (ScriptConfiguration scriptConfiguration : configuration.getScripts()) {
            if (scriptConfiguration.getName().equalsIgnoreCase(scriptType.toString())) {
                return scriptConfiguration;
            }
        }

        throw new IllegalArgumentException("Script " + scriptType + " not found in configuration");
    }
}
