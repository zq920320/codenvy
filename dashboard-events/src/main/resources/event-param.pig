---------------------------------------------------------------------------
-- Is used to calculate amount of every value of additional parameter 
-- relatively to particular event.
--
-- Incoming parameters:
-- log       - (mandatory) the list of resources to load
-- event     - (mandatory) the event being inspected
-- paramName - (mandatory) additional parameter name
-- from      - the beginning of the time-frame
-- to        - the ending of the time-frame
-- storeInto - (mandatory) the directory where result will be stored
--
-- How to run:
-- pig -x local -param log="<DIRECTORY1>,<DIRECTORY2>..." -param event=<EVENT_ID> 
--              -param paramName=<PARAM> 
--              -param from=<YYYYMMDD> -param to=<YYYYMMDD> 
--              -param storeInto=<DIRECTORY_TO_STORE_RESULT> event-param.pig
---------------------------------------------------------------------------
IMPORT 'macros.pig';

%DEFAULT from '00000000';    -- 0000-00-00
%DEFAULT to   '99999999';    -- 9999-99-99

log = LOAD '$log' using PigStorage() as (message : chararray);
filteredByDate = extractAndFilterByDate(log, $from, $to);
uniqEvents = DISTINCT filteredByDate;

a1 = FILTER uniqEvents BY INDEXOF(message, 'EVENT#$event#', 0) != -1;
resA = FOREACH a1 GENERATE date, FLATTEN(REGEX_EXTRACT_ALL(message, '.*$paramName\\#([^\\#]*)\\#.*')) AS paramValue;

g1 = GROUP resA BY (paramValue, date);
g2 = FOREACH g1 GENERATE FLATTEN(group), COUNT(resA) AS value;
g3 = GROUP g2 BY date;

resG = FOREACH g3 {
	g4 = FOREACH g2 GENERATE paramValue, value; -- removes 'date' from tuple
	GENERATE '$event' AS event, '$paramName' AS paramName, group AS date, g4;
       }

result = ORDER resG BY event, paramName, date;

DUMP result;
STORE result INTO '$storeInto' USING com.codenvy.dashboard.pig.store.fs.FileStorage('event_param');