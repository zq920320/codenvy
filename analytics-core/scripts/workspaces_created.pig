IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$FROM_DATE', '$TO_DATE');
fR = filterByEvent(f2, 'tenant-created');

t1 = extractWs(fR);
tR = extractUser(t1);

result = FOREACH tR GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(user));

