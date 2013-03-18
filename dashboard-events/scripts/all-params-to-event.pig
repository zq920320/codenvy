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

a1 = FILTER fR BY INDEXOF(message, 'EVENT#$event#', 0) != -1;
aR = FOREACH a1 GENERATE date, FLATTEN(REGEX_EXTRACT_ALL(message, '.*$paramName\\#([^\\#]*)\\#.*')) AS paramValue;

g1 = GROUP aR BY (paramValue, date);
g2 = FOREACH g1 GENERATE FLATTEN(group), COUNT(aR) AS value;
g3 = GROUP g2 BY date;

g4 = FOREACH g3 {
	g4 = FOREACH g2 GENERATE paramValue, value; -- removes 'date' from tuple
	GENERATE '$event' AS event, '$paramName' AS paramName, group AS date, g4;
       }
result = ORDER g4 BY event, paramName, date;

DUMP result;
