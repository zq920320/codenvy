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
package com.codenvy.analytics.services.view;

import java.util.Map;

/**
 * @author Alexander Reshetnyak
 */
public class CVSDateRow extends DateRow {

    private static final String CVS_DAY_FORMAT           = "dd MMM yyyy";
    private static final String CVS_WEEK_FORMAT          = "dd MMM yyyy";


    public CVSDateRow(Map<String, String> parameters) {
        super(parameters);
    }

    @Override
    public String getDayFormat() {
        return CVS_DAY_FORMAT;
    }

    @Override
    public String getWeekFormat() {
        return CVS_WEEK_FORMAT;
    }
}
