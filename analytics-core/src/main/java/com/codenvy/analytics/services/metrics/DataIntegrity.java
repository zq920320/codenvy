/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.services.metrics;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.services.Feature;
import com.codenvy.analytics.services.metrics.integrity.CollectionDataIntegrity;
import com.codenvy.analytics.services.metrics.integrity.DataIntegrityFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * @author Alexander Reshetnyak
 */
@Singleton
public class DataIntegrity extends Feature {

    private static final Logger LOG         = LoggerFactory.getLogger(DataIntegrity.class);
    public static final  String COLLECTIONS = "analytics.data-integrity.collections";
    private final String[] collections;

    @Inject
    public DataIntegrity(Configurator configurator)
            throws IOException {
        this.collections = configurator.getArray(COLLECTIONS);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    protected void doExecute(Context context) throws IOException {
        for (String collectionName : collections) {
            LOG.info("DataIntegrity is started for " + collectionName);
            long start = System.currentTimeMillis();
            try {
                CollectionDataIntegrity dataIntegrity = DataIntegrityFactory.getIntegrity(collectionName);
                dataIntegrity.doCompute();
            } finally {
                LOG.info("DataIntegrity is finished in " + (System.currentTimeMillis() - start) / 1000 +
                         " sec. for " + collectionName);
            }
        }
    }
}
