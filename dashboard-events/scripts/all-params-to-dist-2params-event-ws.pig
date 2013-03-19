---------------------------------------------------------------------------
-- Finds amount of occurence of every parameter's value for 
-- unique sequences consisting of fixed event and any another parameter's value.
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

a1 = extractWs(fR);
a2 = extractParam(a1, '$paramName', 'paramValue');
a3 = extractParam(a2, '$secondParamName', 'secondParamValue');
a4 = FOREACH a3 GENERATE ws, paramValue, secondParamValue;
aR = DISTINCT a4;

r1 = countByParam(aR, 'secondParamValue');
result = FOREACH r1 GENERATE '$event', '$paramName', '$secondParamName', '$date', '$toDate', *;

DUMP result;
