IMPORT 'macros.pig';

lR = LOAD '$RESULT_DIR/USERS/$INTERVAL' USING PigStorage() AS (user: chararray, count: long, delta: long);
r = companiesByTimeSpent(lR, '$RESULT_DIR');

STORE r INTO '$RESULT_DIR/$ENTITY/$INTERVAL' USING PigStorage();
result = FOREACH r GENERATE TOTUPLE(TOTUPLE(user), TOTUPLE(count), TOTUPLE(time));
