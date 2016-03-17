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
package com.codenvy.analytics.services.marketo;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.services.view.CSVFileHolder;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Alexander Reshetnyak
 */
@Singleton
public class MarketoUpdater extends MarketoInitializer {

    private static final String AVAILABLE = "analytics.marketo.updater_available";

    @Inject
    public MarketoUpdater(Configurator configurator,
                          MarketoReportGenerator reportGenerator,
                          CSVFileHolder csvFileHolder) {
        super(configurator, reportGenerator, csvFileHolder);
    }

    @Override
    public boolean isAvailable() {
        return configurator.getBoolean(AVAILABLE);
    }

    @Override
    protected boolean processActiveUsersOnly() {
        return true;
    }

    @Override
    protected boolean cleanListBeforeImport() {
        return false;
    }

    protected Context prepareActiveUsersContext(Context context) {
        Context.Builder builder = new Context.Builder(context.getAll());
        builder.put(Parameters.FROM_DATE, context.getAsString(Parameters.TO_DATE));
        return builder.build();
    }
}
