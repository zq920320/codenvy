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
package com.codenvy.analytics.services.metrics.integrity;

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricNotFoundException;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedMetric;

/**
 * @author Alexander Reshetnyak
 */
public class DataIntegrityFactory {

    private final static String CREATED_USERS =
            ((ReadBasedMetric)MetricFactory.getMetric(MetricType.CREATED_USERS)).getStorageCollectionName();

    private final static String USERS_STATISTICS =
            ((ReadBasedMetric)MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST)).getStorageCollectionName();

    public static CollectionDataIntegrity getIntegrity(String name) {
        if (CREATED_USERS.equalsIgnoreCase(name)) {
            return Injector.getInstance(CreatedUsersIntegrity.class);
        } else {
            throw new MetricNotFoundException("There is no CollectionDataIntegrity with name " + name);
        }


    }
}
