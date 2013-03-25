---------------------------------------------------------------------------
-- Loads resources.
-- @return {ip : bytearray, date : int, time : int, event : bytearray, message : chararray} 
-- In details:
--   field 'date' contains date in format 'YYYYMMDD'
--   field 'time' contains seconds from midnight
---------------------------------------------------------------------------
DEFINE loadResources(resourceParam) RETURNS Y {
  l1 = LOAD '$resourceParam' using PigStorage() as (message : chararray);
  l2 = FOREACH l1 GENERATE REGEX_EXTRACT_ALL($0, '([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}) ([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2}),([0-9]{3}).*EVENT\\#([^\\#]*)\\#.*') 
                          AS pattern, message;
  l3 = FILTER l2 BY pattern.$8 != '';
  l4 = FOREACH l3 GENERATE pattern.$0 AS ip, 
                          (int)pattern.$1 * 10000 + (int)pattern.$2 * 100 + (int)pattern.$3 AS date, 
                          (int)pattern.$4 * 3600 + (int)pattern.$5 * 60 + (int)pattern.$6 AS time, 
                          pattern.$8 AS event, message;
  $Y = DISTINCT l4;
};


---------------------------------------------------------------------------
-- Filters events by date of occurrence.
-- @param fromDateParam - date in format 'YYYYMMDD'
-- @param toDateParam  - date in format 'YYYYMMDD'
---------------------------------------------------------------------------
DEFINE filterByDate(X, fromDateParam, toDateParam) RETURNS Y {
  $Y = FILTER $X BY (int) $fromDateParam <= date AND date <= (int) $toDateParam;
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
-- Extract user name out of message and adds as field to tuple.
-- @return  {..., user : bytearray}
---------------------------------------------------------------------------
DEFINE extractUser(X) RETURNS Y {
  x1 = extractParam($X, 'USER', 'user');
  x2 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[(.*)\\]\\[.*\\]\\[.*\\] - .*')) AS user;
  x3 = UNION x1, x2;
  x4 = DISTINCT x3;
  $Y = FILTER x4 BY user != '';
};

---------------------------------------------------------------------------
-- Extract parameter value out of message and adds as field to tuple.
-- @param paramNameParam - the parameter name
-- @param paramFieldNameParam - the name of filed in the tuple
-- @return  {..., $paramFieldNameParam : bytearray}
---------------------------------------------------------------------------
DEFINE extractParam(X, paramNameParam, paramFieldNameParam) RETURNS Y {
  x1 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*$paramNameParam\\#([^\\#]*)\\#.*')) AS $paramFieldNameParam;
  $Y = FILTER x1 BY $paramFieldNameParam != '';
};

---------------------------------------------------------------------------
-- Counts how many times every distinct value of given field have met.
-- @param fieldNameParam - the field name
-- @return {countByField : {(field : bytearray, count : long)}}
---------------------------------------------------------------------------
DEFINE countByField(X, fieldNameParam) RETURNS Y {
  x1 = GROUP $X BY $fieldNameParam;
  x2 = FOREACH x1 GENERATE FLATTEN(group) AS field, COUNT($X) AS count;
  x3 = GROUP x2 ALL;
  $Y = FOREACH x3 GENERATE x2 AS countByField;
};

---------------------------------------------------------------------------
-- Counts the number of tuples.
-- @return {count : long}
---------------------------------------------------------------------------
DEFINE countAll(X) RETURNS Y {
  x1 = GROUP $X ALL;
  $Y = FOREACH x1 GENERATE COUNT($X) AS count;
};

---------------------------------------------------------------------------
-- Counts the number of given events in every workspace separatly.
-- @param eventsParam - comma separated list of events
-- @return {countByField : {(field : bytearray, count : long)}}
---------------------------------------------------------------------------
DEFINE countEventsInWs(X, eventsParam) RETURNS Y {
  z1 = filterByEvent($X, '$eventsParam');
  z2 = extractWs(z1);
  $Y = countByField(z2, 'ws');
};

---------------------------------------------------------------------------
-- Counts the number of given events in every workspace separatly.
-- @param eventsParam - comma separated list of events
-- @return {ws : bytearray, count : long}
---------------------------------------------------------------------------
DEFINE countEventsInWsFlatten(X, eventsParam) RETURNS Y {
  w1 = countEventsInWs($X, '$eventsParam');
  w2 = FOREACH w1 GENERATE FLATTEN(countByField);
  $Y = FOREACH w2 GENERATE countByField::field AS ws, countByField::count AS count;
};

-----------------------------------------------------------------------------
-- Finds top workspaces in which events had place.
-- @param logParam - the list of resources to load
-- @param fromDateParam - beginning of the time frame
-- @param toDateParam - ending of the time frame
-- @param topParam - how many workspaces should be in result
-- @param eventsParam - comma separated list of events
-- @return {(field : bytearray, count : long)}
---------------------------------------------------------------------------
DEFINE topWsByEvents(logParam, fromDateParam, toDateParam, topParam, eventsParam) RETURNS Y {
  w1 = loadResources('$logParam');
  w2 = filterByDate(w1, '$fromDateParam', '$toDateParam');
  w3 = countEventsInWs(w2, '$eventsParam');

  $Y = FOREACH w3 {
    GENERATE TOP($topParam, 1, countByField);
  }
};

-----------------------------------------------------------------------------
-- Counts the number of given events.
-- @param logParam - the list of resources to load
-- @param fromDateParam - beginning of the time frame
-- @param toDateParam - ending of the time frame
-- @param eventsParam - comma separated list of events
-- @return {count : long}
---------------------------------------------------------------------------
DEFINE countEvents(logParam, fromDateParam, toDateParam, eventsParam) RETURNS Y {
  w1 = loadResources('$logParam');
  w2 = filterByDate(w1, '$fromDateParam', '$toDateParam');
  w3 = filterByEvent(w2,'$eventsParam');
  $Y = countAll(w3);
};

---------------------------------------------------------------------------
-- Calculates the difference between two relation.
-- The relations should be preprocessed using 'prepareSet'.
-- @return {{(bytearray)}}
---------------------------------------------------------------------------
DEFINE differSets(A, B) RETURNS Y {
  w1 = JOIN $A BY $0 LEFT, $B BY $0;
  w2 = FILTER w1 BY $1 IS NULL;
  w3 = FOREACH w2 GENERATE $0;
  w4 = GROUP w3 ALL;
  $Y = FOREACH w4 GENERATE $1;
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

---------------------------------------------------------------------------
-- Finds amount of occurence of every parameter's value in
-- unique sequences consisting of fixed event, workspace and two parameter's value.
-- @param logParam - the list of resources to load
-- @param fromDateParam - beginning of the time frame
-- @param toDateParam - ending of the time frame
-- @param eventParam - which event should be taken in account
-- @param paramNameParam - the first parameter name
-- @param secondParamNameParam - the second parameter name
-- @return {countByField : {(field : bytearray, count : long)}}
---------------------------------------------------------------------------
DEFINE countSecondParamInDist2ParamsEventWs(logParam, fromDateParam, toDateParam, eventParam, paramNameParam, secondParamNameParam) RETURNS Y {
  w1 = loadResources('$logParam');
  w2 = filterByDate(w1, '$fromDateParam', '$toDateParam');
  w3 = filterByEvent(w2, '$eventParam');

  w4 = extractWs(w3);
  w5 = extractParam(w4, '$paramNameParam', 'paramFieldName');
  w6 = extractParam(w5, '$secondParamNameParam', 'secondParamFieldName');
  w7 = FOREACH w6 GENERATE ws, paramFieldName, secondParamFieldName;
  w8 = DISTINCT w7;

  $Y = countByField(w8, 'secondParamFieldName');
};

---------------------------------------------------------------------------
-- Finds amount of occurence of every parameter's value in
-- unique sequences consisting of fixed event and two parameter's value.
-- @param logParam - the list of resources to load
-- @param fromDateParam - beginning of the time frame
-- @param toDateParam - ending of the time frame
-- @param eventParam - which event should be taken in account
-- @param paramNameParam - the first parameter name
-- @param secondParamNameParam - the second parameter name
-- @return {countByField : {(field : bytearray, count : long)}}
---------------------------------------------------------------------------
DEFINE countSecondParamInDist2ParamsEvent(logParam, fromDateParam, toDateParam, eventParam, paramNameParam, secondParamNameParam) RETURNS Y {
  w1 = loadResources('$logParam');
  w2 = filterByDate(w1, '$fromDateParam', '$toDateParam');
  w3 = filterByEvent(w2, '$eventParam');

  w5 = extractParam(w3, '$paramNameParam', 'paramFieldName');
  w6 = extractParam(w5, '$secondParamNameParam', 'secondParamFieldName');
  w7 = FOREACH w6 GENERATE paramFieldName, secondParamFieldName;
  w8 = DISTINCT w7;

  $Y = countByField(w8, 'secondParamFieldName');
};

---------------------------------------------------------------------------
-- Finds amount of occurence of every parameter's value in
-- sequences consisting of fixed event and parameter's value.
-- @param logParam - the list of resources to load
-- @param fromDateParam - beginning of the time frame
-- @param toDateParam - ending of the time frame
-- @param eventParam - comma separated list of events
-- @param paramNameParam - the first parameter name
-- @return {countByField : {(field : bytearray, count : long)}}
---------------------------------------------------------------------------
DEFINE countParamInParamEvent(logParam, fromDateParam, toDateParam, eventParam, paramNameParam) RETURNS Y {
  w1 = loadResources('$logParam');
  w2 = filterByDate(w1, '$fromDateParam', '$toDateParam');
  w3 = filterByEvent(w2, '$eventParam');
  w4 = extractParam(w3, '$paramNameParam', 'paramFieldName');
  $Y = countByField(w4, 'paramFieldName');
};

---------------------------------------------------------------------------
-- Finds amount of occurence of every parameter's value in
-- sequences consisting of fixed event and parameter's value.
-- @param logParam - the list of resources to load
-- @param fromDateParam - beginning of the time frame
-- @param toDateParam - ending of the time frame
-- @param eventParam - comma separated list of events
-- @param paramNameParam - the first parameter name
-- @return {count : long}
---------------------------------------------------------------------------
DEFINE countAllInDistParamEventWs(logParam, fromDateParam, toDateParam, eventParam, paramNameParam) RETURNS Y {
  w1 = loadResources('$logParam');
  w2 = filterByDate(w1, '$fromDateParam', '$toDateParam');
  w3 = filterByEvent(w2, '$eventParam');

  w5 = extractWs(w3);
  w6 = extractParam(w5, '$paramNameParam', 'paramValue');

  w7 = FOREACH w6 GENERATE ws, paramValue;
  w8 = DISTINCT w7;

  $Y = countAll(w8);
};