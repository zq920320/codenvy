-----------------------------------------------------------------------------
-- Is used to calculate amount of active users per time-frame.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the timeframe
-- toDate     - ending of the timeframe
--
-- How to run:
-- pig -x local -param log="<DIRECTORY1>,<DIRECTORY2>..." 
--              -param date=<YYYYMMDD> -param toDate=<YYYYMMDD>
--              active-user-count.pig
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = LOAD '$log' using PigStorage() as (message : chararray);
filteredByDate = extractAndFilterByDate(log, $date, $toDate);

--
-- Remove unrelated events
--
uniq1 = DISTINCT filteredByDate;
uniqResult = FILTER uniq1 BY INDEXOF(message, 'EVENT#user-sso-logged-out#', 0) == -1;

--
-- extract user name out of USER identifier
--
a1 = FOREACH uniqResult GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*USER\\#([^\\#]*)\\#.*')) AS user;
aResult = FILTER a1 BY user != '';

--
-- extract user name out of message
--
b1 = FOREACH uniqResult GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[(.*)\\]\\[.*\\]\\[.*\\] - .*')) AS user;
bResult = FILTER b1 BY user != '';

u1 = UNION aResult, bResult;
u2 = DISTINCT u1;
uResult = FOREACH u2 GENERATE '$date' AS date, user; 

g1 = GROUP uResult BY date;
result = FOREACH g1 GENERATE FLATTEN(group), '$toDate', COUNT(uResult) AS value;

DUMP result;