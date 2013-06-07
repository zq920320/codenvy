IMPORT 'macros.pig';

lR = LOAD '$resultDir/USERS/$interval' USING PigStorage() AS (user: chararray, count: long, delta: long);
r = companiesByTimeSpent(lR, '$resultDir');

STORE r INTO '$resultDir/$entity/$interval' USING PigStorage();
result = FOREACH r GENERATE TOTUPLE(TOTUPLE(user), TOTUPLE(count), TOTUPLE(time));
