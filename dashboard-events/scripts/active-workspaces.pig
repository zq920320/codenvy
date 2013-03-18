---------------------------------------------------------------------------
-- Finds amount of active workspaces in time frame.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the time frame
-- toDate     - ending of the time frame
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = LOAD '$log' using PigStorage() as (message : chararray);

f1 = extractAndFilterByDate(log, $date, $toDate);
f2 = FILTER f1 BY INDEXOF(message, 'EVENT#tenant-stopped#', 0) == -1;
f3 = FILTER f2 BY INDEXOF(message, 'EVENT#tenant-destroyed#', 0) == -1;
fR = FILTER f3 BY INDEXOF(message, 'EVENT#user-sso-logged-out#', 0) == -1;

a1 = extractWs(fR);

a2 = FOREACH a1 GENERATE ws;
aR = DISTINCT a2;

r1 = countAll(aR);
result = FOREACH r1 GENERATE '$date', '$toDate', *;

DUMP result;
