---------------------------------------------------------------------------
-- Finds total number of 'project-created' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = countEvents('$log', '$fromDate', '$toDate', 'project-created');

