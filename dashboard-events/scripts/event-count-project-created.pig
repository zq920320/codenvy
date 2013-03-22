---------------------------------------------------------------------------
-- Finds total number of 'project-created' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

r1 = countEvents('$log', '$fromDate', '$toDate', 'project-created');
result = FOREACH r1 GENERATE '$fromDate', '$toDate', *;

DUMP result;
