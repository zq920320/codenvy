---------------------------------------------------------------------------
-- Finds amount of active projects in time frame.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the timeframe
-- toDate     - ending of the timeframe
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = LOAD '$log' using PigStorage() as (message : chararray);

f1 = extractAndFilterByDate(log, $date, $toDate);
fR = FILTER f1 BY INDEXOF(message, 'EVENT#project-destroyed#', 0) == -1;

a1 = extractWs(fR);
a2 = extractParam(a1, 'PROJECT', 'project');
a3 = FOREACH a2 GENERATE ws, project;
aR = DISTINCT a3;

r1 = countAll(aR);
result = FOREACH r1 GENERATE '$date', '$toDate', *;

DUMP result;