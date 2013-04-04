---------------------------------------------------------------------------
-- Reveals detail information of which types project were created.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = countParamInParamEvent('$log', '$fromDate', '$toDate', 'project-created', 'TYPE');
DUMP result;
