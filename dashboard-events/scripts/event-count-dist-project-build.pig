---------------------------------------------------------------------------
-- Finds total number of unique projects built.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

r1 = countAllInDistParamEventWs('$log', '$fromDate', '$toDate', 'project-built,application-created,project-deployed', 'PROJECT');
result = FOREACH r1 GENERATE '$fromDate', '$toDate', *;

DUMP result;
