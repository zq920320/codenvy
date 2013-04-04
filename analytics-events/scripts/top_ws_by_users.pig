-----------------------------------------------------------------------------
-- Find top workspaces by amount of users
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = topWsByEvents('$log', '$fromDate', '$toDate', '$top', 'user-added-to-ws');
DUMP result;
