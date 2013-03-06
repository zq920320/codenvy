---------------------------------------------------------------------------
-- Is used to calculate amount of the particular event occurrence. 
--
-- Incoming parameters:
-- log       - the list of resources to load
-- event     - the event being inspected
-- date      - the timeframe
--
-- How to run:
-- pig -x local -param log="<DIRECTORY1>,<DIRECTORY2>..." -param event=<EVENT_ID> 
--              -param date=<YYYYMMDD> event.pig
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = LOAD '$log' using PigStorage() as (message : chararray);
fR = extractAndFilterByDate(log, $date, $date);

a1 = FILTER fR BY INDEXOF(message, 'EVENT#$event#', 0) != -1;

a2 = GROUP a1 BY date;
result = FOREACH a2 GENERATE '$event', group AS date, COUNT(a1);

DUMP result;
