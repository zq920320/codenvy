IMPORT 'macros.pig';

%DEFAULT inactiveInterval '10';  -- in minutes

lR = LOAD '$resultDir/LOG' USING PigStorage() AS (ws: chararray, user: chararray, dt: datetime);

r1 = filterByDateInterval(lR, '$toDate', '$interval');
r2 = productUsageTimeList(r1, '$inactiveInterval');
r = usersByTimeSpent(r2);

STORE r INTO '$resultDir/$entity/$interval' USING PigStorage();
result = FOREACH r GENERATE TOTUPLE(TOTUPLE(user), TOTUPLE(count), TOTUPLE(time));
