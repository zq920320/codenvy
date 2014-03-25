/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
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

package com.codenvy.analytics.pig.scripts.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/** @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a> */
public class Event {
    private final String              date;
    private final String              time;
    private final EventContext        context;
    private final boolean             isIDE3Event;
    private final Map<String, String> params;

    /**
     * Event constructor. {@link EventContext} parameters could be null. It means they'll be omitted in the resulted
     * message. The same true and for date parameter;
     */
    private Event(String date, String time, EventContext context, Map<String, String> params, boolean isIDE3Event) {
        this.date = date;
        this.time = time;
        this.context = context;
        this.params = params;
        this.isIDE3Event = isIDE3Event;
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

        if (isIDE3Event) {
            builder.append("[ide3]");
        }

        if (context.user != null) {
            builder.append("[");
            builder.append(context.user);
            builder.append("]");
        }

        if (context.ws != null) {
            builder.append("[");
            builder.append(context.ws);
            builder.append("]");
        }

        if (context.session != null) {
            builder.append("[");
            builder.append(context.session);
            builder.append("]");
        }

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
        private EventContext        context = new EventContext();
        private Map<String, String> params  = new LinkedHashMap<>();

        public Builder withContext(String user, String ws, String session) {
            context = new EventContext(user, ws, session);
            return this;
        }

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

        public Event build() {
            return new Event(date, time, context, params, false);
        }

        public Event buildIDE3Event() {
            return new Event(date, time, context, params, true);
        }

        public static Builder createTenantCreatedEvent(String ws, String user) {
            return new Builder().withParam("EVENT", "tenant-created").withParam("WS", ws).withParam("USER", user);
        }

        public static Builder createProjectBuiltEvent(String user, String ws, String session, String project,
                                                      String type) {
            return new Builder().withContext(user, ws, session).withParam("EVENT", "project-built")
                                .withParam("PROJECT", project).withParam("TYPE", type);
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

        public static Builder createRunFinishedEvent(String user, String ws, String project, String type, String id) {
            return new Builder().withParam("EVENT", "run-finished")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type)
                                .withParam("ID", id);
        }

        public static Builder createBuildFinishedEvent(String user, String ws, String project, String type, String id) {
            return new Builder().withParam("EVENT", "build-finished")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type)
                                .withParam("ID", id);
        }

        public static Builder createBuildQueueWaitingFinishedEvent(String user, String ws, String project, String type,
                                                                   String id) {
            return new Builder().withParam("EVENT", "build-queue-waiting-finished")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type)
                                .withParam("ID", id);
        }

        public static Builder createRunQueueWaitingFinishedEvent(String user, String ws, String project, String type,
                                                                 String id) {
            return new Builder().withParam("EVENT", "run-queue-waiting-finished")
                                .withParam("WS", ws)
                                .withParam("USER", user)
                                .withParam("PROJECT", project)
                                .withParam("TYPE", type)
                                .withParam("ID", id);
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

        public static Builder createApplicationCreatedEvent(String user, String ws, String session, String project,
                                                            String type,
                                                            String paas) {
            return new Builder().withContext(user, ws, session)
                                .withParam("EVENT", "application-created")
                                .withParam("PROJECT", project).withParam("TYPE", type).withParam("PAAS", paas);
        }

        public static Builder createProjectDeployedEvent(String user, String ws, String session, String project,
                                                         String type, String paas) {
            return new Builder().withContext(user, ws, session)
                                .withParam("EVENT", "project-deployed")
                                .withParam("PROJECT", project).withParam("TYPE", type).withParam("PAAS", paas);
        }

        public static Builder createUserAddedToWsEvent(String user,
                                                       String ws,
                                                       String session,
                                                       String wsParam,
                                                       String userParam,
                                                       String from) {
            return new Builder().withContext(user, ws, session)
                                .withParam("EVENT", "user-added-to-ws")
                                .withParam("WS", wsParam)
                                .withParam("USER", userParam)
                                .withParam("FROM", from);
        }

        public static Builder createUserSSOLoggedOutEvent(String user) {
            return new Builder().withParam("EVENT", "user-sso-logged-out").withParam("USER", user);
        }

        public static Builder createUserSSOLoggedInEvent(String user, String using) {
            return new Builder().withParam("EVENT", "user-sso-logged-in").withParam("USING", using)
                                .withParam("USER", user);
        }

        public static Builder createProjectCreatedEvent(String user, String ws, String session, String project,
                                                        String type) {
            return new Builder().withContext(user, ws, session).withParam("EVENT", "project-created")
                                .withParam("PROJECT", project).withParam("TYPE", type);
        }

        public static Builder createUserCreatedEvent(String userId,
                                                     String aliases) {
            return new Builder().withParam("EVENT", "user-created")
                                .withParam("USER-ID", userId)
                                .withParam("ALIASES", aliases);
        }

        public static Builder createUserChangedNameEvent(String oldUser,
                                                         String newUser) {
            return new Builder().withParam("EVENT", "user-changed-name")
                                .withParam("OLD-USER", oldUser)
                                .withParam("NEW-USER", newUser);
        }


        public static Builder createUserUpdateProfile(String user,
                                                      String firstName,
                                                      String lastName,
                                                      String company,
                                                      String phone,
                                                      String jobTitle) {
            return new Builder().withParam("EVENT", "user-update-profile")
                                .withParam("USER", user)
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
            return new Builder().withContext(user, ws, UUID.randomUUID().toString())
                                .withParam("EVENT", "factory-created")
                                .withParam("WS", ws)
                                .withParam("USER", user)
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

        public static Builder createUserEvent(String user, String action) {
            Builder builder = new Builder().withParam("EVENT", "users-events")
                                           .withParam("ACTION", action)
                                           .withParam("USER", user);

            return builder;
        }

        public static Builder collaborativeSessionStartedEvent(String ws, String userId, String sessionId) {
            Builder builder = new Builder().withParam("EVENT", "collaborative-session-started")
                                           .withParam("WS", ws)
                                           .withParam("USER", userId)
                                           .withParam("ID", sessionId);

            return builder;
        }

        public static Builder buildQueueTerminatedEvent(String ws, String user, String project, String type, String uuid) {
            Builder builder = new Builder().withParam("EVENT", "build-queue-terminated")
                                           .withParam("WS", ws)
                                           .withParam("USER", user)
                                           .withParam("PROJECT", project)
                                           .withParam("TYPE", type)
                                           .withParam("ID", uuid);

            return builder;
        }

        public static Builder buildRunQueueTerminatedEvent(String ws, String user, String project, String type, String uuid) {
            Builder builder = new Builder().withParam("EVENT", "run-queue-terminated")
                                           .withParam("WS", ws)
                                           .withParam("USER", user)
                                           .withParam("PROJECT", project)
                                           .withParam("TYPE", type)
                                           .withParam("ID", uuid);

            return builder;
        }

        public static Builder createShellLaunchedEvent(String user, String ws, String session) {
            return new Builder().withContext(user, ws, session).withParam("EVENT", "shell-launched");
        }

    }

    /** Event context contains 3 parameters. */
    static private class EventContext {
        private final String user;
        private final String ws;
        private final String session;

        EventContext() {
            this(null, null, null);
        }

        private EventContext(String user, String ws, String session) {
            this.user = user;
            this.ws = ws;
            this.session = session;
        }
    }
}
