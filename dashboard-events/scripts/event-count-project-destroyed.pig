---------------------------------------------------------------------------
-- Finds total number of 'project-destroyed' events in given day.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - the time frame
---------------------------------------------------------------------------
IMPORT 'macros.pig';

%default toDate '$date';

result = countEvents('$log', '$date', '$toDate', 'project-destroyed');
DUMP result;
