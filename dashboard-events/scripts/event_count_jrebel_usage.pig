---------------------------------------------------------------------------
-- Finds total number of 'jrebel-usage' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

w = countAllInDistParamEventWs('$log', '$fromDate', '$toDate', 'jrebel-usage', 'PROJECT');

result = FOREACH w GENERATE '$fromDate', '$toDate', *;

DUMP result;
