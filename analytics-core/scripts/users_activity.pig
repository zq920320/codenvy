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

IMPORT 'macros.pig';

l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

a1 = removeEvent(l, 'session-usage,session-factory-usage');
a = FOREACH a1 GENERATE dt, user, ws, event, message;

-- Every parameter in the message will be stored separately as well as a whole message.
-- It depends on 'event', that's why message must be passed to the storage function after an event
result = FOREACH a GENERATE UUID(),
                            TOTUPLE('date', ToMilliSeconds(dt)),
                            TOTUPLE('event', event),
                            TOTUPLE('action', EventDescription(event)),
                            TOTUPLE('ws', NullToEmpty(ws)),
                            TOTUPLE('user', NullToEmpty(user)),
                            TOTUPLE('message', message);

STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
