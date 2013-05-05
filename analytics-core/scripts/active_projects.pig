---------------------------------------------------------------------------
-- Finds amount of active projects.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$fromDate', '$toDate');
fR = removeEvent(f2, 'project-destroyed');

a1 = extractWs(fR);
a2 = extractParam(a1, 'PROJECT', 'project');
result = FOREACH a2 GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(project));
