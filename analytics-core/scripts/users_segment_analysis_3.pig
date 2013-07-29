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

%DEFAULT condition       'count > 5 AND time > 300 * 60';

%DEFAULT inactiveInterval '10';  -- in minutes

a1 = LOAD '$RESULT_DIR/LOG' USING PigStorage() AS (ws: chararray, user: chararray, dt: datetime);
a2 = productUsageTimeList(a1, '$inactiveInterval');

s1 = GROUP a2 ALL;
s = FOREACH s1 GENERATE $0;

a3 = calculateCondition(a2, '$condition', '$TO_DATE', s);
result = FOREACH a3 GENERATE TOTUPLE(TOTUPLE($0), TOTUPLE($1), TOTUPLE($2), TOTUPLE($3), TOTUPLE($4), TOTUPLE($5));
