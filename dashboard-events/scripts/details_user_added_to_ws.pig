---------------------------------------------------------------------------
-- Reveals detail information how users were added to workspaces: 
-- website, invite etc.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

r1 = countSecondParamInDist2ParamsEventWs('$log', '$fromDate', '$toDate', 'user-added-to-ws', 'USER', 'FROM');
result = FOREACH r1 GENERATE '$fromDate', '$toDate', *;

DUMP result;
