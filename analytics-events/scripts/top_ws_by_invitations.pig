-----------------------------------------------------------------------------
-- Find top workspaces by amount of invitations sent.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = topWsByEvents('$log', '$fromDate', '$toDate', '$top', 'user-invite');
DUMP result;