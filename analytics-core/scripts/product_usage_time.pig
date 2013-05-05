-----------------------------------------------------------------------------
-- Calculation total working time for all users in workspace.
-- If the user becomes inactive for 'inactiveInterval' amount of time, 
-- the 'coding session' is considered finished and a new coding session 
-- is started at the next interaction.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

%DEFAULT inactiveInterval '10';

-------------------------------------------------------
-- Let's keep only needed events in given time frame
-------------------------------------------------------
f1 = loadResources('$log');
f2 = filterByDate(f1, '$fromDate', '$toDate');
fR = removeEvent(f2, 'user-added-to-ws,user-created,user-removed');

a1 = extractUser(fR);
aR = FOREACH a1 GENERATE user, dt;
bR = FOREACH a1 GENERATE user, dt;

-------------------------------------------------------
-- Finds for every event the closest next one 
-- as long as interval between two events less 
-- than 'inactiveInterval'
-------------------------------------------------------
j1 = JOIN aR BY (user), bR BY (user);
j2 = FOREACH j1 GENERATE aR::user AS user, aR::dt AS dt, SecondsBetween(bR::dt, aR::dt) AS interval;
jR = FILTER j2 BY 0 < interval AND interval <= (long) $inactiveInterval * 60;

g1 = GROUP jR BY (user, dt);
gR = FOREACH g1 GENERATE MIN(jR.interval) AS interval;

-------------------------------------------------------
-- Calculates to total time in minutes
-------------------------------------------------------
r1 = GROUP gR ALL;
result = FOREACH r1 GENERATE SUM(gR.interval) / 60;


