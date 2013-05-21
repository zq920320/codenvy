IMPORT 'macros.pig';

f1 = loadResources('$log');
fR = filterByDate(f1, '$fromDate', '$toDate');

a1 = extractWs(fR);
a2 = extractUser(a1);
a3 = extractParam(a2, 'PROJECT', 'project');
aR = extractParam(a3, 'TYPE', 'type');

result = FOREACH aR GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(user), TOTUPLE(project), TOTUPLE(type));
