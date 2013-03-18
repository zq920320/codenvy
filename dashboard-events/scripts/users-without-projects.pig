-----------------------------------------------------------------------------
-- Finds users who did not create any projects in time frame.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the time frame
-- toDate     - ending of the time frame
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = LOAD '$log' using PigStorage() as (message : chararray);

--
-- prepare list of created users in given day
--
a1 = extractAndFilterByDate(log, $date, $date);
a2 = FILTER a1 BY INDEXOF(message, 'EVENT#user-created#', 0) != -1;
a3 = FOREACH a2 GENERATE 'user-created', FLATTEN(REGEX_EXTRACT_ALL(message, '.*ALIASES\\#\\[([^\\#]*)\\]\\#.*')) AS user;
aR = DISTINCT a3;

--
-- prepare list of users who created projects in time frame
--
b1 = extractAndFilterByDate(log, $date, $toDate);
b2 = FILTER b1 BY INDEXOF(message, 'EVENT#project-created#', 0) != -1;
b3 = FOREACH b2 GENERATE 'project-created', FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[(.*)\\]\\[.*\\]\\[.*\\] - .*')) AS user;
bR = DISTINCT b3;

--
-- find tuples where user-created record exists and project-created record does not
--
g1 = COGROUP bR BY user, aR BY user;
g2 = FILTER g1 BY IsEmpty($1) AND NOT IsEmpty($2);
g3 = FOREACH g2 GENERATE group;
g4 = GROUP g3 ALL;

result = FOREACH g4 GENERATE '$date', '$toDate', g3;
DUMP result;
