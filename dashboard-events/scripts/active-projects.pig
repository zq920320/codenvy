---------------------------------------------------------------------------
-- Finds amount of active projects in time frame.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the timeframe
-- toDate     - ending of the timeframe
---------------------------------------------------------------------------
IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$date', '$toDate');
fR = skipEvent(f2, 'project-destroyed');

a1 = extractWs(fR);
a2 = extractParam(a1, 'PROJECT', 'project');
a3 = FOREACH a2 GENERATE ws, project;
aR = DISTINCT a3;

r1 = countAll(aR);
result = FOREACH r1 GENERATE '$date', '$toDate', *;

DUMP result;