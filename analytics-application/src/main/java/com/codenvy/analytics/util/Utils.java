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
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.accounts.AbstractAccountMetric;
import com.codenvy.analytics.metrics.accounts.AccountWorkspacesList;
import com.codenvy.api.analytics.shared.dto.MetricInfoDTO;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codenvy.analytics.Utils.getFilterAsString;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;

/** @author Anatoliy Bazko */
public class Utils {

    private static final Pattern ADMIN_ROLE_EMAIL_PATTERN = Pattern.compile("@codenvy[.]com$");

    public static Map<String, String> extractParams(UriInfo info,
                                                    String page,
                                                    String perPage,
                                                    SecurityContext securityContext) {

        MultivaluedMap<String, String> parameters = info.getQueryParameters();
        Map<String, String> context = new HashMap<>(parameters.size());

        putQueryParameters(parameters, context);
        if (page != null && perPage != null) {
            putPaginationParameters(page, perPage, context);
        }
        if (securityContext != null) {
            putPossibleUsersAsFilter(context, securityContext);
            putPossibleWorkspacesAsFilter(context, securityContext);
        }
        putDefaultValueIfAbsent(context, Parameters.FROM_DATE);
        putDefaultValueIfAbsent(context, Parameters.TO_DATE);
        validateFormDateToDate(context);

        context.remove(MetricFilter.DATA_UNIVERSE.toString().toLowerCase());

        return context;
    }

    public static Map<String, String> extractParams(UriInfo info, SecurityContext securityContext) {
        return extractParams(info,
                             null,
                             null,
                             securityContext);
    }

    public static Map<String, String> extractParams(UriInfo info) {
        return extractParams(info,
                             null,
                             null,
                             null);
    }

    public static boolean isRolesAllowed(MetricInfoDTO metricInfoDTO, SecurityContext securityContext) {
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

    public static boolean isSystemUser(String email) {
        Matcher matcher = ADMIN_ROLE_EMAIL_PATTERN.matcher(email);
        return matcher.find();
    }

    public static boolean isSystemUser(SecurityContext securityContext) {
        Principal userPrincipal = securityContext.getUserPrincipal();
        if (userPrincipal == null) {
            return false;
        }

        return isSystemUser(userPrincipal.getName())
               || securityContext.isUserInRole("system/admin")
               || securityContext.isUserInRole("system/manager");
    }

    private static void putDefaultValueIfAbsent(Map<String, String> context, Parameters param) {
        if (!context.containsKey(param.toString())) {
            context.put(param.toString(), param.getDefaultValue());
        }
    }

    private static void validateFormDateToDate(Map<String, String> context) {
        try {
            Calendar fromDate = com.codenvy.analytics.Utils.parseDate(context.get(Parameters.FROM_DATE.toString()));
            Calendar toDate = com.codenvy.analytics.Utils.parseDate(context.get(Parameters.TO_DATE.toString()));

            if (fromDate.after(toDate)) {
                throw new RuntimeException("The parameter " + Parameters.TO_DATE + " must be greater than " + Parameters.FROM_DATE);
            }
        } catch (ParseException e) {
            throw new RuntimeException("Can not parse " + Parameters.FROM_DATE + " or " + Parameters.TO_DATE + " parameters");
        }
    }

    private static void putPossibleUsersAsFilter(Map<String, String> context, SecurityContext securityContext) {
        if (!isSystemUser(securityContext)) {
            Set<String> allowedUsers = getAllowedUsers();

            if (!context.containsKey(MetricFilter.USER.toString())) {
                context.put(MetricFilter.USER.toString(), getFilterAsString(allowedUsers));
            }
            context.put(Parameters.ORIGINAL_USER.toString(), getFilterAsString(allowedUsers));
        }
    }

    private static Set<String> getAllowedUsers() {
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

    private static Set<String> getUsers(String accountId) throws IOException {
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

    private static void putPossibleWorkspacesAsFilter(Map<String, String> context, SecurityContext securityContext) {
        if (!isSystemUser(securityContext)) {
            Set<String> allowedWorkspaces = getAllowedWorkspacesForCurrentUser(context);

            if (!context.containsKey(MetricFilter.WS.toString())) {
                context.put(MetricFilter.WS.toString(), getFilterAsString(allowedWorkspaces));
            }
            context.put(Parameters.ORIGINAL_WS.toString(), getFilterAsString(allowedWorkspaces));
        }
    }

    private static Set<String> getAllowedWorkspacesForCurrentUser(Map<String, String> context) {
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

    private static Set<String> getWorkspacesByRole(String role, ListValueData accounts) throws IOException {
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

    private static boolean isWorkspacesRoleExists(String role) {
        return role != null && (role.equalsIgnoreCase(AbstractAccountMetric.ROLE_WORKSPACE_DEVELOPER)
                                || role.equalsIgnoreCase(AbstractAccountMetric.ROLE_WORKSPACE_ADMIN));
    }

    private static boolean isAccountsRoleExists(String role) {
        return role != null && (role.equalsIgnoreCase(AbstractAccountMetric.ROLE_ACCOUNT_MEMBER)
                                || role.equalsIgnoreCase(AbstractAccountMetric.ROLE_ACCOUNT_OWNER));
    }

    private static ListValueData doWorkspaceFilterByWorkspaceRole(String role, ListValueData workspaces) {
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

    private static ListValueData doFilterAccountsByRole(String role, ListValueData accounts) {
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


    private static void putQueryParameters(MultivaluedMap<String, String> parameters, Map<String, String> context) {
        for (String key : parameters.keySet()) {
            context.put(key.toUpperCase(), parameters.getFirst(key));
        }
    }

    private static void putPaginationParameters(String page, String perPage, Map<String, String> context) {
        context.put("PAGE", page);
        context.put("PER_PAGE", perPage);
    }
}
