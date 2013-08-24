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

SS = extractEventsWithSessionId(t, 'session-started');
SF = extractEventsWithSessionId(t, 'session-finished');

j1 = JOIN SS BY sId FULL, SF BY sId;
j2 = removeEmptyField(j1, 'SS::sId');
j3 = removeEmptyField(j2, 'SF::sId');
j = FOREACH j3 GENERATE SS::ws AS ws, SS::user, SS::sId AS dt, SecondsBetween(SF::dt, SS::dt) AS delta;

profiles = LOAD '$LOAD_DIR' USING PigStorage() AS (user : chararray, firstName: chararray, lastName: chararray, company: chararray, phone : chararray, job : chararray);
c1 = JOIN j BY user LEFT, profiles BY user;
c2 = removeEmptyField(c1, 'j2::company');
c = FOREACH c2 GENERATE j2::company AS company, j::delta AS delta;

r = GROUP c BY company;
result = FOREACH r GENERATE group, TOBAG(SUM(R1.delta) / 60, COUNT(R1.delta));