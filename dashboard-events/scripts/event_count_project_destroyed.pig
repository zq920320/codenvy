---------------------------------------------------------------------------
-- Finds total number of 'project-destroyed' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = countEvents('$log', '$fromDate', '$toDate', 'project-destroyed');
DUMP result;
