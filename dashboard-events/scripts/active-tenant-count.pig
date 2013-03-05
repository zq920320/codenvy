---------------------------------------------------------------------------
-- Is used to calculate amount of active tenants per time-frame.
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
-- extract workspace name out of WS identifier
--
a1 = FILTER uniqEvents BY INDEXOF(message, 'EVENT#', 0) != -1;
a2 = FOREACH a1 GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*WS\\#([^\\#]*)\\#.*')) AS ws;
SPLIT a2 INTO a3 IF ws != '', aOther OTHERWISE;
aResult = a3;

--
-- extract workspace name out of message
--
b1 = FOREACH uniqEvents GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[.*\\]\\[(.*)\\]\\[.*\\] - .*')) AS ws;
SPLIT b1 INTO b2 IF ws != '', bOther OTHERWISE;
bResult = b2;

u1 = UNION aResult, bResult;
u2 = DISTINCT u1;
uResult = FOREACH u2 GENERATE '$date' AS date, ws; 

g1 = GROUP uResult BY date;
gResult = FOREACH g1 GENERATE FLATTEN(group), '$toDate', COUNT(uResult) AS value;

result = gResult;
DUMP result;