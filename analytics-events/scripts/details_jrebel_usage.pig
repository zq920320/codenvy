---------------------------------------------------------------------------
-- Finds detailed number of 'jrebel-usage' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = countSecondParamInDist2ParamsEventWs('$log', '$fromDate', '$toDate', 'jrebel-usage', 'PROJECT', 'JREBEL');
DUMP result;