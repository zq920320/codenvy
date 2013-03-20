-----------------------------------------------------------------------------
-- Finds users who did not create any projects in time frame.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the time frame
-- toDate     - ending of the time frame
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = loadResources('$log');

--
-- prepare list of created users in given day
--
a1 = filterByDate(log, $date, $date);
a2 = filterByEvent(a1, 'user-created');
a3 = FOREACH a2 GENERATE 'user-created', FLATTEN(REGEX_EXTRACT_ALL(message, '.*ALIASES\\#\\[([^\\#]*)\\]\\#.*')) AS user;
aR = DISTINCT a3;

--
-- prepare list of users who created projects in time frame
--
b1 = filterByDate(log, $date, $toDate);
b2 = filterByEvent(b1, 'project-created');
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
