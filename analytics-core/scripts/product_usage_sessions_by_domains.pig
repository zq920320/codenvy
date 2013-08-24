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

t = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

SS = extractEventsWithSessionId(t, 'session-started');
SF = extractEventsWithSessionId(t, 'session-finished');

j1 = JOIN SS BY sId FULL, SF BY sId;
j2 = removeEmptyField(j1, 'SS::sId');
j3 = removeEmptyField(j2, 'SF::sId');
j4 = FOREACH j3 GENERATE SS::ws AS ws, REGEX_EXTRACT(SS::user, '.*@(.*)', 1) AS domain, SS::sId AS dt, SecondsBetween(SF::dt, SS::dt) AS delta;
j = removeEmptyField(j4, 'domain');

r1 = GROUP j BY domain;
result = FOREACH r1 {
    r2 = FOREACH j GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(domain), TOTUPLE(dt), TOTUPLE(delta));
    GENERATE group, r2;
}
