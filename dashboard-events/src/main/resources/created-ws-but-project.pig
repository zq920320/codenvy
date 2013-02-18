------------------------------------------------------------------
-- Returns list of users who have been added to workspace but did 
-- not create project. Incoming parameters:
-- log       - (mandatory) the list of resources to load
-- from      - the beginning of the time-frame
-- to        - the ending of the time-frame
-- storeInto - mongoDb server URI where result being stored
--
-- How to run:
-- pig -x local -param log="<DIRECTORY1>,<DIRECTORY2>..." 
--              -param from=<YYYYMMDD> -param to=<YYYYMMDD> 
--              -param storeInto=<MONGO_DB_SERVER_URI> created-ws-but-project.pig
------------------------------------------------------------------

IMPORT 'macros.pig';

%DEFAULT from '00000000';    -- 0000-00-00
%DEFAULT to   '99999999';    -- 9999-99-99

log = LOAD '$log' using PigStorage() as (message : chararray);
filteredByDate = extractAndFilterByDate(log, $from, $to);
uniqEvents = DISTINCT filteredByDate;

SPLIT uniqEvents INTO b1 IF INDEXOF(message, 'EVENT#project-created#', 0) != -1, a1 IF INDEXOF(message, 'EVENT#user-added-to-ws#', 0) != -1;

--
-- resB Tuple : ('project-created', ws, user)
--
b2 = FOREACH b1 GENERATE REGEX_EXTRACT_ALL(message, '.*\\[(.*)\\].*\\[(.*)\\].*\\[(.*)\\].*\\[(.*)\\].*\\[(.*)\\].*\\[(.*)\\](.*)');
resB = FOREACH b2 GENERATE 'project-created', $0.$4 AS ws, $0.$3 AS user; 

--
-- resA Tuple : ('user-added-to-ws', ws, user)
--
a2 = FOREACH a1 GENERATE REGEX_EXTRACT_ALL(message, '.*EVENT\\#(.*)\\#.*WS\\#(.*)\\#.*USER\\#(.*)\\#.*');
a3 = FOREACH a2 GENERATE FLATTEN($0);

--
-- https://jira.exoplatform.org/browse/CLDIDE-593
--
resA = FOREACH a3 GENERATE $0, SUBSTRING($1,1,1000) AS ws, SUBSTRING($2,1,1000) AS user;

--
-- groups by user and finds Tuples where 'project-created' bag is empty
--
g = COGROUP resB BY user, resA BY user;
k = FILTER g BY IsEmpty($1);

result = foreach k generate group, flatten($2.$1) AS ws;

DUMP result;

