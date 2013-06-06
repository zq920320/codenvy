IMPORT 'macros.pig';

a1 = loadResources('$log');
a2 = extractUser(a1);
a3 = extractWs(a2);
r = FOREACH a3 GENERATE ws, user, dt;

STORE r INTO '$resultDir/LOG' USING PigStorage();
result = FOREACH r GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(user), TOTUPLE(dt));

