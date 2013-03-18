--------------------------------------------------------------------------
-- Finds amount of distinct sequences consisting of fixed event 
-- and every parameter's value in time frame.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the timeframe
-- event      - the event being inspected
-- paramName  - the additional parameter name
-- toDate     - ending of the timeframe
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = LOAD '$log' using PigStorage() as (message : chararray);

f1 = extractAndFilterByDate(log, $date, $toDate);
fR = filterByEvent(f1, '$event');

a1 = extractWs(fR);
a2 = extractParam(a1, '$paramName', 'paramValue');
a3 = FOREACH a2 GENERATE ws, paramValue;
aR = DISTINCT a3;

r1 = countAll(aR);
result = FOREACH r1 GENERATE '$event', '$paramName', '$date', '$toDate', *;

DUMP result;