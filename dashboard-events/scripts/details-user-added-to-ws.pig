---------------------------------------------------------------------------
-- Reveals detail information how users were added to workspaces: 
-- website, invite etc.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the time frame
-- toDat      - ending of the time frame
---------------------------------------------------------------------------
IMPORT 'macros.pig';

%default toDate '$date';

r1 = countSecondParamInDist2ParamsEventWs('$log', '$date', '$toDate', 'user-added-to-ws', 'USER', 'FROM');
result = FOREACH r1 GENERATE '$date', *;

DUMP result;
