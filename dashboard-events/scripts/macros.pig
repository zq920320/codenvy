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