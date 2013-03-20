-----------------------------------------------------------------------------
-- Find top workspaces by amount of invitations sent
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the time frame
-- toDate     - ending of the time frame
-- top        - how many workspaces should be returned
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = topWsByEvents('$log', '$date', '$toDate', '$top', 'user-invite');
DUMP result;