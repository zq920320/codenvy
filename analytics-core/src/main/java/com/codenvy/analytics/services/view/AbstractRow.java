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

import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractRow implements Row {

    protected static final String COLUMNS = "columns";

    protected final Map<String, String> parameters;

    protected AbstractRow(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the count of the columns in the row, taking into account the possibility of overriding the global
     *         parameter by specific row
     */
    protected int getOverriddenColumnsCount(int columns) {
        String columnsParam = parameters.get(COLUMNS);
        if (columnsParam != null) {
            return Integer.parseInt(columnsParam);

        } else {
            return columns;
        }
    }
}
