/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.services.pig;

import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class ScriptsManager {

    private static final String CONFIGURATION = "scripts.xml";
    private final Map<ScriptKey, ScriptConfiguration> scripts;

    @Inject
    public ScriptsManager(XmlConfigurationManager confManager) throws IOException {
        PigRunnerConfiguration configuration = confManager.loadConfiguration(PigRunnerConfiguration.class, CONFIGURATION);

        this.scripts = new LinkedHashMap<>();
        for (ScriptConfiguration scriptConf : configuration.getScripts()) {
            ScriptType scriptType = ScriptType.valueOf(scriptConf.getName().toUpperCase());
            String storageTable = scriptConf.getParamsAsMap().get(Parameters.STORAGE_TABLE.toString());

            ScriptConfiguration prevConf = scripts.put(new ScriptKey(scriptType, storageTable), scriptConf);

            if (prevConf != null) {
                throw new IllegalStateException("Script duplicated in the configuration " + CONFIGURATION);
            }
        }
    }

    public Collection<ScriptConfiguration> getAllScripts() {
        return scripts.values();
    }

    public ScriptConfiguration getScript(ScriptType scriptType, String collection) {
        return scripts.get(new ScriptKey(scriptType, collection));
    }

    public ScriptConfiguration getScript(ScriptType scriptType, MetricType collectionForMetric) {
        if (MetricFactory.exists(collectionForMetric)) {
            ReadBasedMetric metric = (ReadBasedMetric)MetricFactory.getMetric(collectionForMetric);
            return getScript(scriptType, metric.getStorageCollectionName());
        } else {
            return getScript(scriptType, collectionForMetric.toString().toLowerCase());
        }
    }

    /**
     * Internal Key class.
     */
    private static class ScriptKey {
        private final ScriptType scriptType;
        private final String     storageTable;

        private ScriptKey(ScriptType scriptType, String storageTable) {
            this.scriptType = scriptType;
            this.storageTable = storageTable;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ScriptKey scriptKey = (ScriptKey)o;

            if (scriptType != scriptKey.scriptType) return false;
            if (storageTable != null ? !storageTable.equals(scriptKey.storageTable) : scriptKey.storageTable != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = scriptType != null ? scriptType.hashCode() : 0;
            result = 31 * result + (storageTable != null ? storageTable.hashCode() : 0);
            return result;
        }
    }
}
