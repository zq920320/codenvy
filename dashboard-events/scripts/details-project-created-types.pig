---------------------------------------------------------------------------
-- Reveals detail information what project types were created.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - the time frame
---------------------------------------------------------------------------
IMPORT 'macros.pig';

%DEFAULT toDate '$date';

r1 = countParamInParamEvent('$log', '$date', '$toDate', 'project-created', 'TYPE');
result = FOREACH r1 GENERATE '$date',  *;

DUMP result;
