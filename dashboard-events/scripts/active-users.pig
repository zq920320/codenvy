-----------------------------------------------------------------------------
-- Finds amount of active users in time frame.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the time frame
-- toDate     - ending of the time frame
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = LOAD '$log' using PigStorage() as (message : chararray);

f1 = extractAndFilterByDate(log, $date, $toDate);
fR = FILTER f1 BY INDEXOF(message, 'EVENT#user-sso-logged-out#', 0) == -1;

a1 = extractUser(fR);
a2 = FOREACH a1 GENERATE user;
aR = DISTINCT a2;

r1 = countAll(aR);
result = FOREACH r1 GENERATE '$date', '$toDate', *;

DUMP result;