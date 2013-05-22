/*
 *    Copyright (C) 2013 eXo Platform SAS.
 *
 *    This is free software; you can redistribute it and/or modify it
 *    under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation; either version 2.1 of
 *    the License, or (at your option) any later version.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this software; if not, write to the Free
 *    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *    02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.codenvy.analytics.scripts.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/** @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a> */
public class Event {
    private final Map<String, String> params;

    private final EventContext context;

    private final String date;

    private final String time;

    /**
     * Event constructor. {@link EventContext} parameters could be null. It means they'll be omitted in the resulted message. The same true
     * and for date parameter;
     */
    private Event(String date, String time, EventContext context, Map<String, String> params) {
        this.date = date;
        this.time = time;
        this.context = context;
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
        private Map<String, String> params = new LinkedHashMap<String, String>();

        private EventContext context = new EventContext();

        private String date;

        private String time;

        public Builder withContext(String user, String ws, String session) {
            context = new EventContext(user, ws, session);
            return this;
        }

        public Builder withDate(String date) {
            this.date = date;
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
            return new Event(date, time, context, params);
        }

        /** Create 'tenant-created' event. */
        public static Builder createTenantCreatedEvent(String ws, String user) {
            return new Builder().withParam("EVENT", "tenant-created").withParam("WS", ws).withParam("USER", user);
        }

        /** Create 'user-created' event. */
        public static Builder createUserCreatedEvent(String userId, String aliases) {
            return new Builder().withParam("EVENT", "user-created").withParam("USER-ID", userId)
                                .withParam("ALIASES", "[" + aliases + "]");
        }

        public static Builder createUserRemovedEvent(String user) {
            return new Builder().withParam("EVENT", "user-removed").withParam("USER", user);
        }

        /** Create 'tenant-destroyed' event. */
        public static Builder createTenantDestroyedEvent(String ws) {
            return new Builder().withParam("EVENT", "tenant-destroyed").withParam("WS", ws);
        }

        /** Create 'tenant-stopped' event. */
        public static Builder createTenantStoppedEvent(String ws) {
            return new Builder().withParam("EVENT", "tenant-stopped").withParam("WS", ws);
        }

        /** Create 'project-created' event. */
        public static Builder createProjectCreatedEvent(String user, String ws, String session, String project) {
            return new Builder().withContext(user, ws, session).withParam("EVENT", "project-created")
                                .withParam("PROJECT", project);
        }

        /** Create 'project-destroyed' event. */
        public static Builder createProjectDestroyedEvent(String user, String ws, String session, String project) {
            return new Builder().withContext(user, ws, session).withParam("EVENT", "project-destroyed")
                                .withParam("PROJECT", project);
        }

        /** Create 'project-built' event. */
        public static Builder createProjectBuiltEvent(String user, String ws, String session, String project, String type) {
            return new Builder().withContext(user, ws, session).withParam("EVENT", "project-built")
                                .withParam("PROJECT", project).withParam("TYPE", type);
        }

        /** Create 'application-created' event. */
        public static Builder createApplicationCreatedEvent(String user, String ws, String session, String project, String type,
                                                            String paas) {
            return new Builder().withContext(user, ws, session).withParam("EVENT", "application-created")
                                .withParam("PROJECT", project).withParam("TYPE", type).withParam("PAAS", paas);
        }

        /** Create 'project-deployed' event. */
        public static Builder createProjectDeployedEvent(String user, String ws, String session, String project,
                                                         String type, String paas) {
            return new Builder().withContext(user, ws, session).withParam("EVENT", "project-deployed")
                                .withParam("PROJECT", project).withParam("TYPE", type).withParam("PAAS", paas);
        }

        /** Create 'user-added-to-ws' event. */
        public static Builder createUserAddedToWsEvent(String user, String ws, String session, String wsParam,
                                                       String userParam, String from) {
            return new Builder().withContext(user, ws, session).withParam("EVENT", "user-added-to-ws")
                                .withParam("WS", wsParam).withParam("USER", userParam).withParam("FROM", from);
        }

        public static Builder createUserSSOLoggedOutEvent(String user) {
            return new Builder().withParam("EVENT", "user-sso-logged-out").withParam("USER", user);
        }

        public static Builder createUserSSOLoggedInEvent(String user, String using) {
            return new Builder().withParam("EVENT", "user-sso-logged-in").withParam("USING", using)
                                .withParam("USER", user);
        }

        public static Builder createUserInviteEvent(String user, String ws, String session, String email) {
            return new Builder().withContext(user, ws, session).withParam("EVENT", "user-invite")
                                .withParam("EMAIL", email);
        }

        public static Builder createJRebelUsageEvent(String user, String ws, String session, String project, String type, boolean jrebel) {
            return new Builder().withContext(user, ws, session).withParam("EVENT", "jrebel-usage")
                                .withParam("PROJECT", project).withParam("TYPE", type).withParam("JREBEL", String.valueOf(jrebel));
        }

        public static Builder createProjectCreatedEvent(String user, String ws, String session, String project, String type) {
            return createProjectCreatedEvent(user, ws, session, project).withParam("TYPE", type);
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
