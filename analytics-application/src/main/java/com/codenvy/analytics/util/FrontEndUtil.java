/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.util;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.users.UsersProfilesList;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/** @author Dmytro Nochevnov */
public class FrontEndUtil {

    /**
     * Read name of logged user from userPrincipal and read his/him first and last names by using metric
     * "users_profiles_list" filtered by logged user name.
     *
     * @return "{user_first_name} {user_last_name}" String,
     * <br>- or "{logged user name}" if metric "users_profiles_list" is returning empty user_first_name and
     * user_last_name attributes,
     * <br>- or "{logged user name}" if there was impossible to read data from metric (exception happened in
     * time of reading data by metric).
     */
    public static String getFirstAndLastName(Principal userPrincipal) {
        String firstAndLastName;
        String email = null;
        if (userPrincipal != null) {
            email = userPrincipal.getName();
        }

        HashMap<String, String> metricContext = new HashMap<>();

        if (email != null) {
            metricContext.put(Parameters.USER.toString(), email);
        }

        ListValueData value;
        try {
            value = (ListValueData)MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST).getValue(metricContext);
        } catch (IOException e) {
            // return user email if there was impossible to read data from metric
            return email;
        }

        String firstName = "";
        String lastName = "";

        if (value.size() > 0) {
            Map<String, ValueData> userProfile = ((MapValueData)value.getAll().get(0)).getAll();

            firstName = userProfile.get(UsersProfilesList.USER_FIRST_NAME).toString();
            lastName = userProfile.get(UsersProfilesList.USER_LAST_NAME).toString();
        }

        // return user email if there are empty both his/her first name and last name
        if (firstName.isEmpty() && lastName.isEmpty()) {
            firstAndLastName = email;

            // return "{user_first_name} {user_last_name}" String
        } else {
            firstAndLastName = firstName + " " + lastName;
        }

        return firstAndLastName;
    }
}