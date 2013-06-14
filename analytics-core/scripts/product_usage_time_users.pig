IMPORT 'macros.pig';

%DEFAULT inactiveInterval '10';  -- in minutes

lR = LOAD '$RESULT_DIR/LOG' USING PigStorage() AS (ws: chararray, user: chararray, dt: datetime);

r1 = filterByDateInterval(lR, '$TO_DATE', '$INTERVAL');
r2 = productUsageTimeList(r1, '$inactiveInterval');
r = usersByTimeSpent(r2);

STORE r INTO '$RESULT_DIR/$ENTITY/$INTERVAL' USING PigStorage();
result = FOREACH r GENERATE TOTUPLE(TOTUPLE(user), TOTUPLE(count), TOTUPLE(time));
