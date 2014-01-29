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
package com.codenvy.analytics.pig.udf;

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.pig.scripts.EventsHolder;

import org.apache.pig.FilterFunc;
import org.apache.pig.data.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class EventExists extends FilterFunc {

    private static final Logger LOG = LoggerFactory.getLogger(EventExists.class);


    private final EventsHolder eventsHolder;

    public EventExists() {
        eventsHolder = Injector.getInstance(EventsHolder.class);
    }

    @Override
    public Boolean exec(Tuple input) throws IOException {
        String eventNames = (String)input.get(0);

        if (!eventNames.equals("*")) {
            for (String event : eventNames.split(",")) {
                if (!eventsHolder.isEventExists(event)) {
                    String msg = "Unknown event " + event;
                    LOG.error(msg);
                    throw new IOException(msg);
                }
            }
        }

        return true;
    }
}
