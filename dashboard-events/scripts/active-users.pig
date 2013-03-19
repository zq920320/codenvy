-----------------------------------------------------------------------------
-- Finds amount of active users in time frame.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the time frame
-- toDate     - ending of the time frame
---------------------------------------------------------------------------
IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$date', '$toDate');
fR = skipEvent(f2, 'user-sso-logged-out');

a1 = extractUser(fR);
a2 = FOREACH a1 GENERATE user;
aR = DISTINCT a2;

r1 = countAll(aR);
result = FOREACH r1 GENERATE '$date', '$toDate', *;

DUMP result;