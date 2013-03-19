---------------------------------------------------------------------------
-- Finds amount of active workspaces in time frame.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the time frame
-- toDate     - ending of the time frame
---------------------------------------------------------------------------
IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$date', '$toDate');
f3 = skipEvent(f2, 'tenant-stopped');
f4 = skipEvent(f3, 'tenant-destroyed');
fR = skipEvent(f4, 'user-sso-logged-out');

a1 = extractWs(fR);
a2 = FOREACH a1 GENERATE ws;
aR = DISTINCT a2;

r1 = countAll(aR);
result = FOREACH r1 GENERATE '$date', '$toDate', *;

DUMP result;
