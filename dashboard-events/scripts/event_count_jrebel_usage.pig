---------------------------------------------------------------------------
-- Finds total number of 'jrebel-usage' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = countAllInDistParamEventWs('$log', '$fromDate', '$toDate', 'jrebel-usage', 'PROJECT');
DUMP result;
