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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.users.UsersAccountsOwnerList;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.codenvy.analytics.pig.udf.UUIDFrom;

import org.eclipse.che.commons.lang.NameGenerator;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.metrics.MetricFactory.getMetric;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Reshetnyak
 */
public class TestUsersAccounts extends BaseTest {

    protected static final String ACID1 = NameGenerator.generate("account_1_", org.eclipse.che.api.user.server.Constants.ID_LENGTH - 3);
    protected static final String ACID2 = NameGenerator.generate("account_2_", org.eclipse.che.api.user.server.Constants.ID_LENGTH - 3);
    protected static final String ACID3 = NameGenerator.generate("account_3_", org.eclipse.che.api.user.server.Constants.ID_LENGTH - 3);
    protected static final String ACID4 = NameGenerator.generate("account_4_", org.eclipse.che.api.user.server.Constants.ID_LENGTH - 3);

    private File prepareLog() throws IOException {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCreatedEvent(UID1, "u1", "u1@u.com").withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createUserCreatedEvent(UID2, "u2", "u2@u.com").withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createUserCreatedEvent(UID3, "u3", "u3@u.com").withDate("2013-01-01", "10:00:00").build());

        events.add(Event.Builder.createAccountAddMemberEvent(ACID1, UID1, Arrays.asList("account/owner")).withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent(ACID1, UID1, Arrays.asList("account/member")).withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent(ACID2, UID2, Arrays.asList("account/owner")).withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent(ACID2, UID2, Arrays.asList("account/member")).withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent(ACID3, UID3, Arrays.asList("account/owner")).withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent(ACID3, UID3, Arrays.asList("account/member")).withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent(ACID4, UID1, Arrays.asList("account/owner")).withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent(ACID4, UID1, Arrays.asList("account/member")).withDate("2013-01-01", "10:00:00").build());

        events.add(Event.Builder.createAccountAddMemberEvent(ACID2, UID1, Arrays.asList("account/member")).withDate("2013-01-02", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent(ACID1, UID2, Arrays.asList("account/member")).withDate("2013-01-02", "10:00:00").build());

        events.add(Event.Builder.createAccountRemoveMemberEvent(ACID2, UID1).withDate("2013-01-03", "10:00:00").build());

        return LogGenerator.generateLog(events);
    }

    public void computeData(String date) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, prepareLog().getPath());

        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_ACCOUNTS, MetricType.USERS_ACCOUNTS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_ACCOUNTS, builder.build());
    }

    @Test
    public void testUsersAccountsList() throws Exception {
        computeData("20130101");
        computeData("20130102");

        ListValueData l = getAsList(getMetric(MetricType.USERS_ACCOUNTS_LIST), Context.EMPTY);
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.ID);

        assertEquals(m.size(), 6);
        assertTrue(m.containsKey(UUIDFrom.exec(ACID1 + UID1)));
        Map<String, ValueData> vdm = m.get(UUIDFrom.exec(ACID1 + UID1));
        assertEquals(vdm.get(AbstractMetric.ID).getAsString(), UUIDFrom.exec(ACID1 + UID1));
        assertEquals(vdm.get(AbstractMetric.USER).getAsString(), UID1);
        assertEquals(vdm.get(AbstractMetric.ACCOUNT).getAsString(), ACID1);
        assertEquals(vdm.get(AbstractMetric.ROLES).getAsString(), "[account/member, account/owner]");
        assertTrue(m.containsKey(UUIDFrom.exec(ACID2 + UID2)));
        vdm = m.get(UUIDFrom.exec(ACID2 + UID2));
        assertEquals(vdm.get(AbstractMetric.ID).getAsString(), UUIDFrom.exec(ACID2 + UID2));
        assertEquals(vdm.get(AbstractMetric.USER).getAsString(), UID2);
        assertEquals(vdm.get(AbstractMetric.ACCOUNT).getAsString(), ACID2);
        assertEquals(vdm.get(AbstractMetric.ROLES).getAsString(), "[account/member, account/owner]");
        assertTrue(m.containsKey(UUIDFrom.exec(ACID3 + UID3)));
        vdm = m.get(UUIDFrom.exec(ACID3 + UID3));
        assertEquals(vdm.get(AbstractMetric.ID).getAsString(), UUIDFrom.exec(ACID3 + UID3));
        assertEquals(vdm.get(AbstractMetric.USER).getAsString(), UID3);
        assertEquals(vdm.get(AbstractMetric.ACCOUNT).getAsString(), ACID3);
        assertEquals(vdm.get(AbstractMetric.ROLES).getAsString(), "[account/member, account/owner]");
        assertTrue(m.containsKey(UUIDFrom.exec(ACID4 + UID1)));
        vdm = m.get(UUIDFrom.exec(ACID4 + UID1));
        assertEquals(vdm.get(AbstractMetric.ID).getAsString(), UUIDFrom.exec(ACID4 + UID1));
        assertEquals(vdm.get(AbstractMetric.USER).getAsString(), UID1);
        assertEquals(vdm.get(AbstractMetric.ACCOUNT).getAsString(), ACID4);
        assertEquals(vdm.get(AbstractMetric.ROLES).getAsString(), "[account/member, account/owner]");
        assertTrue(m.containsKey(UUIDFrom.exec(ACID1 + UID2)));
        vdm = m.get(UUIDFrom.exec(ACID1 + UID2));
        assertEquals(vdm.get(AbstractMetric.ID).getAsString(), UUIDFrom.exec(ACID1 + UID2));
        assertEquals(vdm.get(AbstractMetric.USER).getAsString(), UID2);
        assertEquals(vdm.get(AbstractMetric.ACCOUNT).getAsString(), ACID1);
        assertEquals(vdm.get(AbstractMetric.ROLES).getAsString(), "[account/member]");
        assertTrue(m.containsKey(UUIDFrom.exec(ACID2 + UID1)));
        vdm = m.get(UUIDFrom.exec(ACID2 + UID1));
        assertEquals(vdm.get(AbstractMetric.ID).getAsString(), UUIDFrom.exec(ACID2 + UID1));
        assertEquals(vdm.get(AbstractMetric.USER).getAsString(), UID1);
        assertEquals(vdm.get(AbstractMetric.ACCOUNT).getAsString(), ACID2);
        assertEquals(vdm.get(AbstractMetric.ROLES).getAsString(), "[account/member]");


        computeData("20130103");

        l = getAsList(getMetric(MetricType.USERS_ACCOUNTS_LIST), Context.EMPTY);
        m = listToMap(l, AbstractMetric.ID);

        assertEquals(m.size(), 6);
        assertTrue(m.containsKey(UUIDFrom.exec(ACID2 + UID1)));
        vdm = m.get(UUIDFrom.exec(ACID2 + UID1));
        assertEquals(vdm.get(AbstractMetric.ID).getAsString(), UUIDFrom.exec(ACID2 + UID1));
        assertEquals(vdm.get(AbstractMetric.USER).getAsString(), UID1);
        assertEquals(vdm.get(AbstractMetric.ACCOUNT).getAsString(), ACID2);
        assertEquals(vdm.get(AbstractMetric.ROLES).getAsString(), "[account/member]");
        assertEquals(vdm.get(AbstractMetric.REMOVED).getAsString(), 1);
        assertEquals(vdm.get(AbstractMetric.REMOVED_DATE).getAsString(), "1357200000000");
    }

    @Test
    public void testAddMemberInAccount() throws Exception {
        computeData("20130101");
        computeData("20130102");

        ListValueData l = getAsList(getMetric(MetricType.USERS_ACCOUNTS_OWNER_LIST), Context.EMPTY);
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.USER);

        assertEquals(l.size(), 3);

        assertTrue(m.containsKey(UID1));
        Map<String, ValueData> vdm = m.get(UID1);
        assertEquals(vdm.get(AbstractMetric.USER).getAsString(), UID1);
        assertEquals(vdm.get(UsersAccountsOwnerList.ACCOUNTS).getAsString(), "[" + ACID4 + ", " + ACID1 + "]");

        assertTrue(m.containsKey(UID2));
        vdm = m.get(UID2);
        assertEquals(vdm.get(AbstractMetric.USER).getAsString(), UID2);
        assertEquals(vdm.get(UsersAccountsOwnerList.ACCOUNTS).getAsString(), "[" + ACID2 + "]");

        assertTrue(m.containsKey(UID3));
        vdm = m.get(UID3);
        assertEquals(vdm.get(AbstractMetric.USER).getAsString(), UID3);
        assertEquals(vdm.get(UsersAccountsOwnerList.ACCOUNTS).getAsString(), "[" + ACID3 + "]");
    }

    @Test
    public void testUsersOwnersAccountsListWithUserFilter() throws Exception {
        computeData("20130101");
        computeData("20130102");

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_ID, UID1);

        ListValueData l = getAsList(getMetric(MetricType.USERS_ACCOUNTS_OWNER_LIST), builder.build());
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.USER);

        assertEquals(l.size(), 1);

        assertTrue(m.containsKey(UID1));
        Map<String, ValueData> vdm = m.get(UID1);
        assertEquals(vdm.get(AbstractMetric.USER).getAsString(), UID1);
        assertEquals(vdm.get(UsersAccountsOwnerList.ACCOUNTS).getAsString(), "[" + ACID4 + ", " + ACID1 + "]");
    }

    /*@Test  ADD metric USERS_MEMBERS_ACCOUNTS_LIST
    public void testRemoveMemberFromAccount() throws Exception {
        computeData("20130101");
        computeData("20130102");
        computeData("20130103");

        ListValueData l = ValueDataUtil.getAsList(MetricFactory.getMetric(MetricType.USERS_ACCOUNTS_OWNER_LIST), Context.EMPTY);
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.USER);

        assertEquals(m.size(), 3);
    }*/
}