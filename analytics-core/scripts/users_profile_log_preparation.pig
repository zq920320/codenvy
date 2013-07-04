IMPORT 'macros.pig';

f1 = loadResources('$log');
f = filterByDate(f1, '$FROM_DATE', '$TO_DATE');

a2 = filterByEvent(f, 'user-update-profile');
a3 = extractUser(a2);
a4 = smartExtractParam(a3, 'FIRSTNAME', 'firstName');
a5 = smartExtractParam(a4, 'LASTNAME', 'lastName');
a6 = smartExtractParam(a5, 'COMPANY', 'company');
a = FOREACH a6 GENERATE user, firstName, lastName, company, MilliSecondsBetween(dt, ToDate('2010-01-01', 'yyyy-MM-dd')) AS delta;

b1 = LOAD '$RESULT_DIR/PREV_PROFILES' USING PigStorage() AS (user : chararray, firstName: chararray, lastName: chararray, company: chararray);
b = FOREACH b1 GENERATE *, 0 AS delta;

c = UNION a, b;
d = lastUserProfileUpdate(c);

STORE d INTO '$RESULT_DIR/PROFILES' USING PigStorage();


