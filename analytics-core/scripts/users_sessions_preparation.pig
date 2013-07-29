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

f1 = loadResources('$log');
f2 = filterByDate(f1, '$FROM_DATE', '$TO_DATE');
f3 = extractUser(f2);
f = extractWs(f3);

r1 = productUsageTimeList(f, '$inactiveInterval');

r2 = GROUP r1 BY user;
r = FOREACH r2 {
    t = FOREACH r1 GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(dt), TOTUPLE(delta));
    GENERATE group, t;
    }

result = FOREACH r GENERATE group, t;

