---------------------------------------------------------------------------
-- Finds total number of 'project-destroyed' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

r1 = countEvents('$log', '$fromDate', '$toDate', 'project-destroyed');
result = FOREACH r1 GENERATE '$fromDate', '$toDate', *;

DUMP result;
