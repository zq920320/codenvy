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

IMPORT 'macros.pig';

%DEFAULT inactiveInterval '10';  -- in minutes

t = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');
j = combineSmallSessions(t, 'session-factory-started', 'session-factory-stopped');

-- get all 'session-factory-started' events with their 'id' and authenticated type
a1 = filterByEvent(t, 'session-factory-started');
a2 = extractParam(a1, 'AUTHENTICATED', 'authenticated');
a3 = extractParam(a2, 'SESSION-ID', 'id');
a = FOREACH a3 GENERATE id, authenticated;

-- keeps only the events of started sessions that match with combined ones
b1 = JOIN a BY id, j BY id;
b2 = FILTER b1 BY j::id IS NOT NULL;
b = FOREACH b2 GENERATE a::authenticated AS authenticated;

result = countByField(b, 'authenticated');

