---------------------------------------------------------------------------
-- Reveals detail information of which types project were created.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

r1 = countParamInParamEvent('$log', '$fromDate', '$toDate', 'project-created', 'TYPE');
result = FOREACH r1 GENERATE '$fromDate', '$toDate', *;

DUMP result;
