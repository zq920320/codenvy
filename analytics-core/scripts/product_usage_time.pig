-----------------------------------------------------------------------------
-- Calculation total working time for all users in workspace.
-- If the user becomes inactive for 'inactiveInterval' amount of time, 
-- the 'coding session' is considered finished and a new coding session 
-- is started at the next interaction.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

%DEFAULT inactiveInterval '10';  -- in minutes

f1 = loadResources('$log');
fR = filterByDate(f1, '$fromDate', '$toDate');

t1 = extractUser(fR);
tR = extractWs(t1);

r1 = productUsageTimeList(tR, '$inactiveInterval');
result = FOREACH r1 GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(user), TOTUPLE(dt), TOTUPLE(delta));

