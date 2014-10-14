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
package com.codenvy.analytics.pig.udf;

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.pig.scripts.EventsHolder;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Anatoliy Bazko
 */
public class EventDescription extends EvalFunc<String> {

    private final EventsHolder eventsHolder;

    private static final Logger LOG = LoggerFactory.getLogger(EventDescription.class);

    public EventDescription() {
        eventsHolder = Injector.getInstance(EventsHolder.class);
    }

    @Override
    public String exec(Tuple input) throws IOException {
        String event = (String)input.get(0);
        try {
            return eventsHolder.getDescription(event);
        } catch (IllegalArgumentException e) {
            LOG.error(e.getMessage());
            return event;
        }
    }

    @Override
    public Schema outputSchema(Schema input) {
        return new Schema(new Schema.FieldSchema(getSchemaName(this.getClass().getName().toLowerCase(), input),
                                                 DataType.CHARARRAY));
    }
}
