---------------------------------------------------------------------------
-- Find total number of unique occurrence event and its parameter per time frame.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the timeframe
-- event      - the event being inspected
-- paramName  - the additional parameter name
-- toDate     - ending of the timeframe
--
-- How to run:
-- pig -x local -param log="<DIRECTORY1>,<DIRECTORY2>..." 
--              -param date=<YYYYMMDD> -param toDate=<YYYYMMDD>
--              -param event=<EVENT> -param paramName=<PARAM>
--              event-param-unique-count.pig
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
a1 = FOREACH fR GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*$paramName\\#([^\\#]*)\\#.*WS\\#([^\\#]*)\\#.*'));
a2 = FOREACH a1 GENERATE $0 AS paramValue, $1 AS ws;
aR = FILTER a2 BY paramValue != '' AND ws != '';

--
-- extract workspace name and event parameter type value out of identifiers
--
c1 = FOREACH fR GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*WS\\#([^\\#]*)\\#.*$paramName\\#([^\\#]*)\\#.*'));
c2 = FOREACH c1 GENERATE $1 AS paramValue, $0 AS ws;
cR = FILTER c2 BY paramValue != '' AND ws != '';

--
-- extract workspace name out of message and parameter type value out of identifiers
--
b1 = FOREACH fR GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[.*\\]\\[(.*)\\]\\[.*\\] - .*$paramName\\#([^\\#]*)\\#.*'));
b2 = FOREACH b1 GENERATE $1 AS paramValue, $0 AS ws;
bR = FILTER b2 BY paramValue != '' AND ws != '';

u1 = UNION aR, bR, cR;
uR = DISTINCT u1;

g1 = GROUP uR ALL;
gR = FOREACH g1 GENERATE COUNT(uR.$0) AS value;

result = FOREACH gR GENERATE '$event', '$paramName', '$date', '$toDate', value;
DUMP result;