IMPORT 'macros.pig';

f1 = loadResources('$log');
fR = filterByDate(f1, '$FROM_DATE', '$TO_DATE');

a1 = extractUser(fR);
a2 = extractWs(a1);
aR = skipDefaults(a2);

r1 = FOREACH aR GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(user));
result = DISTINCT r1;
