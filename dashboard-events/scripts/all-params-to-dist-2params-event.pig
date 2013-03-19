---------------------------------------------------------------------------
-- Finds amount of particular events distributed by parameter's value.
-- All events in question are distinct by the values of two additional parameters.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the time frame
-- toDate     - ending of the time frame
-- event      - the inspected event
-- paramName  - the first parameter name
-- secondParamName - the second parameter name
---------------------------------------------------------------------------
IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$date', '$toDate');
fR = filterByEvent(f2, '$event');

a1 = extractParam(fR, '$paramName', 'paramValue');
a2 = extractParam(a1, '$secondParamName', 'secondParamValue');
a3 = FOREACH a2 GENERATE paramValue, secondParamValue;
aR = DISTINCT a3;

r1 = countByParam(aR, 'secondParamValue');
result = FOREACH r1 GENERATE '$event', '$paramName', '$secondParamName', '$date', '$toDate', *;

DUMP result;
