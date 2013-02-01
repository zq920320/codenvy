--
-- Returns list of users who were added to workspace but did not create project. Runtime parameters:
-- log   - (mandatory) the list of resources to load
-- from  - the first date occurred event
-- to    - the last date occurred event
--

IMPORT 'macros.pig';

%DEFAULT from '0';           -- 0000-00-00
%DEFAULT to   '99999999';    -- 9999-99-99

log = LOAD '$log' using PigStorage() as (message : chararray);
filteredByDate = extractAndFilterByDate(log, $from, $to);

SPLIT filteredByDate INTO b1 IF INDEXOF(message, 'EVENT#project-created#', 0) != -1, a1 IF INDEXOF(message, 'EVENT#user-added-to-ws#', 0) != -1;

--
-- resB Tuple : ('project-created', ws, user)
--
b2 = FOREACH b1 GENERATE REGEX_EXTRACT_ALL(message, '.*\\[(.*)\\].*\\[(.*)\\].*\\[(.*)\\].*\\[(.*)\\].*\\[(.*)\\].*\\[(.*)\\](.*)');
b3 = FOREACH b2 GENERATE 'project-created', $0.$4 AS ws, $0.$3 AS user; 
resB = DISTINCT b3;

--
-- resA Tuple : ('user-added-to-ws', ws, user)
--
a2 = FOREACH a1 GENERATE REGEX_EXTRACT_ALL(message, '.*EVENT\\#(.*)\\#.*WS\\#(.*)\\#.*USER\\#(.*)\\#.*');
a3 = FOREACH a2 GENERATE FLATTEN($0);
a4 = DISTINCT a3;

--
-- https://jira.exoplatform.org/browse/CLDIDE-593
--
resA = FOREACH a4 GENERATE $0, SUBSTRING($1,1,1000) AS ws, SUBSTRING($2,1,1000) AS user;

--
-- groups by user and finds Tuples where 'project-created' bag is empty
--
g = COGROUP resB BY user, resA BY user;
k = FILTER g BY IsEmpty($1);

result = foreach k generate 'user who created workspace by did not create project', $0, flatten($2.$1);

DUMP result;
