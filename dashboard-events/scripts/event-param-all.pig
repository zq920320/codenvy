---------------------------------------------------------------------------
-- Is used to calculate amount of every value of additional parameter 
-- relatively to particular event.
--
-- Incoming parameters:
-- log       - the list of resources to load
-- event     - the event being inspected
-- paramName - the additional parameter name
-- date      - the time-frame
--
-- How to run:
-- pig -x local -param log="<DIRECTORY1>,<DIRECTORY2>..." -param event=<EVENT_ID> 
--              -param paramName=<PARAM> -param date=<YYYYMMDD> event-param-all.pig
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = LOAD '$log' using PigStorage() as (message : chararray);
filteredByDate = extractAndFilterByDate(log, $date, $date);
uniqEvents = DISTINCT filteredByDate;

a1 = FILTER uniqEvents BY INDEXOF(message, 'EVENT#$event#', 0) != -1;
resA = FOREACH a1 GENERATE date, FLATTEN(REGEX_EXTRACT_ALL(message, '.*$paramName\\#([^\\#]*)\\#.*')) AS paramValue;

g1 = GROUP resA BY (paramValue, date);
g2 = FOREACH g1 GENERATE FLATTEN(group), COUNT(resA) AS value;
g3 = GROUP g2 BY date;

resG = FOREACH g3 {
	g4 = FOREACH g2 GENERATE paramValue, value; -- removes 'date' from tuple
	GENERATE '$event' AS event, '$paramName' AS paramName, group AS date, g4;
       }

result = ORDER resG BY event, paramName, date;
DUMP result;
