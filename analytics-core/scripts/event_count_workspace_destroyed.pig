---------------------------------------------------------------------------
-- Finds total number of 'tenant-destroyed' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = countEvents('$log', '$fromDate', '$toDate', 'tenant-destroyed');

