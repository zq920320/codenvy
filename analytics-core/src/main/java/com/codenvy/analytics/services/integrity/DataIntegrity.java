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
package com.codenvy.analytics.services.integrity;

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.services.Feature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Reshetnyak
 */
@Singleton
public class DataIntegrity extends Feature {

    private static final Logger LOG = LoggerFactory.getLogger(DataIntegrity.class);

    private static final List<CollectionDataIntegrity> DATA_INTEGRITY = new ArrayList<CollectionDataIntegrity>() {{
        Injector.getInstance(TasksIntegrity.class);
    }};

    @Inject
    public DataIntegrity() throws IOException {
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAvailable() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(Context context) {
        for (CollectionDataIntegrity integrity : DATA_INTEGRITY) {
            try {
                integrity.doCompute(context);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
