-----------------------------------------------------------------------------
-- Find top workspaces by amount of users
---------------------------------------------------------------------------
IMPORT 'macros.pig';

r1 = topWsByEvents('$log', '$fromDate', '$toDate', '$top', 'user-added-to-ws');
result = FOREACH r1 GENERATE '$fromDate', '$toDate', *;

DUMP result;
