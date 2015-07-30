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

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.users.UsersOwnersAccountsSet;
import com.codenvy.analytics.pig.udf.UnionAccountRoles;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Alexander Reshetnyak
 */
public final class MarketoReportGeneratorUtils {

    public static final String ON_PREMISES = "OnPremises";

    /**
     * Account in lockdown [true | false]
     * We give 'true' 'true' if at least one account is in lockdown..
     * We give 'false' if all accounts don't lockdown.
     *
     * @param userId
     *        the user identifier
     * @return boolean
     */
    public static boolean isUserAccountsLockdown(String userId) throws IOException {
        List<String> userAccounts = getUserOwnerAccounts(userId);

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.ACCOUNT, userAccounts.toArray(new String[userAccounts.size()]));

        ListValueData locked = ValueDataUtil.getAsList(MetricFactory.getMetric(MetricType.ACCOUNT_LOCKED_LIST), builder.build());
        ListValueData unLocked = ValueDataUtil.getAsList(MetricFactory.getMetric(MetricType.ACCOUNT_UNLOCKED_LIST), builder.build());

        for (ValueData lockedMap : locked.getAll()) {
            Map<String, ValueData> dataLockedMap = ((MapValueData)lockedMap).getAll();
            String lockedAccountId = dataLockedMap.get(AbstractMetric.ACCOUNT).getAsString();
            long lockedDate = ((LongValueData)dataLockedMap.get(AbstractMetric.DATE)).getAsLong();

            boolean thisAccountLocked = true;

            for (ValueData unLockedMap : unLocked.getAll()) {
                Map<String, ValueData> dataUnLockedMap = ((MapValueData)unLockedMap).getAll();
                String unLockedAccountId = dataUnLockedMap.get(AbstractMetric.ACCOUNT).getAsString();
                long unLockedDate = ((LongValueData)dataUnLockedMap.get(AbstractMetric.DATE)).getAsLong();

                if (lockedAccountId.equals(unLockedAccountId) && lockedDate < unLockedDate) {
                    thisAccountLocked = false;
                }
            }

            if (thisAccountLocked) {
                return true;
            }
        }

        return false;
    }

    /**
     * Credit card added [true | false]
     * We give 'true' if at least one account have a credit card.
     *
     * @param userId
     *        the user identifier
     * @return boolean
     */
    public static boolean isUserCreditCardAdded(String userId) throws IOException {
        List<String> userAccounts = getUserOwnerAccounts(userId);

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.ACCOUNT, userAccounts.toArray(new String[userAccounts.size()]));

        LongValueData creditCardAdded = ValueDataUtil.getAsLong(MetricFactory.getMetric(MetricType.CREDIT_CARD_ADDED), builder.build());
        LongValueData creditCardRemoved = ValueDataUtil.getAsLong(MetricFactory.getMetric(MetricType.CREDIT_CARD_REMOVED), builder.build());

        return creditCardAdded.getAsLong() - creditCardRemoved.getAsLong() > 0;
    }

    /**
     * On-Prem subscription added [date]
     * We give 'date as long' for first subscription.
     * We give '' (empty string or long '0') if there aren't any subscription in accounts.
     *
     * @param userId
     *        the user identifier
     * @return date as long
     */
    public static long getDateOnPremisesSubscriptionAdded(String userId) throws IOException {
        List<String> userAccounts = getUserOwnerAccounts(userId);

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.ACCOUNT, userAccounts.toArray(new String[userAccounts.size()]));
        builder.put(MetricFilter.SERVICE, ON_PREMISES);

        ListValueData subsAdded = ValueDataUtil.getAsList(MetricFactory.getMetric(MetricType.SUBSCRIPTION_ADDED_LIST), builder.build());
        ListValueData subsRemoved = ValueDataUtil.getAsList(MetricFactory.getMetric(MetricType.SUBSCRIPTION_REMOVED_LIST), builder.build());

        long subscriptionAddedDate = Long.MAX_VALUE;

        for (ValueData subAddedMap : subsAdded.getAll()) {
            Map<String, ValueData> dataSubAddedMap = ((MapValueData)subAddedMap).getAll();
            String addedAccountId = dataSubAddedMap.get(AbstractMetric.ACCOUNT).getAsString();
            long addedDate = ((LongValueData)dataSubAddedMap.get(AbstractMetric.DATE)).getAsLong();
            String addedService = dataSubAddedMap.get(AbstractMetric.SERVICE).getAsString();

            boolean thisSubscriptionRemoved = false;

            for (ValueData subRemovedMap : subsRemoved.getAll()) {
                Map<String, ValueData> dataSubRemovedMap = ((MapValueData)subRemovedMap).getAll();
                String removedAccountId = dataSubRemovedMap.get(AbstractMetric.ACCOUNT).getAsString();
                long removedDate = ((LongValueData)dataSubRemovedMap.get(AbstractMetric.DATE)).getAsLong();
                String removedService = dataSubRemovedMap.get(AbstractMetric.SERVICE).getAsString();

                if (addedAccountId.equals(removedAccountId) &&
                    addedService.equals(removedService) &&
                    addedDate < removedDate) {
                    thisSubscriptionRemoved = true;
                }
            }

            if (!thisSubscriptionRemoved && addedDate < subscriptionAddedDate) {
                subscriptionAddedDate = addedDate;
            }
        }

        return subscriptionAddedDate < Long.MAX_VALUE ? subscriptionAddedDate : 0;
    }

    /**
     * On-Prem subscription removed [date]
     * We give '' (empty string or long '0') if at least one subscription exists.
     * We give 'date' if there aren't any subscriptions. This date is last subscription removed date.
     *
     * @param userId
     *        the user identifier
     * @return date as long
     */
    public static long getDateOnPremisesSubscriptionRemoved(String userId) throws IOException {
        List<String> userAccounts = getUserOwnerAccounts(userId);

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.ACCOUNT, userAccounts.toArray(new String[userAccounts.size()]));
        builder.put(MetricFilter.SERVICE, ON_PREMISES);

        ListValueData subsAdded = ValueDataUtil.getAsList(MetricFactory.getMetric(MetricType.SUBSCRIPTION_ADDED_LIST), builder.build());
        ListValueData subsRemoved = ValueDataUtil.getAsList(MetricFactory.getMetric(MetricType.SUBSCRIPTION_REMOVED_LIST), builder.build());

        long lastSubscriptionRemovedDate = Long.MIN_VALUE;

        for (ValueData subAddedMap : subsAdded.getAll()) {
            Map<String, ValueData> dataSubAddedMap = ((MapValueData)subAddedMap).getAll();
            String addedAccountId = dataSubAddedMap.get(AbstractMetric.ACCOUNT).getAsString();
            long addedDate = ((LongValueData)dataSubAddedMap.get(AbstractMetric.DATE)).getAsLong();
            String addedService = dataSubAddedMap.get(AbstractMetric.SERVICE).getAsString();

            boolean thisSubscriptionNotRemoved = true;

            for (ValueData subRemovedMap : subsRemoved.getAll()) {
                Map<String, ValueData> dataSubRemovedMap = ((MapValueData)subRemovedMap).getAll();
                String removedAccountId = dataSubRemovedMap.get(AbstractMetric.ACCOUNT).getAsString();
                long removedDate = ((LongValueData)dataSubRemovedMap.get(AbstractMetric.DATE)).getAsLong();
                String removedService = dataSubRemovedMap.get(AbstractMetric.SERVICE).getAsString();

                if (addedAccountId.equals(removedAccountId) &&
                    addedService.equals(removedService) &&
                    addedDate < removedDate) {

                    thisSubscriptionNotRemoved = false;

                    if (removedDate > lastSubscriptionRemovedDate) {
                        lastSubscriptionRemovedDate = removedDate;
                    }
                }
            }

            if (thisSubscriptionNotRemoved) {
                return 0;
            }
        }

        return lastSubscriptionRemovedDate > Long.MIN_VALUE ? lastSubscriptionRemovedDate : 0;
    }

    private static List<String> getUserOwnerAccounts(String userId) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_ID, userId);

        ListValueData l = ValueDataUtil.getAsList(MetricFactory.getMetric(MetricType.USERS_OWNERS_ACCOUNTS_LIST), builder.build());

        ValueData accounts = ValueDataUtil.getFirstValue(l, UsersOwnersAccountsSet.ACCOUNTS, StringValueData.valueOf("[]"));
        Set<String> accountsSet = UnionAccountRoles.rolesToSet(accounts.getAsString());
        return Arrays.asList(accountsSet.toArray(new String[accountsSet.size()]));
    }
}
