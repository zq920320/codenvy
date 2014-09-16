/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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

%DEFAULT idleInterval '600000'; -- 10 min

---------------------------------------------------------------------------------------------
DEFINE addLogoutInterval(X, L, idleIntervalParam) RETURNS Y {
  z1 = filterByEvent($L, 'user-sso-logged-out');
  z = FOREACH z1 GENERATE dt, user;

  -- checking if logout event occurred after session
  x1 = JOIN $X BY user LEFT, z BY user;
  x2 = FOREACH x1 GENERATE *, (z::user IS NULL ? 0 : ToMilliSeconds(z::dt) - ($X::startTime + $X::usageTime)) AS delta;
  x3 = FOREACH x2 GENERATE *, (0 < delta AND delta < (long) $idleIntervalParam ? delta : 0) AS logoutInterval;
  $Y = FOREACH x3 GENERATE $X::dt AS dt,
                           $X::ws AS ws,
                           $X::user AS user,
                           $X::startTime AS startTime,
                           $X::usageTime AS usageTime,
                           $X::sessionID AS sessionID,
                           logoutInterval AS logoutInterval;
};

---------------------------------------------------------------------------------------------
l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

a1 = filterByEvent(l, '$EVENT');
a2 = removeEmptyField(a1, 'user');
a3 = extractParam(a2, 'SESSION-ID', sessionID);
a4 = extractParam(a3, 'START-TIME', startTime);
a5 = extractParam(a4, 'USAGE-TIME', usageTime);
a = FOREACH a5 GENERATE dt, ws, user, sessionID, (long) startTime, (long) usageTime;


b1 = lastUpdate(a, 'sessionID');
b = FOREACH b1 GENERATE a::startTime AS dt,
                        a::ws AS ws,
                        a::user AS user,
                        a::sessionID AS sessionID,
                        a::startTime AS startTime,
                        a::usageTime AS usageTime;

c = addLogoutInterval(b, l, '$idleInterval');
d = FOREACH c GENERATE ws,
                       user,
                       sessionID,
                       startTime,
                       (usageTime + logoutInterval) AS usageTime,
                       (startTime + usageTime + logoutInterval) AS endTime,
                       logoutInterval,
                       NullToEmpty(GetUserCompany(user)) AS userCompany,
                       NullToEmpty(GetDomain(user)) AS userEmailDomain;

result = FOREACH d GENERATE sessionID,
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
result2 = FOREACH d GENERATE sessionID,
                             TOTUPLE('date', startTime),
                             TOTUPLE('user', user),
                             TOTUPLE('ws', ws),
                             TOTUPLE('time', usageTime),
                             TOTUPLE('session_id', sessionID),
                             TOTUPLE('sessions', 1);
STORE result2 INTO '$STORAGE_URL.$STORAGE_TABLE_USERS_STATISTICS' USING MongoStorage;
