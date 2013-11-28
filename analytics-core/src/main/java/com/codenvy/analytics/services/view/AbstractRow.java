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


package com.codenvy.analytics.services.view;

import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractRow implements Row {

    private static final String DESCRIPTION = "description";

    protected final Map<String, String> parameters;

    protected AbstractRow(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getDescription() throws IOException {
        return parameters.containsKey(DESCRIPTION) ? new StringValueData(parameters.get(DESCRIPTION))
                                                   : StringValueData.DEFAULT;
    }
}
