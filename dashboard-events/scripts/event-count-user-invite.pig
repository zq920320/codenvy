---------------------------------------------------------------------------
-- Finds total number of 'user-created' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

r1 = countEvents('$log', '$fromDate', '$toDate', 'user-invite');
result = FOREACH r1 GENERATE '$fromDate', '$toDate', *;

DUMP result;
