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

---------------------------------------------------------------------------
-- Finds last updates user profile
-- @return {user : chararray, firstName : chararray, lastName : chararray, company : chararry}
---------------------------------------------------------------------------
DEFINE lastUserProfileUpdate(X) RETURNS Y {
  y1 = GROUP $X BY user;
  y2 = FOREACH y1 GENERATE *, MAX($X.dt) AS maxDt;
  y3 = FOREACH y2 GENERATE group AS user, maxDt, FLATTEN($X);
  y4 = FILTER y3 BY dt == maxDt;
  $Y = FOREACH y4 GENERATE user, $X::firstName AS firstName, $X::lastName AS lastName, $X::company AS company, $X::phone AS phone, $X::job AS job;
};

l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

a1 = filterByEvent(l, 'user-update-profile');
a2 = extractParam(a1, 'FIRSTNAME', 'firstName');
a3 = extractParam(a2, 'LASTNAME', 'lastName');
a4 = extractParam(a3, 'COMPANY', 'company');
a5 = extractParam(a4, 'PHONE', 'phone');
a6 = extractParam(a5, 'JOBTITLE', 'job');
a7 = FOREACH a6 GENERATE user, firstName, lastName, company, phone, job, dt;
a8 = FOREACH a7 GENERATE user, (firstName == 'null' OR firstName IS NULL ? '' : firstName) AS firstName,
			    (lastName == 'null' OR lastName IS NULL ? '' : lastName) AS lastName,
			    (company == 'null' OR company IS NULL ? '' : company) AS company,
			    (phone == 'null' OR phone IS NULL ? '' : phone) AS phone,
			    (job == 'null' OR job IS NULL ? '' : job) AS job, dt;
a = lastUserProfileUpdate(a8);

result = FOREACH a GENERATE user, TOTUPLE('user_email', user), TOTUPLE('user_first_name', firstName),
                        TOTUPLE('user_last_name', lastName), TOTUPLE('user_company', company),
                        TOTUPLE('user_phone', phone), TOTUPLE('user_job', job);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage();

stat = FOREACH a GENERATE user, TOTUPLE('user_email', user), TOTUPLE('user_first_name', firstName),
                TOTUPLE('user_last_name', lastName), TOTUPLE('user_company', company),
                TOTUPLE('user_phone', phone);
STORE stat INTO '$STORAGE_URL.$STORAGE_TABLE_USERS_STATISTICS' USING MongoStorage();
