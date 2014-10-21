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
package com.codenvy.analytics.pig.scripts.util;

import com.codenvy.analytics.BaseTest;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/** @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a> */
public class Event {
    private final String              date;
    private final String              time;
    private final Map<String, String> params;

    private Event(String date, String time, Map<String, String> params) {
        this.date = date;
        this.time = time;
        this.params = params;
    }

    /** Represents event as a message of the log. */
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("127.0.0.1");
        builder.append(' ');

        builder.append(date == null ? "2010-10-10" : date);
        builder.append(' ');

        builder.append(time == null ? "10:10:10,000" : time + ",000");
        builder.append("[main] [INFO] [HelloWorld 1010] ");

        builder.append(" - ");
        for (Entry<String, String> entry : params.entrySet()) {
            builder.append(entry.getKey());
            builder.append("#");
            builder.append(entry.getValue());
            builder.append("#");
            builder.append(" ");
        }

        return builder.toString();
    }

    /** Helps to generate events. Uses Builder pattern. */
    public static class Builder {
        private String time;
        private String date;
        private Map<String, String> params  = new LinkedHashMap<>();

        public Builder withDate(String date) {
            if (date.length() == 8) {
                this.date = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
            } else {
                this.date = date;
            }
            return this;
        }

        public Builder withTime(String time) {
            this.time = time;
            return this;
        }

        public Builder withParam(String name, String value) {
            params.put(name, value);
            return this;
        }

        private Builder withParam(String name, Map<String, String> value) {
            StringBuilder sb = new StringBuilder();
            Iterator<Entry<String, String>> iterator = value.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, String> entry = iterator.next();

                sb.append(entry.getKey()).append("=").append(entry.getValue());

                if (iterator.hasNext()) {
                    sb.append(",");
                }
            }

            params.put(name, sb.toString());
            return this;
        }

        public Event build() {
            return new Event(date, time, params);
        }

        public static Builder createTenantCreatedEvent(String ws, String user) {
            return new Builder().withParam("EVENT", "tenant-created").withParam("WS", ws).withParam("USER", user);
        }

        public static Builder createProjectBuiltEvent(String user,
                                                      String ws,
                                                      String project,
                                                      String type) {
            return new Builder().withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("EVENT", "project-built")
                                .withParam("PROJECT", project).withParam("TYPE", type);
        }

        public static Builder createSessionUsageEvent(String user,
                                                      String ws,
                                                      String sessionId,
                                                      long startTime,
                                                      long usageTime,
                                                      boolean isFactory) {
            return new Builder().withParam("EVENT", isFactory ? "session-factory-usage" : "session-usage")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PARAMETERS", "USAGE-TIME=" + usageTime + ",START-TIME=" + startTime + ",SESSION-ID=" + sessionId);
        }

        public static Builder createSessionUsageEvent(String user,
                                                      String ws,
                                                      String sessionId,
                                                      String statStr,
                                                      String endStr,
                                                      boolean isFactory) throws Exception {

            Date startTime = BaseTest.fullDateFormat.parse(statStr);
            Date endTime = BaseTest.fullDateFormat.parse(endStr);
            return createSessionUsageEvent(user, ws, sessionId, startTime.getTime(), endTime.getTime() - startTime.getTime(), isFactory);
        }

        public static Builder createSessionStartedEvent(String user, String ws, String window, String sessionId) {
            return new Builder().withParam("EVENT", "session-started")
                                .withParam("SESSION-ID", sessionId)
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("WINDOW", window);
        }

        public static Builder createSessionFinishedEvent(String user, String ws, String window, String sessionId) {
            return new Builder().withParam("EVENT", "session-finished")
                                .withParam("SESSION-ID", sessionId)
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("WINDOW", window);
        }

        public static Builder createSessionStartedEventParamenters(String user, String ws, String window, String sessionId) {
            Map<String, String> map = new HashMap<>();
            map.put("SESSION-ID", sessionId);
            map.put("WINDOW", window);


            return new Builder().withParam("EVENT", "session-started")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PARAMETERS", map);
        }

        public static Builder createSessionFinishedEventParameters(String user, String ws, String window, String sessionId) {
            Map<String, String> map = new HashMap<>();
            map.put("SESSION-ID", sessionId);
            map.put("WINDOW", window);

            return new Builder().withParam("EVENT", "session-finished")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PARAMETERS", map);
        }

        public static Builder createRunStartedEvent(String user, String ws, String project, String type, String id) {
            return new Builder().withParam("EVENT", "run-started")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type)
                                .withParam("ID", id);
        }

        public static Builder createBuildStartedEvent(String user, String ws, String project, String type, String id) {
            return new Builder().withParam("EVENT", "build-started")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type)
                                .withParam("ID", id);
        }

        public static Builder createBuildQueueWaitingStartedEvent(String user, String ws, String project, String type,
                                                                  String id) {
            return new Builder().withParam("EVENT", "build-queue-waiting-started")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type)
                                .withParam("ID", id);
        }

        public static Builder createRunQueueWaitingStartedEvent(String user, String ws, String project, String type,
                                                                String id) {
            return new Builder().withParam("EVENT", "run-queue-waiting-started")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type)
                                .withParam("ID", id);
        }

        public static Builder createConfigureDockerStartedEvent(String user, String ws, String id) {
            return new Builder().withParam("EVENT", "configure-docker-started")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("ID", id);
        }

        public static Builder createDebugStartedEvent(String user, String ws, String project, String type, String id) {
            return new Builder().withParam("EVENT", "debug-started")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type)
                                .withParam("ID", id);
        }

        public static Builder createRunFinishedEvent(String user, String ws, String project, String type, String id, long usageTime,
                                                     long stoppedByUser) {
            return new Builder().withParam("EVENT", "run-finished")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type)
                                .withParam("ID", id)
                                .withParam("USAGE-TIME", Long.toString(usageTime))
                                .withParam("STOPPED-BY-USER", Long.toString(stoppedByUser));
        }

        public static Builder createBuildFinishedEvent(String user, String ws, String project, String type, String id, long usageTime) {
            return new Builder().withParam("EVENT", "build-finished")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type)
                                .withParam("ID", id)
                                .withParam("USAGE-TIME", Long.toString(usageTime));
        }

        public static Builder createConfigureDockerFinishedEvent(String user, String ws, String id) {
            return new Builder().withParam("EVENT", "configure-docker-finished")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("ID", id);
        }

        public static Builder createUserInviteEvent(String user, String ws, String email) {
            return new Builder().withParam("EVENT", "user-invite")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("EMAIL", email);
        }

        public static Builder createApplicationCreatedEvent(String user,
                                                            String ws,
                                                            String project,
                                                            String type,
                                                            String paas) {
            return new Builder().withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("EVENT", "application-created")
                                .withParam("PROJECT", project).withParam("TYPE", type).withParam("PAAS", paas);
        }

        public static Builder createProjectDeployedEvent(String user,
                                                         String ws,
                                                         String project,
                                                         String type,
                                                         String paas) {
            return new Builder().withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("EVENT", "project-deployed")
                                .withParam("PROJECT", project).withParam("TYPE", type).withParam("PAAS", paas);
        }

        public static Builder createUserAddedToWsEvent(String user,
                                                       String ws,
                                                       String from) {
            return new Builder().withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("EVENT", "user-added-to-ws")
                                .withParam("FROM", from);
        }

        public static Builder createUserSSOLoggedOutEvent(String user) {
            return new Builder().withParam("EVENT", "user-sso-logged-out").withParam("USER", user);
        }

        public static Builder createUserSSOLoggedInEvent(String user, String using) {
            return new Builder().withParam("EVENT", "user-sso-logged-in").withParam("USING", using)
                                .withParam("USER", user);
        }

        public static Builder createProjectCreatedEvent(String user,
                                                        String ws,
                                                        String project,
                                                        String type) {
            return new Builder().withParam("USER", user)
                                .withParam("WS", ws)
                                .withParam("EVENT", "project-created")
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type);
        }

        public static Builder createUserCreatedEvent(String userId,
                                                     String user,
                                                     String aliases) {
            return new Builder().withParam("EVENT", "user-created")
                                .withParam("USER-ID", userId)
                                .withParam("USER", user)
                                .withParam("EMAILS", aliases);
        }

        public static Builder createWorkspaceCreatedEvent(String wsId, String ws,
                                                          String user) {
            return new Builder().withParam("EVENT", "workspace-created")
                                .withParam("WS", ws)
                                .withParam("WS-ID", wsId)
                                .withParam("USER", user);
        }

        public static Builder createUserUpdatedEvent(String userId,
                                                     String user,
                                                     String aliases) {
            return new Builder().withParam("EVENT", "user-updated")
                                .withParam("USER", user)
                                .withParam("USER-ID", userId)
                                .withParam("EMAILS", aliases);
        }

        public static Builder createIDEUsageEvent(String user,
                                                  String ws,
                                                  String action,
                                                  String source,
                                                  String project,
                                                  String type,
                                                  String parameters) {
            Builder builder = new Builder().withParam("EVENT", "ide-usage");
            builder = addIfNotNull(builder, "WS", ws);
            builder = addIfNotNull(builder, "USER", user);
            builder = addIfNotNull(builder, "ACTION", action);
            builder = addIfNotNull(builder, "SOURCE", source);
            builder = addIfNotNull(builder, "PROJECT", project);
            builder = addIfNotNull(builder, "TYPE", type);
            builder = addIfNotNull(builder, "PARAMETERS", parameters);

            return builder;
        }


        public static Builder createUserChangedNameEvent(String oldUser,
                                                         String newUser) {
            return new Builder().withParam("EVENT", "user-changed-name")
                                .withParam("OLD-USER", oldUser)
                                .withParam("NEW-USER", newUser);
        }


        public static Builder createUserUpdateProfile(String userId,
                                                      String aliases,
                                                      String user,
                                                      String firstName,
                                                      String lastName,
                                                      String company,
                                                      String phone,
                                                      String jobTitle) {
            return new Builder().withParam("EVENT", "user-update-profile")
                                .withParam("USER-ID", userId)
                                .withParam("USER", user)
                                .withParam("EMAILS", aliases)
                                .withParam("FIRSTNAME", firstName)
                                .withParam("LASTNAME", lastName)
                                .withParam("COMPANY", company)
                                .withParam("PHONE", phone)
                                .withParam("JOBTITLE", jobTitle);
        }

        public static Builder createFactoryCreatedEvent(String ws,
                                                        String user,
                                                        String project,
                                                        String type,
                                                        String repoUrl,
                                                        String factoryUrl,
                                                        String orgId,
                                                        String affiliateId) {
            return new Builder().withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("EVENT", "factory-created")
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type)
                                .withParam("REPO-URL", repoUrl)
                                .withParam("FACTORY-URL", factoryUrl)
                                .withParam("ORG-ID", orgId)
                                .withParam("AFFILIATE-ID", affiliateId);
        }

        public static Builder createSessionFactoryStartedEvent(String sessionId,
                                                               String tempWs,
                                                               String tempUser,
                                                               String auth,
                                                               String userAgent) {
            return new Builder().withParam("EVENT", "session-factory-started")
                                .withParam("SESSION-ID", sessionId)
                                .withParam("WS", tempWs)
                                .withParam("USER", tempUser)
                                .withParam("AUTHENTICATED", auth)
                                .withParam("USER-AGENT", userAgent);

        }

        public static Builder createFactoryProjectImportedEvent(String ws,
                                                                String user,
                                                                String project,
                                                                String type) {
            return new Builder().withParam("EVENT", "factory-project-imported")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type);

        }

        public static Builder createSessionFactoryStoppedEvent(String sessionId,
                                                               String tempWs,
                                                               String tempUser) {
            return new Builder().withParam("EVENT", "session-factory-stopped")
                                .withParam("SESSION-ID", sessionId)
                                .withParam("WS", tempWs)
                                .withParam("USER", tempUser);
        }

        public static Builder createFactoryUrlAcceptedEvent(String tempWs,
                                                            String factoryUrl,
                                                            String referrerUrl,
                                                            String orgId,
                                                            String affiliateId) {
            return new Builder().withParam("EVENT", "factory-url-accepted")
                                .withParam("WS", tempWs)
                                .withParam("REFERRER", referrerUrl)
                                .withParam("FACTORY-URL", factoryUrl)
                                .withParam("ORG-ID", orgId)
                                .withParam("AFFILIATE-ID", affiliateId);
        }

        public static Builder collaborativeSessionStartedEvent(String ws, String userId, String sessionId) {

            return new Builder().withParam("EVENT", "collaborative-session-started")
                                .withParam("WS", ws)
                                .withParam("USER", userId)
                                .withParam("ID", sessionId);
        }

        public static Builder buildQueueTerminatedEvent(String ws, String user, String project, String type,
                                                        String uuid) {

            return new Builder().withParam("EVENT", "build-queue-terminated")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type)
                                .withParam("ID", uuid);
        }

        public static Builder buildRunQueueTerminatedEvent(String ws, String user, String project, String type,
                                                           String uuid) {

            return new Builder().withParam("EVENT", "run-queue-terminated")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type)
                                .withParam("ID", uuid);
        }

        private static Builder addIfNotNull(Builder builder, String param, String value) {
            if (value != null) {
                return builder.withParam(param, value);
            } else {
                return builder;
            }
        }

        public static Builder createProjectDestroyedEvent(String user,
                                                          String ws,
                                                          String project,
                                                          String type) {
            return new Builder().withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("EVENT", "project-destroyed")
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type);
        }

        public static Builder createDebugFinishedEvent(String user, String ws, String project, String type, String id) {
            return new Builder().withParam("EVENT", "debug-finished")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type)
                                .withParam("ID", id);
        }
    }
}
