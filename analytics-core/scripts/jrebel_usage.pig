---------------------------------------------------------------------------
-- Finds total number of 'jrebel-usage' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$fromDate', '$toDate');
fR = filterByEvent(f2, 'jrebel-usage');

t1 = extractWs(fR);
t2 = extractUser(t1);
t3 = extractParam(t2, 'TYPE', 'type');
t4 = extractParam(t3, 'PROJECT', 'project');
t5 = extractParam(t4, 'JREBEL', 'jrebel');
tR = DISTINCT t5;

r1 = FOREACH tR GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(user), TOTUPLE(project), TOTUPLE(type), TOTUPLE(jrebel));
result = DISTINCT r1;


