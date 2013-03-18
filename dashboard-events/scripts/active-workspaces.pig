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

--
-- Remove events unrelated to active tenant action
--
f1 = extractAndFilterByDate(log, $date, $toDate);
f2 = FILTER f1 BY INDEXOF(message, 'EVENT#tenant-stopped#', 0) == -1;
f3 = FILTER f2 BY INDEXOF(message, 'EVENT#tenant-destroyed#', 0) == -1;
fR = FILTER f3 BY INDEXOF(message, 'EVENT#user-sso-logged-out#', 0) == -1;

--
-- extract workspace name out of WS identifier
--
a1 = FOREACH fR GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*WS\\#([^\\#]*)\\#.*')) AS ws;
aR = FILTER a1 BY ws != '';

--
-- extract workspace name out of message
--
b1 = FOREACH fR GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[.*\\]\\[(.*)\\]\\[.*\\] - .*')) AS ws;
bR = FILTER b1 BY ws != '';

u1 = UNION aR, bR;
u2 = DISTINCT u1;
uR = FOREACH u2 GENERATE '$date' AS date, ws; 

g1 = GROUP uR BY date;
result = FOREACH g1 GENERATE FLATTEN(group), '$toDate', COUNT(uR) AS value;

DUMP result;
