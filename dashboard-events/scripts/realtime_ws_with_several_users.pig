---------------------------------------------------------------------------
-- Finds the list of workspaces with collaboration sessions 
-- (at least '$sessionsCount' users) in '$lastMinutes'
---------------------------------------------------------------------------
IMPORT 'macros.pig';

%DEFAULT lastMinutes   '60';
%DEFAULT sessionsCount '2';

f1 = loadResources('$log');
fR = filterByLastMinutes(f1, '$lastMinutes');

a1 = extractWs(fR);
a2 = extractUser(a1);
a3 = FOREACH a2 GENERATE ws, user;
aR = DISTINCT a3;

---------------------------------------------------------------------------
-- Counts users in every workspace
---------------------------------------------------------------------------
b1 = GROUP aR BY ws;
b2 = FILTER b1 BY COUNT(aR) >= (int) $sessionsCount;
bR = FOREACH b2 GENERATE group, COUNT(aR);

r1 = GROUP bR ALL;
result = FOREACH r1 GENERATE bR;

DUMP result;
