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
  y2 = FOREACH y1 GENERATE *, MAX($X.delta) AS maxDelta;
  y3 = FOREACH y2 GENERATE group AS user, maxDelta, FLATTEN($X);
  y4 = FILTER y3 BY delta == maxDelta;
  $Y = FOREACH y4 GENERATE user, $X::firstName AS firstName, $X::lastName AS lastName, $X::company AS company, $X::phone AS phone, $X::job AS job;
};

f = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

a2 = filterByEvent(f, 'user-update-profile');
a4 = extractParam(a2, 'FIRSTNAME', 'firstName');
a5 = extractParam(a4, 'LASTNAME', 'lastName');
a6 = extractParam(a5, 'COMPANY', 'company');
a7 = extractParam(a6, 'PHONE', 'phone');
a8 = extractParam(a7, 'JOBTITLE', 'job');
a9 = FOREACH a8 GENERATE user, firstName, lastName, company, phone, job, MilliSecondsBetween(dt, ToDate('2010-01-01', 'yyyy-MM-dd')) AS delta;
a = FOREACH a9 GENERATE user, (firstName == 'null' OR firstName IS NULL ? '' : firstName) AS firstName,
			    (lastName == 'null' OR lastName IS NULL ? '' : lastName) AS lastName,
			    (company == 'null' OR company IS NULL ? '' : company) AS company,
			    (phone == 'null' OR phone IS NULL ? '' : phone) AS phone,
			    (job == 'null' OR job IS NULL ? '' : job) AS job,
			    delta;

b1 = LOAD '$LOAD_DIR' USING PigStorage() AS (user : chararray, firstName: chararray, lastName: chararray, company: chararray, phone : chararray, job : chararray);
b = FOREACH b1 GENERATE *, 0 AS delta;

c = UNION a, b;
d = lastUserProfileUpdate(c);

STORE d INTO '$STORE_DIR' USING PigStorage();

r1 = lastUserProfileUpdate(a);
r2 = GROUP r1 BY user;
r3 = FOREACH r2 GENERATE group, FLATTEN(r1);
result = FOREACH r3 GENERATE group, TOBAG(user, firstName, lastName, company, phone, job);
