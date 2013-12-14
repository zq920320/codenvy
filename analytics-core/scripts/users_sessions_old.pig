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

f1 = productUsageTimeList(t, '10');
f = FOREACH f1 GENERATE '' AS id, *;

result = FOREACH f GENERATE ToMilliSeconds(dt), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('session_id', id),
            TOTUPLE('start_time', ToString(dt, 'yyyy-MM-dd HH:mm:sss')),
            TOTUPLE('end_time', ToString(ToDate(ToMilliSeconds(dt) + delta * 1000), 'yyyy-MM-dd HH:mm:sss')),
            TOTUPLE('time', delta);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');


