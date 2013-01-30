--
-- Returns list of users who created workspace but did not create project
-- Runtime parameters:
--  * asLog - AS logs
--  * sysLog - system AS logs
--
-- log format: YYYY-MM-DD HH:MM:SS,MMM[thread][log][class][user][tenant][sessionid] - message EVENT#event# WS#ws# USER#user#
--

asLog = LOAD '$asLog' using PigStorage() as (message : chararray);

--
-- b4 Tuple : ('project-created', ws, user)
--
b1 = FILTER asLog BY INDEXOF(message, 'EVENT#project-created#', 0) != -1;
b2 = FOREACH b1 GENERATE REGEX_EXTRACT_ALL(message, '(.*)\\[(.*)\\].*\\[(.*)\\].*\\[(.*)\\].*\\[(.*)\\].*\\[(.*)\\].*\\[(.*)\\](.*)');
b3 = FOREACH b2 GENERATE 'project-created', $0.$5, $0.$4; 
b4 = DISTINCT b3;

sysLog = LOAD '$sysLog' using PigStorage() as (message : chararray);

--
-- a4 Tuple : ('tenant-created', ws, user)
--
a1 = FILTER sysLog BY INDEXOF(message, 'EVENT#tenant-created#', 0) != -1;
a2 = FOREACH a1 GENERATE REGEX_EXTRACT_ALL(message, '.*EVENT\\#(.*)\\#.*WS\\#(.*)\\#.*USER\\#(.*)\\#.*');
a3 = FOREACH a2 GENERATE FLATTEN($0);
a4 = DISTINCT a3;

--
-- groups by user and finds Tuples where 'project-created' bag is empty
--
g = COGROUP b3 BY $2, a3 BY $2;
k = FILTER g BY IsEmpty($1);

result = foreach k generate 'user who created workspace by did not create project', $0, flatten($2.$1);

DUMP result;
