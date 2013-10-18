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

DEFINE checkEvent(X, S, eventParam) RETURNS Y {
    x1 = filterByEvent($X, '$eventParam');
    x2 = GROUP x1 ALL;
    $Y = FOREACH $S GENERATE '$eventParam' AS event, (COUNT(x2.$1) > 0 ? 'generated' : NULL) AS status;
};

DEFINE checkEventAndParam(X, S, eventParam, paramName, paramValue) RETURNS Y {
    w1 = filterByEvent($X, '$eventParam');
    w2 = extractParam(w1, '$paramName', 'param'); 
    w3 = FILTER w2 BY param == '$paramValue';
    w4 = GROUP w3 ALL;
    $Y = FOREACH $S GENERATE CONCAT(CONCAT(CONCAT(CONCAT('$eventParam', ':'), '$paramName'), ':'), '$paramValue') AS event,  (COUNT(w4.$1) > 0 ? 'generated' : NULL) AS status;
};

lR = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

s1 = GROUP lR ALL;
s = FOREACH s1 GENERATE 'single';

r1 = checkEvent(lR, s, 'tenant-created');
r2 = checkEvent(lR, s, 'tenant-destroyed');
r3 = checkEvent(lR, s, 'tenant-started');
r4 = checkEvent(lR, s, 'tenant-stopped');

r5 = checkEvent(lR, s, 'project-created');
r6 = checkEvent(lR, s, 'project-destroyed');
r7 = checkEvent(lR, s, 'project-built');
r8 = checkEvent(lR, s, 'project-deployed');
r9 = checkEvent(lR, s, 'application-created');

r10 = checkEvent(lR, s, 'jrebel-usage');
r11 = checkEvent(lR, s, 'jrebel-user-profile-info');

r12 = checkEvent(lR, s, 'user-sso-logged-out');
r13 = checkEvent(lR, s, 'user-sso-logged-in');
r14 = checkEvent(lR, s, 'user-invite');
r15 = checkEvent(lR, s, 'user-added-to-ws');
r16 = checkEvent(lR, s, 'user-created');
r17 = checkEvent(lR, s, 'user-removed');

r18 = checkEvent(lR, s, 'shell-launched');
r19 = checkEvent(lR, s, 'user-code-refactor');
r20 = checkEvent(lR, s, 'user-code-complete');
r21 = checkEvent(lR, s, 'file-manipulation');

r22 = checkEvent(lR, s, 'build-started');
r23 = checkEvent(lR, s, 'build-finished');
r24 = checkEvent(lR, s, 'run-started');
r25 = checkEvent(lR, s, 'run-finished');
r26 = checkEvent(lR, s, 'debug-started');
r27 = checkEvent(lR, s, 'debug-finished');

r28 = checkEvent(lR, s, 'session-started');
r29 = checkEvent(lR, s, 'session-finished');

r30 = checkEvent(lR, s, 'factory-url-accepted');
r31 = checkEvent(lR, s, 'user-changed-name');
r32 = checkEvent(lR, s, 'factory-created');
r33 = checkEvent(lR, s, 'session-factory-started');
r34 = checkEvent(lR, s, 'session-factory-stopped');
r35 = checkEvent(lR, s, 'factory-project-imported');

a1 = UNION r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21,
            r22, r23, r24, r25, r26, r27, r28, r29, r30, r31, r32, r33, r34, r35;
a2 = FILTER a1 BY status IS NULL;
a = FOREACH a2 GENERATE event;

t1 = checkEventAndParam(lR, s, 'application-created', 'PAAS', 'CloudBees');
t2 = checkEventAndParam(lR, s, 'application-created', 'PAAS', 'AWS:BeansTalk');
t4 = checkEventAndParam(lR, s, 'application-created', 'PAAS', 'OpenShift');
t5 = checkEventAndParam(lR, s, 'project-deployed', 'PAAS', 'LOCAL');
t6 = checkEventAndParam(lR, s, 'application-created', 'PAAS', 'Heroku');
t7 = checkEventAndParam(lR, s, 'application-created', 'PAAS', 'Appfog');
t8 = checkEventAndParam(lR, s, 'application-created', 'PAAS', 'GAE');
t9 = checkEventAndParam(lR, s, 'application-created', 'PAAS', 'CloudFoundry');
t10 = checkEventAndParam(lR, s, 'application-created', 'PAAS', 'Tier3 Web Fabric');

b1 = UNION t1, t2, t4, t5, t6, t7, t8, t9, t10;
b2 = FILTER b1 BY status IS NULL;
b = FOREACH b2 GENERATE event;

r1 = UNION a, b;
result = FOREACH r1 GENERATE TOTUPLE(TOTUPLE(event));


