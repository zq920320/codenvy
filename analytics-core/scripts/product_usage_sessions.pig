/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2015] Codenvy, S.A.
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

a1 = getSessions(l, '$EVENT');
a = FOREACH a1 GENERATE ws,
                        user,
                        sessionID,
                        startTime,
                        usageTime,
                        endTime,
                        logoutInterval,
                        NullToEmpty(GetUserCompany(user)) AS userCompany,
                        NullToEmpty(GetDomain(user)) AS userEmailDomain;

result = FOREACH a GENERATE sessionID,
                            TOTUPLE('date', startTime),
                            TOTUPLE('ws', ws),
                            TOTUPLE('user', user),
                            TOTUPLE('session_id', sessionID),
                            TOTUPLE('logout_interval', logoutInterval),
                            TOTUPLE('end_time', endTime),
                            TOTUPLE('time', usageTime),
                            TOTUPLE('domain', userEmailDomain),
                            TOTUPLE('user_company', userCompany);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

-- store sessions for users' statistics
result2 = FOREACH a GENERATE sessionID,
                             TOTUPLE('date', startTime),
                             TOTUPLE('user', user),
                             TOTUPLE('ws', ws),
                             TOTUPLE('time', usageTime),
                             TOTUPLE('session_id', sessionID),
                             TOTUPLE('sessions', 1);
STORE result2 INTO '$STORAGE_URL.$STORAGE_TABLE_USERS_STATISTICS' USING MongoStorage;
