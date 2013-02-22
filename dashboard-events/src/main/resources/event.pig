---------------------------------------------------------------------------
-- Is used to calculate amount of the particular event. 
--
-- Incoming parameters:
-- log       - (mandatory) the list of resources to load
-- event     - (mandatory) the event being inspected
-- from      - the beginning of the time-frame
-- to        - the ending of the time-frame
-- storeInto - (mandatory) the directory where result will be stored
--
-- How to run:
-- pig -x local -param log="<DIRECTORY1>,<DIRECTORY2>..." -param event=<EVENT_ID> 
--              -param from=<YYYYMMDD> -param to=<YYYYMMDD> 
--              -param storeInto=<DIRECTORY_TO_STORE_RESULT> event.pig
---------------------------------------------------------------------------
IMPORT 'macros.pig';

%DEFAULT from '00000000';    -- 0000-00-00
%DEFAULT to   '99999999';    -- 9999-99-99

log = LOAD '$log' using PigStorage() as (message : chararray);
filteredByDate = extractAndFilterByDate(log, $from, $to);
uniqEvents = DISTINCT filteredByDate;

a1 = FILTER uniqEvents BY INDEXOF(message, 'EVENT#$event#', 0) != -1;

a2 = GROUP a1 BY date;
a3 = FOREACH a2 GENERATE '$event', group AS date, COUNT(a1);

result = ORDER a3 BY date;

DUMP result;
STORE result INTO '$storeInto' USING com.codenvy.dashboard.pig.store.fs.FileStorage('event');