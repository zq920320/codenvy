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
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.metrics.AbstractSetValueResulted;
import com.codenvy.analytics.metrics.InternalMetric;
import com.codenvy.analytics.metrics.MetricType;

/**
 * @author Alexander Reshetnyak
 */
@InternalMetric
public class SignupValidationEmailSendSet extends AbstractSetValueResulted {
    public SignupValidationEmailSendSet() {
        super(MetricType.SIGNUP_VALIDATION_EMAIL_SEND_SET, EMAIL);
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.EMAIL_VALIDATION_SEND);
    }

    @Override
    public String getDescription() {
        return "Set of users which send confirm singup validation email";
    }
}
