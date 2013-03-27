---------------------------------------------------------------------------
-- Finds amount of active projects.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$fromDate', '$toDate');
fR = removeEvent(f2, 'project-destroyed');

a1 = extractWs(fR);
a2 = extractParam(a1, 'PROJECT', 'project');
a3 = FOREACH a2 GENERATE ws, project;
aR = DISTINCT a3;

r1 = countAll(aR);
result = FOREACH r1 GENERATE '$fromDate', '$toDate', *;

DUMP result;