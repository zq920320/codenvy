---------------------------------------------------------------------------
-- Find number unique pairs: workspace name + first parameter value for every 
-- second parameter value in time frame.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the timeframe
-- toDate     - ending of the timeframe
-- event      - the event being inspected
-- paramName  - the first parameter name
-- secondParamName - the second parameter name
--
-- How to run:
-- pig -x local -param log="<DIRECTORY1>,<DIRECTORY2>..." 
--              -param date=<YYYYMMDD> -param toDate=<YYYYMMDD>
--              -param event=<EVENT> 
--              -param paramName=<PARAM1> -param secondParamName=<PARAM2>
--              event-2param-unique-all.pig
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = LOAD '$log' using PigStorage() as (message : chararray);

--
-- keep only needed events in time frame
--
f1 = extractAndFilterByDate(log, $date, $toDate);
f2 = DISTINCT f1;
fR = FILTER f2 BY INDEXOF(message, 'EVENT#$event#', 0) > 0;

--
-- extract workspace name and event parameter type value out of identifiers
--
a1 = FOREACH fR GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*WS\\#([^\\#]*)\\#.*')) AS ws, message;
a2 = FOREACH a1 GENERATE ws, FLATTEN(REGEX_EXTRACT_ALL(message, '.*$paramName\\#([^\\#]*)\\#.*')) AS paramValue, message;
a3 = FOREACH a2 GENERATE ws, paramValue, FLATTEN(REGEX_EXTRACT_ALL(message, '.*$secondParamName\\#([^\\#]*)\\#.*')) AS secondParamValue;
aR = FILTER a3 BY ws != '' AND paramValue != '' AND secondParamValue != '';

--
-- extract workspace name out of message and parameter type value out of identifiers
--
b1 = FOREACH fR GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[.*\\]\\[(.*)\\]\\[.*\\] - .*')) AS ws, message;
b2 = FOREACH b1 GENERATE ws, FLATTEN(REGEX_EXTRACT_ALL(message, '.*$paramName\\#([^\\#]*)\\#.*')) AS paramValue, message;
b3 = FOREACH b2 GENERATE ws, paramValue, FLATTEN(REGEX_EXTRACT_ALL(message, '.*$secondParamName\\#([^\\#]*)\\#.*')) AS secondParamValue;
bR = FILTER b3 BY ws != '' AND paramValue != '' AND secondParamValue != '';

u1 = UNION aR, bR;
uR = DISTINCT u1;

--
-- find amount of unique events for every second parameter value
--
g1 = GROUP uR BY secondParamValue;
g2 = FOREACH g1 GENERATE '$date' AS date, FLATTEN(group) AS secondParamValue, COUNT(uR) AS value;
g3 = GROUP g2 BY date;

result = FOREACH g3 {
	g4 = FOREACH g2 GENERATE secondParamValue, value; -- removes 'date' from tuple
	GENERATE '$event', '$paramName', '$secondParamName', group AS date, '$toDate', g4;
       }

DUMP result;