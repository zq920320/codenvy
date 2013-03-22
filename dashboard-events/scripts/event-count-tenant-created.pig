---------------------------------------------------------------------------
-- Finds total number of 'tenant-created' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

r1 = countEvents('$log', '$fromDate', '$toDate', 'tenant-created');
result = FOREACH r1 GENERATE '$fromDate', '$toDate', *;

DUMP result;
