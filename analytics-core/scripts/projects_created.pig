IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$fromDate', '$toDate');
fR = filterByEvent(f2, 'project-created');

t1 = extractWs(fR);
t2 = extractUser(t1);
t3 = extractParam(t2, 'TYPE', 'type');
tR = extractParam(t3, 'PROJECT', 'project');

result = FOREACH tR GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(user), TOTUPLE(project), TOTUPLE(type));

