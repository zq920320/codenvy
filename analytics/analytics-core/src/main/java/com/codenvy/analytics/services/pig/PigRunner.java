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

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.persistent.CollectionsManagement;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.services.Feature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.text.ParseException;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
@Singleton
public class PigRunner extends Feature {

    private static final Logger LOG = LoggerFactory.getLogger(PigRunner.class);

    private final ScriptsManager        scriptsManager;
    private final CollectionsManagement collectionsManagement;

    @Inject
    public PigRunner(CollectionsManagement collectionsManagement, ScriptsManager scriptsManager) throws IOException {
        this.scriptsManager = scriptsManager;
        this.collectionsManagement = collectionsManagement;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    protected void doExecute(Context context) throws IOException, ParseException {
        LOG.info("PigRunner is started");
        long start = System.currentTimeMillis();

        PigServer pigServer = Injector.getInstance(PigServer.class);
        try {
            collectionsManagement.removeData(context);
            collectionsManagement.removeAnonymousUsers(context, 3);
            collectionsManagement.removeTemporaryWorkspaces(context, 3);

            for (ScriptConfiguration scriptConfiguration : scriptsManager.getAllScripts()) {
                String scriptName = scriptConfiguration.getName();
                ScriptType scriptType = ScriptType.valueOf(scriptName.toUpperCase());

                Context.Builder builder = new Context.Builder(context);
                builder.putAll(scriptConfiguration.getParamsAsMap());

                pigServer.execute(scriptType, builder.build());
            }

        } finally {
            LOG.info("PigRunner is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
            if (pigServer != null) {
                pigServer.shutdown();
            }
        }
    }
}
