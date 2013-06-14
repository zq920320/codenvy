IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$FROM_DATE', '$TO_DATE');
fR = filterByEvent(f2, 'user-added-to-ws');

t1 = extractUser(fR);
t2 = extractWs(t1);
tR = extractParam(t2, 'FROM', 'from');

result = FOREACH tR GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(user), TOTUPLE(from));

