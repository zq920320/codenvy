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

l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

a1 = filterByEvent(l, '$EVENT');
a2 = extractParam(a1, '$PARAM', param);
a = FOREACH a2 GENERATE param, event;

b1 = GROUP a BY param;
b = FOREACH b1 GENERATE group, COUNT(a) AS countAll;

result = FOREACH b GENERATE UUID(), TOTUPLE('date', '$TO_DATE'), TOTUPLE('type', group), TOTUPLE('value', countAll);
STORE result INTO '$CASSANDRA_STORAGE/$METRIC' USING CassandraStorage();
