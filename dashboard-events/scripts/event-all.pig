---------------------------------------------------------------------------
-- Is used to calculate amount of all events.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - the timeframe
--
-- How to run:
-- pig -x local -param log="<DIRECTORY1>,<DIRECTORY2>..." 
--              -param date=<YYYYMMDD> event-all.pig
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = LOAD '$log' using PigStorage() as (message : chararray);
filteredByDate = extractAndFilterByDate(log, $date, $date);
uniqEvents = DISTINCT filteredByDate;

a1 = FILTER uniqEvents BY INDEXOF(message, 'EVENT#', 0) != -1;
resa = FOREACH a1 GENERATE date, FLATTEN(REGEX_EXTRACT_ALL(message, '.*EVENT\\#([^\\#]*)\\#.*')) AS event;

g1 = GROUP resa BY (event, date);
g2 = FOREACH g1 GENERATE FLATTEN(group), COUNT(resa) AS value;
g3 = GROUP g2 BY date;

resg = FOREACH g3 {
	g4 = FOREACH g2 GENERATE event, value; -- removes 'date' from tuple
	GENERATE group AS date, g4;
       }

result = resg;
DUMP result;
