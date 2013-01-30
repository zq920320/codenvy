--
-- Is used to calculate amount of specific event. To run script use the follow commands:
-- pig -param log="<DIRECTORY1>,<DIRECTORY_2>" -param event=<EVENT_ID> event-occurrence.pig
-- replacing <EVENT-ID> by particular event you want to check,
--

log = LOAD '$log' using PigStorage() as (message : chararray);
b1 = FILTER log BY INDEXOF(message, 'EVENT#$event#', 0) != -1;
b2 = FOREACH b1 GENERATE '$event';

SPLIT b2 INTO d1 IF $0 == 'project-created', d2 OTHERWISE;

--
-- Count amount of rows in relation, SQL-like: select count(*) from table
--
-- For 'project-created' usecase result is needed to be devide by 2.
-- https://jira.exoplatform.org/browse/IDE-2254
--
k1 = GROUP d1 ALL;
res1 = FOREACH k1 GENERATE '$event', COUNT(d1.$0) / 2;

k2 = GROUP d2 ALL;
res2 = FOREACH k2 GENERATE '$event', COUNT(d2.$0);

result = UNION res1, res2;

DUMP result;
