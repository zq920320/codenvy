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
package com.codenvy.analytics.services.reports;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.services.configuration.ParameterConfiguration;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * All emails are placed into configuration under parameters with 'e-mail' key.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ItemizedRecipientGroupPeriod extends ItemizedRecipientGroup {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String START_DATE  = "start-date";
    private static final String END_DATE    = "end-date";

    public ItemizedRecipientGroupPeriod(List<ParameterConfiguration> parameters) {
        super(parameters);
    }

    @Override
    public Set<String> getEmails(Context context) throws IOException {
        if (inPeriod(context)) {
            return super.getEmails(context);
        } else {
            return Collections.emptySet();
        }
    }

    private boolean inPeriod(Context context) throws IOException {
        try {
            Calendar date = context.getAsDate(Parameters.TO_DATE);

            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            Date startDate = df.parse(getFirstParameter(START_DATE));
            Date endDate = df.parse(getFirstParameter(END_DATE));

            return endDate.getTime() >= date.getTimeInMillis() && date.getTimeInMillis() >= startDate.getTime();
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}
