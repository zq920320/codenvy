---------------------------------------------------------------------------
-- Is used to calculate amount of all events.
--
-- Incoming parameters:
-- log       - (mandatory) the list of resources to load
-- from      - the beginning of the time-frame
-- to        - the ending of the time-frame
-- storeInto - (mandatory) the directory where result will be stored
--
-- How to run:
-- pig -x local -param log="<DIRECTORY1>,<DIRECTORY2>..." 
--              -param from=<YYYYMMDD> -param to=<YYYYMMDD> 
--              -param storeInto=<DIRECTORY_TO_STORE> event-all.pig
---------------------------------------------------------------------------
IMPORT 'macros.pig';

%DEFAULT from '00000000';    -- 0000-00-00
%DEFAULT to   '99999999';    -- 9999-99-99

log = LOAD '$log' using PigStorage() as (message : chararray);
filteredByDate = extractAndFilterByDate(log, $from, $to);
uniqEvents = DISTINCT filteredByDate;

a1 = FILTER uniqEvents BY INDEXOF(message, 'EVENT#', 0) != -1;
a2 = FOREACH a1 GENERATE date, FLATTEN(REGEX_EXTRACT_ALL(message, '.*EVENT\\#([^\\#]*)\\#.*')) AS event;

g1 = GROUP a2 BY (event, date);
g2 = FOREACH g1 GENERATE FLATTEN(group), COUNT(a2);
result = ORDER g2 BY event, date;

DUMP result;
STORE result INTO '$storeInto' USING com.codenvy.dashboard.pig.store.fs.FileStorage('event');