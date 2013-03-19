---------------------------------------------------------------------------
-- Finds amount of every occurred event.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - the time frame
---------------------------------------------------------------------------
IMPORT 'macros.pig';

f1 = loadResources('$log');
fR = filterByDate(f1, '$date', '$date');

a1 = extractParam(fR, 'EVENT', 'event');
aR = countByParam(a1, 'event');

result = FOREACH aR GENERATE '$date', *;
DUMP result;
