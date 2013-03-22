-----------------------------------------------------------------------------
-- Finds amount of active users.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$fromDate', '$toDate');
fR = removeEvent(f2, 'user-sso-logged-out');

a1 = extractUser(fR);
a2 = FOREACH a1 GENERATE user;
aR = DISTINCT a2;

r1 = countAll(aR);
result = FOREACH r1 GENERATE '$fromDate', '$toDate', *;

DUMP result;