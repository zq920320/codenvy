--
-- Macros common file.
--

--
-- Loads resources and returns returns in format: {ip : bytearray, date : int, time : int, message : chararray} 
-- In details:
--   field 'date' contains date in format 'YYYYMMDD'
--   field 'time' contains seconds from midnight
--
DEFINE loadResources(resourceParam) RETURNS Y {
  l1 = LOAD '$resourceParam' using PigStorage() as (message : chararray);
  l2 = DISTINCT l1;
  l3 = FOREACH l2 GENERATE REGEX_EXTRACT_ALL($0, '([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}) ([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2}),([0-9]{3}).*') 
                          AS pattern, message;

  $Y = FOREACH l3 GENERATE pattern.$0 AS ip, 
                          (int)pattern.$1 * 10000 + (int)pattern.$2 * 100 + (int)pattern.$3 AS date, 
                          (int)pattern.$4 * 3600 + (int)pattern.$5 * 60 + (int)pattern.$6 AS time, 
                          message;
};

DEFINE filterByDate(X, fromDateParam, toDateParam) RETURNS Y {
  $Y = FILTER $X BY (int) $fromDateParam <= date AND date <= (int) $toDateParam;
};

DEFINE filterByEvent(X, eventNameParam) RETURNS Y {
  $Y = FILTER $X BY INDEXOF(message, 'EVENT#$eventNameParam#', 0) >= 0;
};

DEFINE skipEvent(X, eventNameParam) RETURNS Y {
  $Y = FILTER $X BY INDEXOF(message, 'EVENT#$eventNameParam#', 0) < 0;
};

DEFINE extractWs(X) RETURNS Y {
  x1 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*WS\\#([^\\#]*)\\#.*')) AS ws;
  x2 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[.*\\]\\[(.*)\\]\\[.*\\] - .*')) AS ws;
  x3 = UNION x1, x2;
  $Y = FILTER x3 BY ws != '';
};

DEFINE extractUser(X) RETURNS Y {
  x1 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*USER\\#([^\\#]*)\\#.*')) AS user;
  x2 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[(.*)\\]\\[.*\\]\\[.*\\] - .*')) AS user;
  x3 = UNION x1, x2;
  $Y = FILTER x3 BY user != '';
};

DEFINE extractParam(X, paramNameParam, paramValueParam) RETURNS Y {
  x1 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*$paramNameParam\\#([^\\#]*)\\#.*')) AS $paramValueParam;
  $Y = FILTER x1 BY $paramValueParam != '';
};

DEFINE countByParam(X, fieldParam) RETURNS Y {
  x1 = GROUP $X BY $fieldParam;
  x2 = FOREACH x1 GENERATE FLATTEN(group), COUNT($X);
  x3 = GROUP x2 ALL;
  $Y = FOREACH x3 GENERATE x2;
};

DEFINE countAll(X) RETURNS Y {
  x1 = GROUP $X ALL;
  $Y = FOREACH x1 GENERATE COUNT($X);
};