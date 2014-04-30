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
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.accounts.AbstractAccountMetric;
import com.codenvy.analytics.metrics.accounts.AccountWorkspacesList;
import com.codenvy.analytics.metrics.accounts.AccountsList;
import com.codenvy.api.analytics.shared.dto.MetricInfoDTO;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        putPaginationParameters(page, perPage, context);
        putNotSystemPrincipal(context, securityContext);
        putDefaultValueIfAbsent(context, Parameters.FROM_DATE);
        putDefaultValueIfAbsent(context, Parameters.TO_DATE);

        return context;
    }

    public static Map<String, String> extractParams(UriInfo info, SecurityContext securityContext) {
        return extractParams(info, null, null, securityContext);
    }

    public static boolean isRolesAllowed(MetricInfoDTO metricInfoDTO, SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        List<String> rolesAllowed = metricInfoDTO.getRolesAllowed();

        if (principal == null || rolesAllowed.isEmpty()) {
            return false;
        }

        if (Utils.isSystemUser(principal.getName(), securityContext) || rolesAllowed.contains("any")) {
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

    public static boolean isSystemUser(String email, SecurityContext securityContext) {
        Matcher matcher = ADMIN_ROLE_EMAIL_PATTERN.matcher(email);
        return matcher.find()
               || securityContext.isUserInRole("system/admin")
               || securityContext.isUserInRole("system/manager");
    }

    private static void putDefaultValueIfAbsent(Map<String, String> context, Parameters param) {
        if (!context.containsKey(param.toString())) {
            context.put(param.toString(), param.getDefaultValue());
        }
    }


    private static void putNotSystemPrincipal(Map<String, String> context,
                                              SecurityContext securityContext) {
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            String user = securityContext.getUserPrincipal().getName();
            if (!isSystemUser(user, securityContext)) {
                context.put("USER", user);
                context.put("WS", getWorkspaces(context));
            }
        }
    }

    private static String getWorkspaces(Map<String, String> context) {
        try {
            AccountsList accountsList = new AccountsList();
            ListValueData accounts = doAccountFilter(context, (ListValueData)accountsList.getValue(Context.EMPTY));

            StringBuilder sb = new StringBuilder();
            for (ValueData accountValueData : accounts.getAll()) {
                Map<String, ValueData> mapAccount = (Map<String, ValueData>)accountValueData;

                Context.Builder builder = new Context.Builder();
                builder.put(MetricFilter.ACCOUNT_ID, mapAccount.get(AccountWorkspacesList.ACCOUNT_ID));

                AccountWorkspacesList accountWorkspacesList = new AccountWorkspacesList();
                ListValueData workspaces = doWorkspaceFilter(context,
                                                             (ListValueData)accountWorkspacesList
                                                                     .getValue(builder.build()));

                for (ValueData workspaceValueData : workspaces.getAll()) {
                    Map<String, ValueData> mapWorkspace = (Map<String, ValueData>)workspaceValueData;

                    if (sb.toString().length() != 0) {
                        sb.append(",");
                    }
                    sb.append(mapWorkspace.get(AccountWorkspacesList.WORKSPACE_NAME));
                }
            }

            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("Can not get workspaces for current user", e);
        }

    }

    private static ListValueData doWorkspaceFilter(Map<String, String> context, ListValueData value) {
        if (context.containsKey(MetricFilter.DATA_UNIVERSE.toString().toLowerCase())) {
            String dataUniverseFilter = context.get(MetricFilter.DATA_UNIVERSE.toString().toLowerCase());

            if (AbstractAccountMetric.WORKSPACE_ROLE_DEVELOPER.equalsIgnoreCase(dataUniverseFilter) ||
                AbstractAccountMetric.WORKSPACE_ROLE_ADMIN.equalsIgnoreCase(dataUniverseFilter)) {

                List<ValueData> list2Return = new ArrayList<>();
                for (ValueData valueData : value.getAll()) {

                    Map<String, ValueData> map = ((MapValueData)valueData).getAll();

                    if (map.get(AbstractAccountMetric.WORKSPACE_ROLES).getAsString().contains(dataUniverseFilter)) {
                        list2Return.add(valueData);
                    }
                }
                return new ListValueData(list2Return);
            }
        }

        return value;
    }

    private static ListValueData doAccountFilter(Map<String, String> context, ListValueData value) {
        if (context.containsKey(MetricFilter.DATA_UNIVERSE.toString().toLowerCase())) {
            String dataUniverseFilter = context.get(MetricFilter.DATA_UNIVERSE.toString().toLowerCase());

            if (AbstractAccountMetric.ACCOUNT_ROLE_MEMBER.equalsIgnoreCase(dataUniverseFilter) ||
                AbstractAccountMetric.ACCOUNT_ROLE_OWNER.equalsIgnoreCase(dataUniverseFilter)) {

                List<ValueData> list2Return = new ArrayList<>();
                for (ValueData valueData : value.getAll()) {

                    Map<String, ValueData> map = ((MapValueData)valueData).getAll();

                    if (map.get(AbstractAccountMetric.ACCOUNT_ROLES).getAsString().contains(dataUniverseFilter)) {
                        list2Return.add(valueData);
                    }
                }
                return new ListValueData(list2Return);
            }
        }

        return value;
    }

    private static void putQueryParameters(MultivaluedMap<String, String> parameters, Map<String, String> context) {
        for (String key : parameters.keySet()) {
            context.put(key.toUpperCase(), parameters.getFirst(key));
        }
    }

    private static void putPaginationParameters(String page, String perPage, Map<String, String> context) {
        if (page != null && perPage != null) {
            context.put("PAGE", page);
            context.put("PER_PAGE", perPage);
        }
    }
}
