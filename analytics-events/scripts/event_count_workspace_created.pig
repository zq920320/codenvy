---------------------------------------------------------------------------
-- Finds total number of 'tenant-created' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = countEvents('$log', '$fromDate', '$toDate', 'tenant-created');
DUMP result;
