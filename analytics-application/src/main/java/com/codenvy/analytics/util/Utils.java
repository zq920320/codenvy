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
package com.codenvy.analytics.util;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.accounts.AbstractAccountMetric;
import com.codenvy.analytics.metrics.accounts.AccountWorkspacesList;
import com.codenvy.api.analytics.shared.dto.MetricInfoDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.codenvy.analytics.Utils.getFilterAsSet;
import static com.codenvy.analytics.Utils.getFilterAsString;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;

/** @author Anatoliy Bazko */
@Singleton
public class Utils {
    private final static String RESTRICT_EVERYONE = "";
    private final Pattern allowedPrincipalsPattern;

    private final UserPrincipalCache cache;

    @Inject
    public Utils(UserPrincipalCache userPrincipalCache, Configurator configurator) {
        this.cache = userPrincipalCache;

        String adminLoginRegexp = configurator.getString("analytics.admin_login_regexp");
        if (adminLoginRegexp == null) {
            allowedPrincipalsPattern = Pattern.compile(RESTRICT_EVERYONE);
        } else {
            allowedPrincipalsPattern = Pattern.compile(adminLoginRegexp);
        }
    }

    public Map<String, String> extractParams(UriInfo info,
                                             String page,
                                             String perPage,
                                             SecurityContext securityContext) throws ParseException {

        MultivaluedMap<String, String> parameters = info.getQueryParameters();
        Map<String, String> context = new HashMap<>(parameters.size());

        putQueryParameters(parameters, context);
        if (page != null && perPage != null) {
            putPaginationParameters(page, perPage, context);
        }
        if (securityContext != null && !isSystemUser(securityContext)) {
            putPossibleUsersAsFilter(context, securityContext);
            putPossibleWorkspacesAsFilter(context, securityContext);

            Principal principal = securityContext.getUserPrincipal();
            if (cache.exist(principal)) {
                cache.updateAccessTime(principal);
            } else {
                Set<String> allowedUsers = getFilterAsSet(context.get(Parameters.ORIGINAL_USER.toString()));
                Set<String> allowedWorkspaces = getFilterAsSet(context.get(Parameters.ORIGINAL_WS.toString()));

                String dataUniverse = context.get(MetricFilter.DATA_UNIVERSE.toString());
                UserPrincipalCache.UserContext userContext = new UserPrincipalCache.UserContext(dataUniverse, allowedUsers, allowedWorkspaces);
                cache.put(principal, userContext);
            }
        }
        validateFormDateToDate(context);
        context.remove(MetricFilter.DATA_UNIVERSE.toString());

        return context;
    }

    public Map<String, String> extractParams(UriInfo info, SecurityContext securityContext) throws ParseException {
        return extractParams(info,
                             null,
                             null,
                             securityContext);
    }

    public Map<String, String> extractParams(UriInfo info) throws ParseException {
        return extractParams(info,
                             null,
                             null,
                             null);
    }

    public boolean isRolesAllowed(MetricInfoDTO metricInfoDTO, SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        List<String> rolesAllowed = metricInfoDTO.getRolesAllowed();

        if (rolesAllowed.contains("any")) {
            return true;
        }

        if (principal == null || rolesAllowed.isEmpty()) {
            return false;
        }

        if (isSystemUser(securityContext)) {
            return true;
        }

        for (String role : rolesAllowed) {
            if (securityContext.isUserInRole(role)) {
                return true;
            }
        }

        return false;
    }

    public boolean isSystemUser(String email) {
        return allowedPrincipalsPattern.matcher(email).matches();
    }

    public boolean isSystemUser(SecurityContext securityContext) {
        Principal userPrincipal = securityContext.getUserPrincipal();
        return userPrincipal != null &&
               (isSystemUser(userPrincipal.getName())
                || securityContext.isUserInRole("system/admin")
                || securityContext.isUserInRole("system/manager"));

    }

    private void validateFormDateToDate(Map<String, String> context) throws ParseException {
        String fromDateKey = Parameters.FROM_DATE.toString();
        Calendar fromDate = com.codenvy.analytics.Utils
                .parseDate(context.containsKey(fromDateKey) ? context.get(fromDateKey) : Parameters.FROM_DATE.getDefaultValue());

        String toDateKey = Parameters.TO_DATE.toString();
        Calendar toDate = com.codenvy.analytics.Utils
                .parseDate(context.containsKey(toDateKey) ? context.get(toDateKey) : Parameters.TO_DATE.getDefaultValue());

        if (fromDate.after(toDate)) {
            throw new RuntimeException("The parameter " + Parameters.TO_DATE + " must be greater than " + Parameters.FROM_DATE);
        }
    }

    private void putPossibleUsersAsFilter(Map<String, String> context, SecurityContext securityContext) {
        Set<String> allowedUsers;

        UserPrincipalCache.UserContext userContext = cache.get(securityContext.getUserPrincipal());
        if (userContext != null) {
            allowedUsers = userContext.getAllowedUsers();
        } else {
            allowedUsers = getAllowedUsers();
        }

        if (!context.containsKey(MetricFilter.USER_ID.toString())) {
            context.put(MetricFilter.USER_ID.toString(), getFilterAsString(allowedUsers));
        }
        context.put(Parameters.ORIGINAL_USER.toString(), getFilterAsString(allowedUsers));
    }

    private Set<String> getAllowedUsers() {
        try {
            Set<String> users = new HashSet<>();

            Metric accountsMetric = MetricFactory.getMetric(MetricType.ACCOUNTS_LIST);
            ListValueData accounts = getAsList(accountsMetric, Context.EMPTY);

            for (ValueData account : accounts.getAll()) {
                Map<String, ValueData> map = ((MapValueData)account).getAll();

                String accountId = map.get(AbstractAccountMetric.ACCOUNT_ID).getAsString();

                users.addAll(getUsers(accountId));
            }

            return users;
        } catch (IOException e) {
            throw new RuntimeException("Can not get users in workspaces of current user", e);
        }
    }

    private Set<String> getUsers(String accountId) throws IOException {
        Set<String> users = new HashSet<>();

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.ACCOUNT_ID, accountId);

        Metric usersMetric = MetricFactory.getMetric(MetricType.ACCOUNT_USERS_ROLES_LIST);
        ListValueData usersData = getAsList(usersMetric, builder.build());

        for (ValueData user : usersData.getAll()) {
            Map<String, ValueData> userData = ((MapValueData)user).getAll();
            users.add(userData.get(AbstractMetric.USER).getAsString());
        }

        return users;
    }

    private void putPossibleWorkspacesAsFilter(Map<String, String> context, SecurityContext securityContext) {
        Set<String> allowedWorkspaces;

        UserPrincipalCache.UserContext userContext = cache.get(securityContext.getUserPrincipal());
        String dataUniverse = context.get(MetricFilter.DATA_UNIVERSE.toString());

        if (userContext != null && isSameDataUniverse(userContext.getDataUniverse(), dataUniverse)) {
            allowedWorkspaces = userContext.getAllowedWorkspaces();
        } else {
            allowedWorkspaces = getAllowedWorkspacesForCurrentUser(context);
        }

        if (!context.containsKey(MetricFilter.WS_ID.toString())) {
            context.put(MetricFilter.WS_ID.toString(), getFilterAsString(allowedWorkspaces));
        }
        context.put(Parameters.ORIGINAL_WS.toString(), getFilterAsString(allowedWorkspaces));
    }

    private boolean isSameDataUniverse(String currentDataUniverse, String newDataUniverse) {
        return (currentDataUniverse == null && newDataUniverse == null)
               || (currentDataUniverse != null && currentDataUniverse.equals(newDataUniverse));
    }

    private Set<String> getAllowedWorkspacesForCurrentUser(Map<String, String> context) {
        try {
            String role = context.get(MetricFilter.DATA_UNIVERSE.toString());

            Metric accountsMetric = MetricFactory.getMetric(MetricType.ACCOUNTS_LIST);
            ListValueData accounts = getAsList(accountsMetric, Context.EMPTY);

            if (isAccountsRoleExists(role)) {
                accounts = doFilterAccountsByRole(role, accounts);
            }

            return getWorkspacesByRole(role, accounts);
        } catch (IOException e) {
            throw new RuntimeException("Can not get workspaces for current user", e);
        }

    }

    private Set<String> getWorkspacesByRole(String role, ListValueData accounts) throws IOException {
        Set<String> result = new HashSet<>();
        Metric accountWorkspaces = MetricFactory.getMetric(MetricType.ACCOUNT_WORKSPACES_LIST);

        for (ValueData account : accounts.getAll()) {
            Map<String, ValueData> accountData = ((MapValueData)account).getAll();

            Context.Builder builder = new Context.Builder();
            builder.put(MetricFilter.ACCOUNT_ID, accountData.get(AccountWorkspacesList.ACCOUNT_ID).getAsString());

            ListValueData workspaces = getAsList(accountWorkspaces, builder.build());
            if (isWorkspacesRoleExists(role)) {
                workspaces = doWorkspaceFilterByWorkspaceRole(role, workspaces);
            }

            for (ValueData workspace : workspaces.getAll()) {
                Map<String, ValueData> workspaceData = ((MapValueData)workspace).getAll();
                result.add(workspaceData.get(AccountWorkspacesList.WORKSPACE_ID).getAsString());
            }
        }

        return result;
    }

    private boolean isWorkspacesRoleExists(String role) {
        return role != null && (role.equalsIgnoreCase(AbstractAccountMetric.ROLE_WORKSPACE_DEVELOPER)
                                || role.equalsIgnoreCase(AbstractAccountMetric.ROLE_WORKSPACE_ADMIN));
    }

    private boolean isAccountsRoleExists(String role) {
        return role != null && (role.equalsIgnoreCase(AbstractAccountMetric.ROLE_ACCOUNT_MEMBER)
                                || role.equalsIgnoreCase(AbstractAccountMetric.ROLE_ACCOUNT_OWNER));
    }

    private ListValueData doWorkspaceFilterByWorkspaceRole(String role, ListValueData workspaces) {
        List<ValueData> list2Return = new ArrayList<>();
        for (ValueData workspace : workspaces.getAll()) {
            Map<String, ValueData> map = ((MapValueData)workspace).getAll();

            String roles = map.get(AbstractAccountMetric.WORKSPACE_ROLES).getAsString();
            if (roles.contains(role)) {
                list2Return.add(workspace);
            }
        }
        return new ListValueData(list2Return);
    }

    private ListValueData doFilterAccountsByRole(String role, ListValueData accounts) {
        List<ValueData> list2Return = new ArrayList<>();
        for (ValueData account : accounts.getAll()) {
            Map<String, ValueData> map = ((MapValueData)account).getAll();

            String roles = map.get(AbstractAccountMetric.ACCOUNT_ROLES).getAsString();
            if (roles.contains(role)) {
                list2Return.add(account);
            }
        }
        return new ListValueData(list2Return);
    }


    private void putQueryParameters(MultivaluedMap<String, String> parameters, Map<String, String> context) {
        for (String key : parameters.keySet()) {
            context.put(key.toUpperCase(), parameters.getFirst(key));
        }
    }

    private void putPaginationParameters(String page, String perPage, Map<String, String> context) {
        context.put("PAGE", page);
        context.put("PER_PAGE", perPage);
    }
}
