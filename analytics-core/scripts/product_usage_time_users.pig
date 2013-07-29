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

lR = LOAD '$RESULT_DIR/LOG' USING PigStorage() AS (ws: chararray, user: chararray, dt: datetime);

r1 = filterByDateInterval(lR, '$TO_DATE', '$INTERVAL');
r2 = productUsageTimeList(r1, '$inactiveInterval');
r = usersByTimeSpent(r2);

STORE r INTO '$RESULT_DIR/$ENTITY/$INTERVAL' USING PigStorage();
result = FOREACH r GENERATE TOTUPLE(TOTUPLE(user), TOTUPLE(count), TOTUPLE(time));
