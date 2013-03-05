---------------------------------------------------------------------------
-- Is used to calculate amount of active projects per time-frame.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the timeframe
-- toDate     - ending of the timeframe
--
-- How to run:
-- pig -x local -param log="<DIRECTORY1>,<DIRECTORY2>..." 
--              -param date=<YYYYMMDD> -param toDate=<YYYYMMDD>
--              active-tenant-count.pig
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = LOAD '$log' using PigStorage() as (message : chararray);
filteredByDate = extractAndFilterByDate(log, $date, $toDate);
uniqEvents = DISTINCT filteredByDate;

--
-- extract workspace name and project name out of WS identifier
--
a1 = FOREACH uniqEvents GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*WS\\#([^\\#]*)\\#.*PROJECT\\#([^\\#]*)\\#.*'));
a2 = FOREACH a1 GENERATE $0 AS ws, $1 AS project;
SPLIT a2 INTO a3 IF ws != '' AND project != '', aOther OTHERWISE;
aResult = a3;

--
-- extract workspace name out of message
--
b1 = FOREACH uniqEvents GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[.*\\]\\[(.*)\\]\\[.*\\] - .*PROJECT\\#([^\\#]*)\\#.*'));
b2 = FOREACH b1 GENERATE $0 AS ws, $1 AS project;
SPLIT b2 INTO b3 IF ws != '' AND project != '', bOther OTHERWISE;
bResult = b3;

u1 = UNION aResult, bResult;
u2 = DISTINCT u1;
uResult = FOREACH u2 GENERATE '$date' AS date, ws, project; 

g1 = GROUP uResult BY date;
gResult = FOREACH g1 GENERATE FLATTEN(group), '$toDate', COUNT(uResult) AS value;

result = gResult;
DUMP result;