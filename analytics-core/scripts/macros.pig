---------------------------------------------------------------------------
-- Loads resources.
-- @return {ip : bytearray, dt : datetime,  event : bytearray, message : chararray} 
-- In details:
--   field 'date' contains date in format 'YYYYMMDD'
--   field 'time' contains seconds from midnight
---------------------------------------------------------------------------
DEFINE loadResources(resourceParam) RETURNS Y {
  l1 = LOAD '$resourceParam' using PigStorage() as (message : chararray);
  l2 = FOREACH l1 GENERATE REGEX_EXTRACT_ALL($0, '([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}) ([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}).*EVENT\\#([^\\#]*)\\#.*') 
                          AS pattern, message;
  l3 = FILTER l2 BY pattern.$2 != '';
  l4 = FOREACH l3 GENERATE pattern.$0 AS ip, ToDate(pattern.$1, 'yyyy-MM-dd HH:mm:ss,SSS') AS dt, pattern.$2 AS event, message;
  $Y = DISTINCT l4;
};

---------------------------------------------------------------------------
-- Filters events by date of occurrence.
-- @param fromDateParam - date in format 'YYYYMMDD'
-- @param toDateParam  - date in format 'YYYYMMDD'
---------------------------------------------------------------------------
DEFINE filterByDate(X, fromDateParam, toDateParam) RETURNS Y {
  $Y = FILTER $X BY MilliSecondsBetween(ToDate('$fromDateParam', 'yyyyMMdd'), dt) <= 0 AND
                    MilliSecondsBetween(AddDuration(ToDate('$toDateParam', 'yyyyMMdd'), 'P1D'), dt) > 0;
};

---------------------------------------------------------------------------
-- Filters events by date of occurrence. Keeps only in $lastMinutesParam.
-- @param lastMinutesParam - time interval in minutes
-- @return {..., curentDt : datetime}
---------------------------------------------------------------------------
DEFINE filterByLastMinutes(X, lastMinutesParam) RETURNS Y {
  x1 = FOREACH $X GENERATE *, CurrentTime() AS currentDt;
  $Y = FILTER x1 BY MinutesBetween(currentDt, dt) < (long) $lastMinutesParam;
};

---------------------------------------------------------------------------
-- Filters events by names. Keeps only events from passed list.
-- @param eventNamesParam - comma separated list of event names
---------------------------------------------------------------------------
DEFINE filterByEvent(X, eventNamesParam) RETURNS Y {
  $Y = FILTER $X BY INDEXOF('$eventNamesParam', event, 0) >= 0;
};

---------------------------------------------------------------------------
-- Filters events by names. Keeps only events out of passed list.
-- @param eventsNameParam - comma separated list of event names
---------------------------------------------------------------------------
DEFINE removeEvent(X, eventNamesParam) RETURNS Y {
  $Y = FILTER $X BY INDEXOF('$eventNamesParam', event, 0) < 0;
};

---------------------------------------------------------------------------
-- Extract workspace name out of message and adds as field to tuple.
-- @return  {..., ws : bytearray}
---------------------------------------------------------------------------
DEFINE extractWs(X) RETURNS Y {
  x1 = extractParam($X, 'WS', 'ws');
  x2 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[.*\\]\\[(.*)\\]\\[.*\\] - .*')) AS ws;
  x3 = UNION x1, x2;
  x4 = DISTINCT x3;
  $Y = FILTER x4 BY ws != '' AND ws != 'default';
};

---------------------------------------------------------------------------
-- Extract sessin id out of message and adds as field to tuple.
-- @return  {..., session : bytearray}
---------------------------------------------------------------------------
DEFINE extractSession(X) RETURNS Y {
  x1 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[.*\\]\\[.*\\]\\[(.*)\\] - .*')) AS session;
  $Y = FILTER x1 BY session != '';
};

---------------------------------------------------------------------------
-- Extract user name out of message and adds as field to tuple.
-- @return  {..., user : bytearray}
---------------------------------------------------------------------------
DEFINE extractUser(X) RETURNS Y {
  x1 = smartExtractParam($X, 'USER', 'user');
  x2 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[(.*)\\]\\[.*\\]\\[.*\\] - .*')) AS user;
  x3 = UNION x1, x2;
  x4 = DISTINCT x3;
  $Y = FILTER x4 BY user != '';
};

---------------------------------------------------------------------------
-- Extract user name out of message and adds as field to tuple.
-- @return  {..., user : bytearray}
---------------------------------------------------------------------------
DEFINE extractUserFromAliases(X) RETURNS Y {
  x1 = FOREACH $X GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*ALIASES\\#[\\[]?([^\\#^\\[^\\]]*)[\\]]?\\#.*')) AS user;
  $Y = FOREACH x1 GENERATE FLATTEN(TOKENIZE(user, ',')) AS user;
};

---------------------------------------------------------------------------
-- Extract parameter value out of message and adds as field to tuple.
-- @param paramNameParam - the parameter name
-- @param paramFieldNameParam - the name of filed in the tuple
-- @return  {..., $paramFieldNameParam : bytearray}
---------------------------------------------------------------------------
DEFINE extractParam(X, paramNameParam, paramFieldNameParam) RETURNS Y {
  x1 = smartExtractParam($X, '$paramNameParam', '$paramFieldNameParam');
  $Y = FILTER x1 BY $paramFieldNameParam != '';
};

---------------------------------------------------------------------------
-- Extract parameter value out of message and adds as field to tuple.
-- @param paramNameParam - the parameter name
-- @param paramFieldNameParam - the name of filed in the tuple
-- @return  {..., $paramFieldNameParam : bytearray}
---------------------------------------------------------------------------
DEFINE smartExtractParam(X, paramNameParam, paramFieldNameParam) RETURNS Y {
  $Y = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*$paramNameParam\\#([^\\#]*)\\#.*')) AS $paramFieldNameParam;
};

---------------------------------------------------------------------------
-- Calculates the difference between two relation.
-- The relations should be preprocessed using 'prepareSet'.
-- @return {{(bytearray)}}
---------------------------------------------------------------------------
DEFINE differSets(A, B) RETURNS Y {
  w1 = JOIN $A BY $0 LEFT, $B BY $0;
  w2 = FILTER w1 BY $1 IS NULL;
  $Y = FOREACH w2 GENERATE $0;
};

---------------------------------------------------------------------------
-- Calculates the intersection between two relation.
-- The relations should be preprocessed using 'prepareSet'.
-- @return {{(bytearray)}}
---------------------------------------------------------------------------
DEFINE intersectSets(A, B) RETURNS Y {
  w1 = JOIN $A BY $0, $B BY $0;
  w2 = FOREACH w1 GENERATE $0;
  w3 = GROUP w2 ALL;
  $Y = FOREACH w3 GENERATE $1 AS list;
};

---------------------------------------------------------------------------
-- Generates a relation with a single field whose values are unique.
-- @param fieldNameParam - the field name to process
-- @return {$fieldNameParam : chararray}
---------------------------------------------------------------------------
DEFINE prepareSet(X, fieldNameParam) RETURNS Y {
  w1 = FOREACH $X GENERATE $fieldNameParam;
  $Y = DISTINCT w1;
};




