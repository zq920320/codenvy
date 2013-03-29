-----------------------------------------------------------------------------
-- Find top workspaces by amount of compile and run action.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = topWsByEvents('$log', '$fromDate', '$toDate', '$top', 'project-built,project-deployed,application-created');
DUMP result;
