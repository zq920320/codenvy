IMPORT 'macros.pig';

%DEFAULT inactiveInterval '10';  -- in minutes

f1 = loadResources('$log');
f2 = filterByDate(f1, '$FROM_DATE', '$TO_DATE');
f3 = extractUser(f2);
f = extractWs(f3);

r1 = productUsageTimeList(f, '$inactiveInterval');

r2 = GROUP r1 BY user;
r = FOREACH r2 {
    t = FOREACH r1 GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(dt), TOTUPLE(delta));
    GENERATE group, t;
    }

result = FOREACH r GENERATE group, t;

