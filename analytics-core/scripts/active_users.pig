-----------------------------------------------------------------------------
-- Finds list of active users.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$fromDate', '$toDate');
fR = removeEvent(f2, 'user-sso-logged-out');

a1 = extractUser(fR);
result = FOREACH a1 GENERATE user;
