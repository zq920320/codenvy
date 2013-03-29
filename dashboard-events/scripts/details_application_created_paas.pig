---------------------------------------------------------------------------
-- Reveals detail information what PAAS are choosed for application creation.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = countSecondParamInDist2ParamsEventWs('$log', '$fromDate', '$toDate', 'application-created,project-deployed', 'PROJECT', 'PAAS');
DUMP result;
