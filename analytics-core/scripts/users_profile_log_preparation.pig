IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByEvent(f1, 'user-update-profile');
f3 = extractUser(f2);
f4 = smartExtractParam(f3, 'FIRSTNAME', 'firstName');
f5 = smartExtractParam(f4, 'LASTNAME', 'lastName');
f6 = smartExtractParam(f5, 'COMPANY', 'company');
fR = FOREACH f6 GENERATE user, firstName, lastName, company, MilliSecondsBetween(dt, ToDate('2010-01-01', 'yyyy-MM-dd')) AS delta;

-------------------------------------
-- Finds the most last update
-------------------------------------
g1 = GROUP fR BY user;
g2 = FOREACH g1 GENERATE *, MAX(fR.delta) AS maxDelta;
g3 = FOREACH g2 GENERATE group AS user, maxDelta, FLATTEN(fR);
g4 = FILTER g3 BY delta == maxDelta;
g = FOREACH g4 GENERATE user, fR::firstName, fR::lastName, fR::company;

STORE g INTO '$resultDir/COMPANIES' USING PigStorage();

