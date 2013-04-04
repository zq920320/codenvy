---------------------------------------------------------------------------
-- Finds total number of unique projects built.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = countAllInDistParamEventWs('$log', '$fromDate', '$toDate', 'project-built,application-created,project-deployed', 'PROJECT');
DUMP result;
