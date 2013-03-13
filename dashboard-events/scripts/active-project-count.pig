---------------------------------------------------------------------------
-- Is used to calculate number of active projects per time-frame.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the timeframe
-- toDate     - ending of the timeframe
--
-- How to run:
-- pig -x local -param log="<DIRECTORY1>,<DIRECTORY2>..." 
--              -param date=<YYYYMMDD> -param toDate=<YYYYMMDD>
--              active-project-count.pig
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = LOAD '$log' using PigStorage() as (message : chararray);

--
-- Remove events unrelated to active tenant action
--
f1 = extractAndFilterByDate(log, $date, $toDate);
fR = FILTER f1 BY INDEXOF(message, 'EVENT#project-destroyed#', 0) == -1;

--
-- extract workspace name and project name out of identifiers
--
a1 = FOREACH fR GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*WS\\#([^\\#]*)\\#.*PROJECT\\#([^\\#]*)\\#.*'));
a2 = FOREACH a1 GENERATE $0 AS ws, $1 AS project;
aR = FILTER a2 BY ws != '' AND project != '';

--
-- extract workspace name and project name out of identifiers
--
c1 = FOREACH fR GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*PROJECT\\#([^\\#]*)\\#.*WS\\#([^\\#]*)\\#.*'));
c2 = FOREACH c1 GENERATE $1 AS ws, $0 AS project;
cR = FILTER c2 BY ws != '' AND project != '';

--
-- extract workspace name and project name out of message
--
b1 = FOREACH fR GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[.*\\]\\[(.*)\\]\\[.*\\] - .*PROJECT\\#([^\\#]*)\\#.*'));
b2 = FOREACH b1 GENERATE $0 AS ws, $1 AS project;
bR = FILTER b2 BY ws != '' AND project != '';

u1 = UNION aR, bR, cR;
u2 = DISTINCT u1;
uR = FOREACH u2 GENERATE '$date' AS date, ws, project; 

g1 = GROUP uR BY date;
result = FOREACH g1 GENERATE FLATTEN(group), '$toDate', COUNT(uR) AS value;

DUMP result;