IMPORT 'macros.pig';

%DEFAULT inactiveInterval '10';  -- in minutes

f1 = loadResources('$log');

t1 = extractUser(fR);
tR = extractWs(t1);

fR = filterByDate(f1, '$fromDate', '$toDate');

r1 = productUsageTimeList(tR, '$inactiveInterval');
result = FOREACH r1 GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(user), TOTUPLE(dt), TOTUPLE(delta));

