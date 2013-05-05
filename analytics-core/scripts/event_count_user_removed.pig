---------------------------------------------------------------------------
-- Finds total number of 'user-removed' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = countEvents('$log', '$fromDate', '$toDate', 'user-removed');

