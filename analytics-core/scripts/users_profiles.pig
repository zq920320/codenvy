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

l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

----------------------------------------------------------------------------------
---------------------------- user creation processing ----------------------------
----------------------------------------------------------------------------------
a1 = filterByEvent(l, 'user-created');
a2 = extractParam(a1, 'USER-ID', 'userId');
a3 = extractParam(a2, 'EMAILS', 'emails');
a4 = extractParam(a3, 'ALIASES', 'aliases');
a = FOREACH a4 GENERATE dt,
                        userId,
                        (emails IS NOT NULL ? (emails == '[]' OR emails == '' ? userName : emails )
                                            : (aliases IS NOT NULL ? aliases : userName)) AS emails;

resultA = FOREACH a GENERATE LOWER(userId),
                             TOTUPLE('date', ToMilliSeconds(dt)),
                             TOTUPLE('aliases', EnsureBrackets(LOWER(emails)));
STORE resultA INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;


----------------------------------------------------------------------------------
---------------------------- user profile updating processing --------------------
----------------------------------------------------------------------------------
d1 = filterByEvent(l, 'user-update-profile');
d2 = extractParam(d1, 'FIRSTNAME', 'firstName');
d3 = extractParam(d2, 'LASTNAME', 'lastName');
d4 = extractParam(d3, 'COMPANY', 'company');
d5 = extractParam(d4, 'PHONE', 'phone');
d6 = extractParam(d5, 'JOBTITLE', 'job');
d7 = extractParam(d6, 'USER-ID', 'userId');
d = FOREACH d7 GENERATE dt,
                        (userId IS NULL ? ReplaceUserWithId(user) : userId) AS userId,
                        NullToEmpty(firstName) AS firstName,
                        NullToEmpty(lastName) AS lastName,
                        NullToEmpty(company) AS company,
                        NullToEmpty(phone) AS phone,
                        NullToEmpty(FixJobTitle(job)) AS job;
e1 = lastUpdate(d, 'userId');
e = FOREACH e1 GENERATE d::userId AS userId,
                        d::firstName AS firstName,
                        d::lastName AS lastName,
                        d::company AS company,
                        d::phone AS phone,
                        d::job  AS job;

resultE = FOREACH e GENERATE LOWER(userId),
                             TOTUPLE('user_first_name', firstName),
                             TOTUPLE('user_last_name', lastName),
                             TOTUPLE('user_company', company),
                             TOTUPLE('user_phone', phone),
                             TOTUPLE('user_job', job);
STORE resultE INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

----------------------------------------------------------------------------------
---------------------------- user updating processing ----------------------------
----------------------------------------------------------------------------------
b1 = filterByEvent(l, 'user-updated,user-update-profile');
b2 = extractParam(b1, 'USER-ID', 'userId');
b3 = extractParam(b2, 'EMAILS', 'emails');
b = FOREACH b3 GENERATE dt,
                        (userId IS NULL ? ReplaceUserWithId(user) : userId) AS userId,
                        (emails IS NOT NULL ? emails : userName) AS emails;

c1 = lastUpdate(b, 'userId');
c = FOREACH c1 GENERATE b::userId AS userId,
                        b::emails AS emails;

resultC = FOREACH c GENERATE LOWER(userId),
                             TOTUPLE('aliases', EnsureBrackets(LOWER(emails)));
STORE resultC INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
