---------------------------------------------------------------------------
-- Finds total number of 'user-removed' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

r1 = countEvents('$log', '$fromDate', '$toDate', 'user-removed');
result = FOREACH r1 GENERATE '$fromDate', '$toDate', *;

DUMP result;
