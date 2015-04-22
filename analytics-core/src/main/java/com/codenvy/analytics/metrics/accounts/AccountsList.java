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

import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.che.api.account.shared.dto.AccountDescriptor;
import org.eclipse.che.api.account.shared.dto.MemberDescriptor;
import org.eclipse.che.api.account.shared.dto.SubscriptionDescriptor;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.Utils.filterAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsDouble;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsLong;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;

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
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "List of accounts";
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Context context) throws IOException {
        List<AccountDescriptor> accountsDesc = getAccountDescriptors(context);
        return toValueData(context, accountsDesc);
    }

    protected ValueData toValueData(Context context, List<AccountDescriptor> accountsDesc) throws IOException {
        List<ValueData> result = new ArrayList<>(accountsDesc.size());

        for (AccountDescriptor accountDesc : accountsDesc) {
            Map<String, ValueData> m = new HashMap<>();

            m.put("account_id", StringValueData.valueOf(accountDesc.getId()));
            m.put("name", StringValueData.valueOf(accountDesc.getName()));

            int projects = 0;

            List<WorkspaceDescriptor> workspacesDesc = getWorkspacesByAccountId(accountDesc.getId());
            m.put("workspaces", LongValueData.valueOf(workspacesDesc.size()));
            for (WorkspaceDescriptor workspace : workspacesDesc) {
                projects += getProjects(workspace.getId()).size();
            }
            m.put("projects", LongValueData.valueOf(projects));

            List<SubscriptionDescriptor> subscriptionsDesc = getSubscriptionsByAccountId(accountDesc.getId());
            List<String> servicesIds = FluentIterable.from(subscriptionsDesc).transform(new Function<SubscriptionDescriptor, String>() {
                @Override
                public String apply(SubscriptionDescriptor subscriptionDesc) {
                    return subscriptionDesc.getServiceId();
                }
            }).toList();
            m.put("subscriptions", StringValueData.valueOf(servicesIds.toString()));

            List<MemberDescriptor> members = getMembersByAccountId(accountDesc.getId());
            m.put("members", LongValueData.valueOf(members.size()));

            UserDescriptor accountOwner = getAccountOwner(members);
            m.put("user", StringValueData.valueOf(accountOwner.getId()));

            if (context.exists(MetricFilter.ACCOUNT_ID)) {
                addUsersStatistics(context, m);

                List<Link> linkFactories = getFactoriesByAccountId(accountDesc.getId());
                m.put("factories", LongValueData.valueOf(linkFactories.size()));
            }

            result.add(MapValueData.valueOf(m));
        }

        return ListValueData.valueOf(result);
    }

    private void addUsersStatistics(Context context, Map<String, ValueData> result) throws IOException {
        Metric usersStat = MetricFactory.getMetric(MetricType.ACCOUNT_USERS_STATISTICS_LIST);

        long time = 0;
        long runs = 0;
        long builds = 0;
        double gbHours = 0;
        for (ValueData vd : getAsList(usersStat, context).getAll()) {
            Map<String, ValueData> m = treatAsMap(vd);

            time += treatAsLong(m.get("time"));
            runs += treatAsLong(m.get("runs"));
            builds += treatAsLong(m.get("builds"));
            gbHours += treatAsDouble(m.get("gigabyte_ram_hours"));
        }

        result.put("time", LongValueData.valueOf(time));
        result.put("runs", LongValueData.valueOf(runs));
        result.put("builds", LongValueData.valueOf(builds));
        result.put("gigabyte_ram_hours", DoubleValueData.valueOf(gbHours));
    }

    protected UserDescriptor getAccountOwner(List<MemberDescriptor> members) throws IOException {
        Optional<MemberDescriptor> first = FluentIterable.from(members).firstMatch(new Predicate<MemberDescriptor>() {
            @Override
            public boolean apply(MemberDescriptor memberDescriptor) {
                return memberDescriptor.getRoles().contains("account/owner");
            }
        });

        if (!first.isPresent()) {
            throw new IOException("Account owner not found");
        }
        MemberDescriptor memberDescriptor = first.get();

        return RESOURCE_FETCHER.fetchResource(UserDescriptor.class,
                                              "GET",
                                              "/user/" + memberDescriptor.getUserId());
    }

    protected List<AccountDescriptor> getAccountDescriptors(Context context) throws IOException {
        List<AccountDescriptor> result = null;
        String accountIdFilter = context.getAsString("ACCOUNT_ID");
        if (accountIdFilter != null) {
            ImmutableList<AccountDescriptor> byIds =
                    FluentIterable.from(filterAsList(accountIdFilter)).transform(new Function<String, AccountDescriptor>() {
                        @Nullable
                        @Override
                        public AccountDescriptor apply(String accountId) {
                            try {
                                return getAccountDescriptorById(accountId);
                            } catch (IOException e) {
                                return null;
                            }
                        }
                    }).filter(Predicates.notNull()).toList();

            result = merge(result, byIds);
            if (result.isEmpty()) {
                return Collections.emptyList();
            }
        }

        String accountNameFilter = context.getAsString("ACCOUNT_NAME");
        if (accountNameFilter != null) {
            ImmutableList<AccountDescriptor> byNames =
                    FluentIterable.from(filterAsList(accountNameFilter)).transform(new Function<String, AccountDescriptor>() {
                        @Nullable
                        @Override
                        public AccountDescriptor apply(String accountName) {
                            try {
                                return getAccountDescriptorByName(accountName);
                            } catch (IOException e) {
                                return null;
                            }
                        }
                    }).filter(Predicates.notNull()).toList();

            result = merge(result, byNames);
            if (result.isEmpty()) {
                return Collections.emptyList();
            }
        }

        String ownerEmailsFilter = context.getAsString("USER");
        if (ownerEmailsFilter != null) {
            ImmutableList<AccountDescriptor> byEmails =
                    FluentIterable.from(filterAsList(ownerEmailsFilter)).transform(new Function<String, UserDescriptor>() {
                        @Nullable
                        @Override
                        public UserDescriptor apply(String ownerEmail) {
                            try {
                                return RESOURCE_FETCHER.fetchResource(UserDescriptor.class,
                                                                      "GET",
                                                                      "/user/find?email=" + ownerEmail);
                            } catch (IOException e) {
                                return null;
                            }
                        }
                    }).filter(Predicates.notNull()).transformAndConcat(new Function<UserDescriptor, List<MemberDescriptor>>() {
                        @Nullable
                        @Override
                        public List<MemberDescriptor> apply(UserDescriptor userDescriptor) {
                            try {
                                List<MemberDescriptor> memberDescriptors = RESOURCE_FETCHER.fetchResources(MemberDescriptor.class,
                                                                                                           "GET",
                                                                                                           "/account/memberships?userid=" +
                                                                                                           userDescriptor.getId());
                                Iterator<MemberDescriptor> iter = memberDescriptors.iterator();
                                while (iter.hasNext()) {
                                    MemberDescriptor memberDescriptor = iter.next();
                                    if (!memberDescriptor.getRoles().contains("account/owner")) {
                                        iter.remove();
                                    }
                                }

                                return memberDescriptors;
                            } catch (IOException e) {
                                return null;
                            }
                        }
                    }).filter(Predicates.notNull()).transform(new Function<MemberDescriptor, AccountDescriptor>() {
                        @Nullable
                        @Override
                        public AccountDescriptor apply(MemberDescriptor memberDescriptor) {
                            try {
                                return getAccountDescriptorById(memberDescriptor.getAccountReference().getId());
                            } catch (IOException e) {
                                return null;
                            }
                        }
                    }).filter(Predicates.notNull()).toList();

            result = merge(result, byEmails);
            if (result.isEmpty()) {
                return Collections.emptyList();
            }
        }

        return result == null ? Collections.<AccountDescriptor>emptyList() : result;
    }

    private List<AccountDescriptor> merge(@Nullable List<AccountDescriptor> result, ImmutableList<AccountDescriptor> search) {
        if (result == null) {
            return new ArrayList<>(search);
        } else {
            result.retainAll(search);
            return result;
        }
    }
}
