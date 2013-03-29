---------------------------------------------------------------------------
-- Finds the list of users who logged-in in $lastMinutes
---------------------------------------------------------------------------
IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByLastMinutes(f1, '$lastMinutes');
fR = filterByEvent(f2, 'user-sso-logged-in');

a1 = extractUser(fR);
a2 = FOREACH a1 GENERATE user;
aR = DISTINCT a2;

r1 = GROUP aR ALL;
result = FOREACH r1 GENERATE aR;
DUMP result;
