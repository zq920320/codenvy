---------------------------------------------------------------------------
-- Finds total number of 'project-created' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

r1 = countAllInDistParamEventWs('$log', '$fromDate', '$toDate', 'project-built,application-created,project-deployed', 'PROJECT');
result = FOREACH r1 GENERATE '$fromDate', '$toDate', *;

DUMP result;
