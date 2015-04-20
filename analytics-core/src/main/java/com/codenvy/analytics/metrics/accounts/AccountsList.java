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
package com.codenvy.analytics.metrics.accounts;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

/**
 * @author Anatoliy Bazko
 */
@RolesAllowed(value = {"system/admin", "system/manager"})
public class AccountsList extends AbstractAccountMetric {

    public AccountsList() {
        super(MetricType.ACCOUNTS_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "List of all accounts";
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Context context) throws IOException {
        // TODO [AB] stub
        return ListValueData.DEFAULT;
    }


    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}