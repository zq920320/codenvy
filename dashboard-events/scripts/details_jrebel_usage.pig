---------------------------------------------------------------------------
-- Finds detailed number of 'jrebel-usage' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

w = countSecondParamInDist2ParamsEventWs('$log', '$fromDate', '$toDate', 'jrebel-usage', 'PROJECT', 'JREBEL');

result = FOREACH w GENERATE '$fromDate', '$toDate', *;

DUMP result;