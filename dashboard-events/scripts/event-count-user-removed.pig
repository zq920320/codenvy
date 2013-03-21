---------------------------------------------------------------------------
-- Finds total number of 'user-removed' events in given day.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - the time frame
---------------------------------------------------------------------------
IMPORT 'macros.pig';

%default toDate '$date';

result = countEvents('$log', '$date', '$toDate', 'user-removed');
DUMP result;
