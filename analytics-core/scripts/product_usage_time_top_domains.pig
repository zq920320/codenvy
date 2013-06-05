IMPORT 'macros.pig';

%DEFAULT inactiveInterval '10';  -- in minutes
%DEFAULT top '100';

lR = loadResources('$log');

---------------------------------------------------------------------------
-- Finds best for period
---------------------------------------------------------------------------
a1 = filterByDateInterval(lR, '$toDate', '$interval');
a2 = extractUser(a1);
a3 = extractWs(a2);
a4 = productUsageTimeList(a3, '$inactiveInterval');
a5 = domainsByTimeSpent(a4);

---------------------------------------------------------------------------
-- Keeps only $top users from the whole list
---------------------------------------------------------------------------
a6 = GROUP a5 ALL;
a7 = FOREACH a6 GENERATE FLATTEN(TOP((int) $top, 2, a5));
aR = FOREACH a7 GENERATE a5::user AS user;

---------------------------------------------------------------------------
-- Calculate for 1D
---------------------------------------------------------------------------
d1_1 = filterByDateInterval(lR, '$toDate', 'P1D');
d1_2 = extractUser(d1_1);
d1_3 = extractWs(d1_2);
d1_4 = productUsageTimeList(d1_3, '$inactiveInterval');
d1 = domainsByTimeSpent(d1_4);

r1_1 = JOIN aR BY user LEFT, d1 BY user;
r1 = FOREACH r1_1 GENERATE aR::user AS user, d1::time AS time1d;

---------------------------------------------------------------------------
-- Calculate for 7D
---------------------------------------------------------------------------
d7_1 = filterByDateInterval(lR, '$toDate', 'P7D');
d7_2 = extractUser(d7_1);
d7_3 = extractWs(d7_2);
d7_4 = productUsageTimeList(d7_3, '$inactiveInterval');
d7 = domainsByTimeSpent(d7_4);

r7_1 = JOIN r1 BY user LEFT, d7 BY user;
r7 = FOREACH r7_1 GENERATE r1::user AS user, r1::time1d AS time1d, d7::time AS time7d;

---------------------------------------------------------------------------
-- Calculate for 30D
---------------------------------------------------------------------------
d30_1 = filterByDateInterval(lR, '$toDate', 'P30D');
d30_2 = extractUser(d30_1);
d30_3 = extractWs(d30_2);
d30_4 = productUsageTimeList(d30_3, '$inactiveInterval');
d30 = domainsByTimeSpent(d30_4);

r30_1 = JOIN r7 BY user LEFT, d30 BY user;
r30 = FOREACH r30_1 GENERATE r7::user AS user, r7::time1d AS time1d, r7::time7d AS time7d, d30::time AS time30d;

---------------------------------------------------------------------------
-- Calculate for 60D
---------------------------------------------------------------------------
d60_1 = filterByDateInterval(lR, '$toDate', 'P60D');
d60_2 = extractUser(d60_1);
d60_3 = extractWs(d60_2);
d60_4 = productUsageTimeList(d60_3, '$inactiveInterval');
d60 = domainsByTimeSpent(d60_4);

r60_1 = JOIN r30 BY user LEFT, d60 BY user;
r60 = FOREACH r60_1 GENERATE r30::user AS user, r30::time1d AS time1d, r30::time7d AS time7d, r30::time30d AS time30d, d60::time AS time60d;

---------------------------------------------------------------------------
-- Calculate for 90D
---------------------------------------------------------------------------
d90_1 = filterByDateInterval(lR, '$toDate', 'P90D');
d90_2 = extractUser(d90_1);
d90_3 = extractWs(d90_2);
d90_4 = productUsageTimeList(d90_3, '$inactiveInterval');
d90 = domainsByTimeSpent(d90_4);

r90_1 = JOIN r60 BY user LEFT, d90 BY user;
r90 = FOREACH r90_1 GENERATE r60::user AS user, r60::time1d AS time1d, r60::time7d AS time7d, r60::time30d AS time30d, r60::time60d AS time60d, 
			    d90::time AS time90d;

---------------------------------------------------------------------------
-- Calculate for 360D
---------------------------------------------------------------------------
d360_1 = filterByDateInterval(lR, '$toDate', 'P365D');
d360_2 = extractUser(d360_1);
d360_3 = extractWs(d360_2);
d360_4 = productUsageTimeList(d360_3, '$inactiveInterval');
d360 = domainsByTimeSpent(d360_4);

r360_1 = JOIN r90 BY user LEFT, d360 BY user;
r360 = FOREACH r360_1 GENERATE r90::user AS user, r90::time1d AS time1d, r90::time7d AS time7d, r90::time30d AS time30d, r90::time60d AS time60d, 
			    r90::time90d AS time90d, d360::time AS time360d;

---------------------------------------------------------------------------
-- Calculate for LIFETIME
---------------------------------------------------------------------------
dlt_1 = filterByDateInterval(lR, '$toDate', 'P100Y');
dlt_2 = extractUser(dlt_1);
dlt_3 = extractWs(dlt_2);
dlt_4 = productUsageTimeList(dlt_3, '$inactiveInterval');
dlt = domainsByTimeSpent(dlt_4);

rlt_1 = JOIN r360 BY user LEFT, dlt BY user;
rlt = FOREACH rlt_1 GENERATE r360::user AS user, dlt::count AS count, r360::time1d AS time1d, r360::time7d AS time7d, r360::time30d AS time30d, 
			r360::time60d AS time60d, r360::time90d AS time90d, r360::time360d AS time360d, dlt::time AS timelt;

r1 = FOREACH rlt GENERATE user, count, (time1d IS NULL ? 0 : time1d) AS time1d, (time7d IS NULL ? 0 : time7d) AS time7d,
	(time30d IS NULL ? 0 : time30d) AS time30d, (time60d IS NULL ? 0 : time60d) AS time60d, (time90d IS NULL ? 0 : time90d) AS time90d,
	(time360d IS NULL ? 0 : time360d) AS time360d, (timelt IS NULL ? 0 : timelt) AS timelt;

result = FOREACH r1 GENERATE TOTUPLE(TOTUPLE(user), TOTUPLE(count), TOTUPLE(time1d/60), TOTUPLE(time7d/60), TOTUPLE(time30d/60), TOTUPLE(time60d/60), 
		TOTUPLE(time90d/60), TOTUPLE(time360d/60), TOTUPLE(timelt/60));




