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

package com.codenvy.analytics.integration;

import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;

import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsDouble;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsLong;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsSet;
import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 */
public class TestUsers extends BaseTest {

    public static final int CREATED_USERS = 3;
    public static final int REMOVED_USERS = 1;

    @Test
    public void testCreatedUsers() throws Exception {
        ValueData valueData = getValue(MetricType.CREATED_USERS);
        long l = treatAsLong(valueData);

        assertEquals(l, CREATED_USERS);
    }

    @Test
    public void testCreatedUsersSet() throws Exception {
        ValueData valueData = getValue(MetricType.CREATED_USERS_SET);
        Set<ValueData> s = treatAsSet(valueData);

        assertEquals(s.size(), CREATED_USERS);
    }

    @Test
    public void testCreatedUsersList() throws Exception {
        ValueData valueData = getValue(MetricType.CREATED_USERS_LIST);
        List<ValueData> l = treatAsList(valueData);

        assertEquals(l.size(), CREATED_USERS);
    }

    @Test
    public void testCreatedUsersFromAuth() throws Exception {
        ValueData valueData = getValue(MetricType.CREATED_USERS_FROM_AUTH);
        long l = treatAsLong(valueData);

        assertEquals(l, CREATED_USERS);
    }

    @Test
    public void testRemovedUsers() throws Exception {
        ValueData valueData = getValue(MetricType.REMOVED_USERS);
        long l = treatAsLong(valueData);

        assertEquals(l, REMOVED_USERS);
    }

    @Test
    public void testActiveUsers() throws Exception {
        ValueData valueData = getValue(MetricType.ACTIVE_USERS);
        long l = treatAsLong(valueData);

// TODO
//        assertEquals(l, CREATED_USERS);
    }

    @Test
    public void testActiveUsersFromBeginning() throws Exception {
        ValueData valueData = getValue(MetricType.ACTIVE_USERS_FROM_BEGINNING);
        long l = treatAsLong(valueData);

// TODO
    }

    @Test
    public void testActiveUsersSet() throws Exception {
        ValueData valueData = getValue(MetricType.ACTIVE_USERS_SET);
        Set<ValueData> s = treatAsSet(valueData);

        for (ValueData vd : s) {
            getUserNameById(vd.getAsString());
        }
    }

    @Test
    public void testNonActiveUsers() throws Exception {
        ValueData valueData = getValue(MetricType.NON_ACTIVE_USERS);
        long l = treatAsLong(valueData);

// TODO
    }

    @Test
    public void testReturningActiveUsers() throws Exception {
        ValueData valueData = getValue(MetricType.RETURNING_ACTIVE_USERS);
        long l = treatAsLong(valueData);

// TODO
    }

    @Test
    public void testNewActiveUsers() throws Exception {
        ValueData valueData = getValue(MetricType.NEW_ACTIVE_USERS);
        long l = treatAsLong(valueData);

        assertEquals(l, CREATED_USERS);
    }

    @Test
    public void testUsersLoggedInWithGitHub() throws Exception {
        ValueData valueData = getValue(MetricType.USERS_LOGGED_IN_WITH_GITHUB);
        long l = treatAsLong(valueData);

        assertEquals(l, 1);
    }

    @Test
    public void testUsersLoggedInWithGoogle() throws Exception {
        ValueData valueData = getValue(MetricType.USERS_LOGGED_IN_WITH_GOOGLE);
        long l = treatAsLong(valueData);

        assertEquals(l, 2);
    }

    @Test
    public void testUsersLoggedInWithForm() throws Exception {
        ValueData valueData = getValue(MetricType.USERS_LOGGED_IN_WITH_FORM);
        long l = treatAsLong(valueData);

        // iedexmain2@gmail.com -> org
        // iedexmain@gmail.com -> org
        // codenvysingle@gmail.com -> email
        assertEquals(l, 3);
    }

    @Test
    public void testUsersLoggedInWithSysLdap() throws Exception {
        ValueData valueData = getValue(MetricType.USERS_LOGGED_IN_WITH_SYSLDAP);
        long l = treatAsLong(valueData);

        assertEquals(l, 1);
    }

    @Test
    public void testUsersLoggedInTotal() throws Exception {
        ValueData valueData = getValue(MetricType.USERS_LOGGED_IN_TOTAL);
        long l = treatAsLong(valueData);

// TODO
    }

    @Test
    public void testUsersLoggedInWithGitHubPercent() throws Exception {
        ValueData valueData = getValue(MetricType.USERS_LOGGED_IN_WITH_GITHUB_PERCENT);
        double d = treatAsDouble(valueData);

// TODO
    }

    @Test
    public void testUsersLoggedInWithGooglePercent() throws Exception {
        ValueData valueData = getValue(MetricType.USERS_LOGGED_IN_WITH_GOOGLE_PERCENT);
        double d = treatAsDouble(valueData);

// TODO
    }

    @Test
    public void testUsersLoggedInWithFormPercent() throws Exception {
        ValueData valueData = getValue(MetricType.USERS_LOGGED_IN_WITH_FORM_PERCENT);
        double d = treatAsDouble(valueData);

// TODO
    }

    @Test
    public void testUsersLoggedInWithSysLdapPercent() throws Exception {
        ValueData valueData = getValue(MetricType.USERS_LOGGED_IN_WITH_SYSLDAP_PERCENT);
        double d = treatAsDouble(valueData);

// TODO
    }

    @Test
    public void testUsersLoggedInTypes() throws Exception {
        ValueData valueData = getValue(MetricType.USERS_LOGGED_IN_TYPES);
        long l = treatAsLong(valueData);

// TODO
    }

    @Test
    public void testUserInvite() throws Exception {
        ValueData valueData = getValue(MetricType.USER_INVITE);
        long l = treatAsLong(valueData);

// TODO
    }

    @Test
    public void testUsersAcceptedInvites() throws Exception {
        ValueData valueData = getValue(MetricType.USERS_ACCEPTED_INVITES);
        long l = treatAsLong(valueData);

// TODO
    }

    @Test
    public void testUsersAcceptedInvitesPercent() throws Exception {
        ValueData valueData = getValue(MetricType.USERS_ACCEPTED_INVITES_PERCENT);
        double d = treatAsDouble(valueData);

// TODO
    }

    @Test
    public void testUsersAddedToWorkspacesUsingInvitation() throws Exception {
        ValueData valueData = getValue(MetricType.USERS_ADDED_TO_WORKSPACES_USING_INVITATION);
        long l = treatAsLong(valueData);

// TODO
    }

    @Test
    public void testCompletedProfiles() throws Exception {
        ValueData valueData = getValue(MetricType.COMPLETED_PROFILES);
        long l = treatAsLong(valueData);

        assertEquals(l, CREATED_USERS);
    }

    @Test
    public void testTotalUsers() throws Exception {
        Context.Builder context = new Context.Builder();
        context.put(Parameters.TO_DATE, Parameters.TO_DATE.getDefaultValue());

        ValueData valueData = getValue(MetricType.TOTAL_USERS, context.build());
        long l = treatAsLong(valueData);

        assertEquals(l, CREATED_USERS - REMOVED_USERS);
    }
}
