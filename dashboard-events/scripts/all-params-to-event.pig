---------------------------------------------------------------------------
-- Finds amount of occurence of every parameter's value for fixed event.
--
-- Incoming parameters:
-- log       - the list of resources to load
-- event     - the inspected event
-- paramName - the parameter name
-- date      - the time frame
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = LOAD '$log' using PigStorage() as (message : chararray);
fR = extractAndFilterByDate(log, $date, $date);

a1 = filterByEvent(fR, '$event');
aR = extractParam(a1, '$paramName', 'paramValue');

r1 = countByParam(aR, 'paramValue');
result = FOREACH r1 GENERATE '$event', '$paramName', '$date', *;

DUMP result;
