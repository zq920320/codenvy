---------------------------------------------------------------------------
-- Finds total number of 'tenant-destroyed' events in given day.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - the time frame
---------------------------------------------------------------------------
IMPORT 'macros.pig';

%default toDate '$date';

result = countEvents('$log', '$date', '$toDate', 'tenant-destroyed');
DUMP result;
