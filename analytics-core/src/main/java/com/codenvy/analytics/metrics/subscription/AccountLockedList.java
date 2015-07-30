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
package com.codenvy.analytics.metrics.subscription;

import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.MetricType;

/**
 * @author Alexander Reshetnyak
 */
public class AccountLockedList extends AbstractListValueResulted {

    public AccountLockedList() {
        super(MetricType.ACCOUNT_LOCKED_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Users' locked accounts.";
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.ACCOUNT_LOCKED);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{ACCOUNT,
                            DATE};
    }
}