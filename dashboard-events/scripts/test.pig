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

--
-- prepare list of created users in given day
--
a1 = extractAndFilterByDate(log, $date, $date);
a2 = FILTER f1 BY INDEXOF(message, 'EVENT#user-created#', 0) != -1;
a3 = FOREACH a2 GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*ALIASES\\#\\[([^\\#]*)\\]\\#.*')) AS user;
aR = DISTINCT a4;

--
-- prepare list of users who created projects in time frame
--
b1 = extractAndFilterByDate(log, $date, $toDate);
b2 = FILTER fR BY INDEXOF(message, 'EVENT#project-created#', 0) != -1;
b3 = FOREACH b2 GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[(.*)\\]\\[.*\\]\\[.*\\] - .*')) AS user;
bR = DISTINCT b3;

--
-- find users who did not create projects
--
g1 = COGROUP bR BY user, aR BY user;
g2 = FILTER g1 BY IsEmpty($1);

result = foreach k generate 'user who created workspace by did not create project', $0, flatten($2.$1);
DUMP result;
