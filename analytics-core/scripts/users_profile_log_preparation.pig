IMPORT 'macros.pig';

f1 = loadResources('$log');
f = filterByDate(f1, '$FROM_DATE', '$TO_DATE');

a2 = filterByEvent(f, 'user-update-profile');
a3 = extractUser(a2);
a4 = extractParam(a3, 'FIRSTNAME', 'firstName');
a5 = extractParam(a4, 'LASTNAME', 'lastName');
a6 = extractParam(a5, 'COMPANY', 'company');
a7 = extractParam(a6, 'PHONE', 'phone');
a8 = extractParam(a7, 'JOBTITLE', 'job');
a = FOREACH a8 GENERATE user, firstName, lastName, company, phone, job, MilliSecondsBetween(dt, ToDate('2010-01-01', 'yyyy-MM-dd')) AS delta;

b1 = LOAD '$RESULT_DIR/PREV_PROFILES' USING PigStorage() AS (user : chararray, firstName: chararray, lastName: chararray, company: chararray, phone, job);
b = FOREACH b1 GENERATE *, 0 AS delta;

c = UNION a, b;
d = lastUserProfileUpdate(c);

STORE d INTO '$RESULT_DIR/PROFILES' USING PigStorage();


