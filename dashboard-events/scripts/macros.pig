--
-- Macros common file.
--

--
-- Filters messages by date. Bordered dates are included. Input parameters represent date
-- in the next format: YYYYMMDD.
--
-- Returns relation (date : int, message : chararry)
--
DEFINE extractAndFilterByDate(X, fromDateParam, toDateParam) RETURNS Y {
  x1 = FOREACH $X GENERATE REGEX_EXTRACT_ALL($0, '[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3} ([0-9]{4})-([0-9]{2})-([0-9]{2}) (.*)');
  x2 = FOREACH x1 GENERATE FLATTEN($0);

  x3 = FOREACH x2 GENERATE (int)$0 * 10000 + (int)$1 * 100 + (int)$2 AS date, $3 AS message;
  x4 = FILTER x3 BY (int) $fromDateParam <= date AND date <= (int) $toDateParam;
  $Y = DISTINCT x4;
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

DEFINE filterByEvent(X, eventNameParam) RETURNS Y {
  $Y = FILTER $X BY INDEXOF(message, 'EVENT#$eventNameParam#', 0) > 0;
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