IMPORT 'macros.pig';

%DEFAULT condition       'count >= 5 AND time >= 120 * 60 AND time < 300 * 60';

%DEFAULT inactiveInterval '10';  -- in minutes

a1 = LOAD '$RESULT_DIR/LOG' USING PigStorage() AS (ws: chararray, user: chararray, dt: datetime);
a2 = productUsageTimeList(a1, '$inactiveInterval');

s1 = GROUP a2 ALL;
s = FOREACH s1 GENERATE $0;

a3 = calculateCondition(a2, '$condition', '$TO_DATE', s);
result = FOREACH a3 GENERATE TOTUPLE(TOTUPLE($0), TOTUPLE($1), TOTUPLE($2), TOTUPLE($3), TOTUPLE($4), TOTUPLE($5));
