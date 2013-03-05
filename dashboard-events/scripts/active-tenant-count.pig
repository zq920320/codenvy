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

--
-- Remove events unrelated to active tenant action
--
uniq1 = DISTINCT filteredByDate;
uniq2 = FILTER uniq1 BY INDEXOF(message, 'EVENT#tenant-stopped#', 0) == -1;
uniq3 = FILTER uniq2 BY INDEXOF(message, 'EVENT#tenant-destroyed#', 0) == -1;
uniqResult = FILTER uniq3 BY INDEXOF(message, 'EVENT#user-sso-logged-out#', 0) == -1;

--
-- extract workspace name out of WS identifier
--
a1 = FOREACH uniqResult GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*WS\\#([^\\#]*)\\#.*')) AS ws;
aResult = FILTER a1 BY ws != '';

--
-- extract workspace name out of message
--
b1 = FOREACH uniqResult GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[.*\\]\\[(.*)\\]\\[.*\\] - .*')) AS ws;
bResult = FILTER b1 BY ws != '';

u1 = UNION aResult, bResult;
u2 = DISTINCT u1;
uResult = FOREACH u2 GENERATE '$date' AS date, ws; 

g1 = GROUP uResult BY date;
gResult = FOREACH g1 GENERATE FLATTEN(group), '$toDate', COUNT(uResult) AS value;

result = gResult;
DUMP result;