IMPORT 'macros.pig';

a1 = loadResources('$log');
a2 = extractUser(a1);
a3 = extractWs(a2);
r = FOREACH a3 GENERATE ws, user, dt;

STORE r INTO '$RESULT_DIR/LOG' USING PigStorage();

