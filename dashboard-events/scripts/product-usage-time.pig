-----------------------------------------------------------------------------
-- Calculation total working time for all users in workspace.
-- If the user becomes inactive for 'inactiveInterval' amount of time, 
-- the 'coding session' is considered finished and a new coding session 
-- is started at the next interaction.
-- 
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the time frame
-- toDate     - ending of the time frame
-- inactiveInterval - the inactive interval for user's actions
---------------------------------------------------------------------------
IMPORT 'macros.pig';

-------------------------------------------------------
-- Let's keep only needed events in given time frame
-------------------------------------------------------
f1 = loadResources('$log');
f2 = filterByDate(f1, '$date', '$toDate');
f3 = skipEvent(f2, 'user-added-to-ws');
f4 = skipEvent(f3, 'user-created');
fR = skipEvent(f4, 'user-removed');

a1 = extractUser(fR);
a2 = extractParam(a1, 'EVENT', 'event');
aR = FOREACH a2 GENERATE date, user, time, event;
bR = FOREACH a2 GENERATE date, user, time, event;

-------------------------------------------------------
-- Finds for every event the closest next one 
-- as long as interval between two events less 
-- than 'inactiveInterval'
--
-- DESCRIBE j1: {aR::date: int,aR::user: bytearray,aR::time: int,aR::event: bytearray,bR::date: int,bR::user: bytearray,bR::time: int,bR::event: bytearray}
-------------------------------------------------------
j1 = JOIN aR BY (date, user), bR BY (date, user);
j2 = FILTER j1 BY (int) $inactiveInterval > bR::time - aR::time AND bR::time - aR::time > 0;
jR = FOREACH j2 GENERATE aR::date AS date, aR::user AS user, aR::time AS time, bR::time - aR::time AS interval;

g1 = GROUP jR BY (date, user, time);
gR = FOREACH g1 GENERATE MIN(jR.interval) AS interval;

-------------------------------------------------------
-- Calculates to total time in minutes
-------------------------------------------------------
r1 = GROUP gR ALL;
result = FOREACH r1 GENERATE '$date', '$toDate', '$inactiveInterval', SUM(gR.interval) / 60;

DUMP result;

