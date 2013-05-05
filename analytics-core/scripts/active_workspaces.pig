---------------------------------------------------------------------------
-- Finds list of active workspaces.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

f1 = loadResources('$log');

f2 = filterByDate(f1, '$fromDate', '$toDate');
fR = removeEvent(f2, 'tenant-stopped,tenant-destroyed,user-sso-logged-out');

a1 = extractWs(fR);
result = FOREACH a1 GENERATE ws;

