---------------------------------------------------------------------------
-- Finds total number of 'tenant-destroyed' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

r1 = countEvents('$log', '$fromDate', '$toDate', 'tenant-destroyed');
result = FOREACH r1 GENERATE '$fromDate', '$toDate', *;

DUMP result;
