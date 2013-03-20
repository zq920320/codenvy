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

f1 = loadResources('$log');
f2 = filterByDate(f1, '$date', '$date');
fR = filterByEvent(f2, '$event');

r1 = extractParam(fR, '$paramName', 'paramValue');
r2 = countByField(r1, 'paramValue');
result = FOREACH r2 GENERATE '$event', '$paramName', '$date', *;

DUMP result;
