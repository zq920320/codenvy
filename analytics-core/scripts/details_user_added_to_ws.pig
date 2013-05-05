---------------------------------------------------------------------------
-- Reveals detail information how users were added to workspaces: 
-- website, invite etc.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = countSecondParamInDist2ParamsEventWs('$log', '$fromDate', '$toDate', 'user-added-to-ws', 'USER', 'FROM');

