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

f = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');
g = GROUP f BY user;

--------------------------------------------------------------------
-- Calculates projects created, built and deployed numbers
--------------------------------------------------------------------
a = FOREACH g {
    pCreated = FILTER f BY INDEXOF('project-created', event, 0) >= 0;
    pBuilt = FILTER f BY INDEXOF('project-built,application-created,project-deployed', event, 0) >= 0;
    pDeployed = FILTER f BY INDEXOF('application-created,project-deployed', event, 0) >= 0;

    GENERATE group AS user, COUNT(pCreated) AS pCreatedNum, COUNT(pBuilt) AS pBuiltNum, COUNT(pDeployed) AS pDeployedNum;
}

--------------------------------------------------------------------
-- Calculates time usage numbers
--------------------------------------------------------------------
b1 = joinEventsWithSameId(f, 'session-started', 'session-finished');
b2 = GROUP b1 BY user;
b = FOREACH b2 GENERATE group AS user, SUM(b1.delta) AS delta;

--------------------------------------------------------------------
-- Unions two results
--------------------------------------------------------------------
c1 = JOIN a BY user LEFT, b BY user;
c = FOREACH c1 GENERATE a::user AS user, a::pCreatedNum AS pCreatedNum, a::pBuiltNum AS pBuiltNum, a::pDeployedNum AS pDeployedNum, ((b::user IS NULL ? 0 : b::delta) / 60) AS time;

r1 = GROUP c BY user;
r2 = FOREACH r1 GENERATE group, FLATTEN(c);
result = FOREACH r2 GENERATE group, TOBAG(pCreatedNum, pBuiltNum, pDeployedNum, time);




