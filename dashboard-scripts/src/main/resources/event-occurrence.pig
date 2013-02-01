--
-- Is used to calculate amount of specific event. Runtime parameters:
-- log   - (mandatory) the list of resources to load
-- event - (mandatory) the event being inspected
-- from  - the first date occurred event
-- to    - the last date occurred event
--

IMPORT 'macros.pig';

%DEFAULT from '0';           -- 0000-00-00
%DEFAULT to   '99999999';    -- 9999-99-99

log = LOAD '$log' using PigStorage() as (message : chararray);
logFilteredByDate = extractAndFilterByDate(log, $from, $to);

resA = FILTER logFilteredByDate BY INDEXOF(message, 'EVENT#$event#', 0) != -1;

--
-- Do like: select count(*) from <table>
--
g = GROUP resA ALL;
result = FOREACH g GENERATE '$event', COUNT(resA.message);

DUMP result;
