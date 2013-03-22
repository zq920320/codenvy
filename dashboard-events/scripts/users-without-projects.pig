-----------------------------------------------------------------------------
-- Finds users who did not create any projects.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = loadResources('$log');

--
-- prepare list of created users in given day
--
a1 = filterByDate(log, $fromDate, $fromDate);
a2 = filterByEvent(a1, 'user-created');
a3 = FOREACH a2 GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*ALIASES\\#\\[([^\\#]*)\\]\\#.*')) AS user;
aR = DISTINCT a3;

--
-- prepare list of users who created projects in time frame
--
b1 = filterByDate(log, $fromDate, $toDate);
b2 = filterByEvent(b1, 'project-created');
b3 = FOREACH b2 GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[(.*)\\]\\[.*\\]\\[.*\\] - .*')) AS user;
bR = DISTINCT b3;

--
-- find tuples where user-created record exists and project-created record does not
--
g1 = JOIN aR BY user LEFT, bR BY user;
g2 = FILTER g1 BY bR::user IS NULL;
g3 = FOREACH g2 GENERATE aR::user;
g4 = GROUP g3 ALL;

result = FOREACH g4 GENERATE '$fromDate', '$toDate', g3;
DUMP result;
