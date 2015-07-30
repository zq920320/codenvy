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
package com.codenvy.analytics.services.marketo;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Reshetnyak
 */
public class TestMarketoReportGeneratorUtils extends BaseTest {

    @BeforeMethod
    public void clearDatabase() {
        super.clearDatabase();
    }

    private File prepareLog() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent("id1", "user1@gmail.com", "user1@gmail.com")
                                .withDate("2013-11-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("id2", "user2@gmail.com", "user2@gmail.com")
                                .withDate("2013-11-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("id3", "user3@gmail.com", "user3@gmail.com")
                                .withDate("2013-11-01").withTime("10:00:00,000").build());

        events.add(Event.Builder.createUserCreatedEvent("id5", "user5@gmail.com", "user5@gmail.com")
                                .withDate("2013-11-01").withTime("10:00:00,000").build());

        events.add(Event.Builder.createAccountAddMemberEvent("acid1", "id1", Arrays.asList("account/owner"))
                                .withDate("2013-11-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent("acid1", "id1", Arrays.asList("account/member"))
                                .withDate("2013-11-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent("acid2", "id2", Arrays.asList("account/owner"))
                                .withDate("2013-11-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent("acid2", "id2", Arrays.asList("account/member"))
                                .withDate("2013-11-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent("acid3", "id3", Arrays.asList("account/owner"))
                                .withDate("2013-11-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent("acid3", "id3", Arrays.asList("account/member"))
                                .withDate("2013-11-01", "10:00:00").build());

        //user5 has two accounts
        events.add(Event.Builder.createAccountAddMemberEvent("acid5", "id5", Arrays.asList("account/owner"))
                                .withDate("2013-11-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent("acid5", "id5", Arrays.asList("account/member"))
                                .withDate("2013-11-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent("acid6", "id5", Arrays.asList("account/owner"))
                                .withDate("2013-11-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent("acid6", "id5", Arrays.asList("account/member"))
                                .withDate("2013-11-01", "10:00:00").build());

        events.add(Event.Builder.createCreditCardAddedEvent("id2", "acid2")
                                .withDate("2013-11-01", "11:00:00").build());
        events.add(Event.Builder.createCreditCardAddedEvent("id3", "acid3")
                                .withDate("2013-11-01", "11:00:00").build());

        events.add(Event.Builder.createSubscriptionAddedEvent("acid2", "OnPremises", "pay-as-you-go")
                                .withDate("2013-11-01", "11:15:01").build());
        events.add(Event.Builder.createSubscriptionAddedEvent("acid3", "Saas", "pay-as-you-go")
                                .withDate("2013-11-01", "11:15:02").build());
        events.add(Event.Builder.createSubscriptionAddedEvent("acid3", "OnPremises", "opm-com-25u-y")
                                .withDate("2013-11-01", "11:15:03").build());

        events.add(Event.Builder.createLockedAccountEvent("acid2")
                                .withDate("2013-11-02", "11:00:00").build());
        events.add(Event.Builder.createLockedAccountEvent("acid3")
                                .withDate("2013-11-02", "11:00:00").build());

        //add credit card to 1st account of user5
        events.add(Event.Builder.createCreditCardAddedEvent("id5","acid5")
                                .withDate("2013-11-02", "11:00:00").build());

        //add 'Saas' subscription to 1st account of user5
        events.add(Event.Builder.createSubscriptionAddedEvent("acid5", "OnPremises", "pay-as-you-go")
                                .withDate("2013-11-02", "11:00:04").build());

        //lock 1st account of user5
        events.add(Event.Builder.createLockedAccountEvent("acid5")
                                .withDate("2013-11-02", "11:00:05").build());

        events.add(Event.Builder.createSubscriptionRemovedEvent("acid2", "OnPremises", "pay-as-you-go")
                                .withDate("2013-11-02", "11:15:07").build());

        //add credit card to 2nd account of user5
        events.add(Event.Builder.createCreditCardAddedEvent("id5","acid6")
                                .withDate("2013-11-03", "11:00:00").build());

        //add 'OnPremises' subscription to 2nd account of user5
        events.add(Event.Builder.createSubscriptionAddedEvent("acid6", "OnPremises", "opm-com-25u-y")
                                .withDate("2013-11-03", "11:00:06").build());

        //lock 2nd account of user5
        events.add(Event.Builder.createLockedAccountEvent("acid6")
                                .withDate("2013-11-03", "11:00:00").build());

        events.add(Event.Builder.createUnLockedAccountEvent("acid3")
                                .withDate("2013-11-03", "12:00:00").build());

        events.add(Event.Builder.createAccountRemoveMemberEvent("acid2", "uid1")
                                .withDate("2013-11-03", "10:00:00").build());

        events.add(Event.Builder.createUserCreatedEvent("id4", "user4@gmail.com", "user4@gmail.com")
                                .withDate("2013-11-03").withTime("10:00:00,000").build());

        events.add(Event.Builder.createAccountAddMemberEvent("acid4", "id4", Arrays.asList("account/owner"))
                                .withDate("2013-11-03", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent("acid4", "id4", Arrays.asList("account/member"))
                                .withDate("2013-11-03", "10:00:00").build());

        events.add(Event.Builder.createCreditCardAddedEvent("id4", "acid4")
                                .withDate("2013-11-03", "11:00:00").build());

        //remove 'Saas' subscription from 1st account of user5
        events.add(Event.Builder.createSubscriptionRemovedEvent("acid5", "OnPremises", "pay-as-you-go")
                                .withDate("2013-11-04", "11:00:08").build());

        //remove credit card from 1st account of user5
        events.add(Event.Builder.createCreditCardRemovedEvent("id5", "acid5")
                                .withDate("2013-11-04", "11:00:00").build());

        //unlock 1st account of user5
        events.add(Event.Builder.createUnLockedAccountEvent("acid5")
                                .withDate("2013-11-04", "12:00:00").build());

        //remove 'OnPremises' subscription from 2nd account of user5
        events.add(Event.Builder.createSubscriptionRemovedEvent("acid6", "OnPremises", "opm-com-25u-y")
                                .withDate("2013-11-05", "11:00:09").build());

        //remove credit card from 2nd account of user5
        events.add(Event.Builder.createCreditCardRemovedEvent("id5","acid6")
                                .withDate("2013-11-05", "11:00:00").build());

        //unlock 2nd account of user5
        events.add(Event.Builder.createUnLockedAccountEvent("acid6")
                                .withDate("2013-11-05", "12:00:00").build());

        return LogGenerator.generateLog(events);
    }

    protected void computeStatistics(String date) throws Exception {
        executeScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST, date);
        executeScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST, date);
        executeScript(ScriptType.USERS_ACCOUNTS, MetricType.USERS_ACCOUNTS_LIST, date);
        executeScript(ScriptType.EVENTS, MetricType.CREDIT_CARD_ADDED, date);
        executeScript(ScriptType.EVENTS, MetricType.CREDIT_CARD_REMOVED, date);
        executeScript(ScriptType.EVENTS, MetricType.ACCOUNT_LOCKED, date);
        executeScript(ScriptType.EVENTS, MetricType.ACCOUNT_UNLOCKED, date);
        executeScript(ScriptType.EVENTS, MetricType.SUBSCRIPTION_ADDED, date);
        executeScript(ScriptType.EVENTS, MetricType.SUBSCRIPTION_REMOVED, date);
    }

    private void executeScript(ScriptType scriptType, MetricType metricType, String date) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.putAll(scriptsManager.getScript(scriptType, metricType).getParamsAsMap());
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.put(Parameters.LOG, prepareLog().getAbsolutePath());
        pigServer.execute(scriptType, builder.build());
    }

    @Test
    public void testIsAccountLockedown() throws Exception {
        computeStatistics("20131101");
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id1"));
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id2"));
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id3"));
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id4"));
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id5"));

        computeStatistics("20131102");
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id1"));
        assertTrue(MarketoReportGeneratorUtils.isUserAccountsLockdown("id2"));
        assertTrue(MarketoReportGeneratorUtils.isUserAccountsLockdown("id3"));
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id4"));
        assertTrue(MarketoReportGeneratorUtils.isUserAccountsLockdown("id5"));

        computeStatistics("20131103");
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id1"));
        assertTrue(MarketoReportGeneratorUtils.isUserAccountsLockdown("id2"));
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id3"));
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id4"));
        assertTrue(MarketoReportGeneratorUtils.isUserAccountsLockdown("id5"));

        computeStatistics("20131104");
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id1"));
        assertTrue(MarketoReportGeneratorUtils.isUserAccountsLockdown("id2"));
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id3"));
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id4"));
        assertTrue(MarketoReportGeneratorUtils.isUserAccountsLockdown("id5"));

        computeStatistics("20131105");
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id1"));
        assertTrue(MarketoReportGeneratorUtils.isUserAccountsLockdown("id2"));
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id3"));
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id4"));
        assertFalse(MarketoReportGeneratorUtils.isUserAccountsLockdown("id5"));
    }

    @Test
    public void testIsUserCreditCardAdded() throws Exception {
        computeStatistics("20131101");
        assertFalse(MarketoReportGeneratorUtils.isUserCreditCardAdded("id1"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id2"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id3"));
        assertFalse(MarketoReportGeneratorUtils.isUserCreditCardAdded("id4"));
        assertFalse(MarketoReportGeneratorUtils.isUserCreditCardAdded("id5"));

        computeStatistics("20131102");
        assertFalse(MarketoReportGeneratorUtils.isUserCreditCardAdded("id1"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id2"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id3"));
        assertFalse(MarketoReportGeneratorUtils.isUserCreditCardAdded("id4"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id5"));

        computeStatistics("20131103");
        assertFalse(MarketoReportGeneratorUtils.isUserCreditCardAdded("id1"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id2"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id3"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id4"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id5"));

        computeStatistics("20131104");
        assertFalse(MarketoReportGeneratorUtils.isUserCreditCardAdded("id1"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id2"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id3"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id4"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id5"));

        computeStatistics("20131105");
        assertFalse(MarketoReportGeneratorUtils.isUserCreditCardAdded("id1"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id2"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id3"));
        assertTrue(MarketoReportGeneratorUtils.isUserCreditCardAdded("id4"));
        assertFalse(MarketoReportGeneratorUtils.isUserCreditCardAdded("id5"));
    }

    @Test
    public void testGetDateOnPremisesSubscriptionAdded() throws Exception {
        computeStatistics("20131101");
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id1"));
        assertEquals(1383297301000L, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id2"));
        assertEquals(1383297303000L, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id3"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id4"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id5"));

        computeStatistics("20131102");
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id1"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id2"));
        assertEquals(1383297303000L, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id3"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id4"));
        assertEquals(1383382804000L, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id5"));

        computeStatistics("20131103");
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id1"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id2"));
        assertEquals(1383297303000L, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id3"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id4"));
        assertEquals(1383382804000L, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id5"));

        computeStatistics("20131104");
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id1"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id2"));
        assertEquals(1383297303000L, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id3"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id4"));
        assertEquals(1383469206000L, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id5"));

        computeStatistics("20131105");
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id1"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id2"));
        assertEquals(1383297303000L, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id3"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id4"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionAdded("id5"));
    }

    @Test
    public void testGetDateOnPremisesSubscriptionRemoved() throws Exception {
        computeStatistics("20131101");
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id1"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id2"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id3"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id4"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id5"));

        computeStatistics("20131102");
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id1"));
        assertEquals(1383383707000L, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id2"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id3"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id4"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id5"));

        computeStatistics("20131103");
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id1"));
        assertEquals(1383383707000L, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id2"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id3"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id4"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id5"));

        computeStatistics("20131104");
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id1"));
        assertEquals(1383383707000L, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id2"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id3"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id4"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id5"));

        computeStatistics("20131105");
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id1"));
        assertEquals(1383383707000L, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id2"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id3"));
        assertEquals(0, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id4"));
        assertEquals(1383642009000L, MarketoReportGeneratorUtils.getDateOnPremisesSubscriptionRemoved("id5"));
    }
}
