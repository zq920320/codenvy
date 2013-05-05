---------------------------------------------------------------------------
-- Finds total number of 'user-created' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = countEvents('$log', '$fromDate', '$toDate', 'user-invite');

