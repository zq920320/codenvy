-----------------------------------------------------------------------------
-- Find top workspaces by amount of invitations sent.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

r1 = topWsByEvents('$log', '$fromDate', '$toDate', '$top', 'user-invite');
result = FOREACH r1 GENERATE '$fromDate', '$toDate', *;

DUMP result;