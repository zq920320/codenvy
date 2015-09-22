/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2015] Codenvy, S.A.
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

-- get create users record
cu = filterByEvent(l, 'user-created');

-- get logged in users record
uli = filterByEvent(l, 'user-sso-logged-in');
uli = extractParam(uli, 'USING', param);
uli = FOREACH uli GENERATE dt, ws, user, LOWER(param) AS param, message;
uli = FILTER uli BY param IS NOT NULL AND param != '';

-- stay only first login of each user
uli_group = GROUP uli BY user;
first_uli = FOREACH uli_group {
        sorted = ORDER uli BY dt;
        first  = LIMIT sorted 1;
        GENERATE group, FLATTEN(first);
};

-- add oauth type info ('using' field) to the created user cortege
cu_uli = JOIN cu BY user LEFT OUTER, first_uli BY user;  /* OUTER because there could be user-created events without user-sso-logged-in after followed by they */
cu_uli_table = FOREACH cu_uli GENERATE UUID(),
                                       TOTUPLE('date', ToMilliSeconds(cu::dt)),
                                       TOTUPLE('user', cu::user),
                                       TOTUPLE('ws', cu::ws),
                                       TOTUPLE('using', first_uli::first::param),
                                       TOTUPLE('event', cu::event),
                                       TOTUPLE('params-only-remove-event', cu::message),
                                       TOTUPLE('value', 1L);

STORE cu_uli_table INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
