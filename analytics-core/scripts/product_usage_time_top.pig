/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

 IMPORT 'macros.pig';

%DEFAULT inactiveInterval '10';  -- in minutes
%DEFAULT top '100';

---------------------------------------------------------------------------
-- Finds best for period
---------------------------------------------------------------------------
a1 = LOAD '$RESULT_DIR/$ENTITY/$INTERVAL' USING PigStorage() AS (user: chararray, count: long, time: long);
a2 = GROUP a1 ALL;
a3 = FOREACH a2 GENERATE FLATTEN(TOP((int) $top, 2, a1));
aR = FOREACH a3 GENERATE a1::user AS user;

---------------------------------------------------------------------------
-- Calculate for 1D
---------------------------------------------------------------------------
d1 = LOAD '$RESULT_DIR/$ENTITY/P1D' USING PigStorage() AS (user: chararray, count: long, time: long);
r1_1 = JOIN aR BY user LEFT, d1 BY user;
r1 = FOREACH r1_1 GENERATE aR::user AS user, d1::time AS time1d;

---------------------------------------------------------------------------
-- Calculate for 7D
---------------------------------------------------------------------------
d7 = LOAD '$RESULT_DIR/$ENTITY/P7D' USING PigStorage() AS (user: chararray, count: long, time: long);
r7_1 = JOIN r1 BY user LEFT, d7 BY user;
r7 = FOREACH r7_1 GENERATE r1::user AS user, r1::time1d AS time1d, d7::time AS time7d;


---------------------------------------------------------------------------
-- Calculate for 30D
---------------------------------------------------------------------------
d30 = LOAD '$RESULT_DIR/$ENTITY/P30D' USING PigStorage() AS (user: chararray, count: long, time: long);
r30_1 = JOIN r7 BY user LEFT, d30 BY user;
r30 = FOREACH r30_1 GENERATE r7::user AS user, r7::time1d AS time1d, r7::time7d AS time7d, d30::time AS time30d;

---------------------------------------------------------------------------
-- Calculate for 60D
---------------------------------------------------------------------------
d60 = LOAD '$RESULT_DIR/$ENTITY/P60D' USING PigStorage() AS (user: chararray, count: long, time: long);
r60_1 = JOIN r30 BY user LEFT, d60 BY user;
r60 = FOREACH r60_1 GENERATE r30::user AS user, r30::time1d AS time1d, r30::time7d AS time7d, r30::time30d AS time30d, d60::time AS time60d;

---------------------------------------------------------------------------
-- Calculate for 90D
---------------------------------------------------------------------------
d90 = LOAD '$RESULT_DIR/$ENTITY/P90D' USING PigStorage() AS (user: chararray, count: long, time: long);
r90_1 = JOIN r60 BY user LEFT, d90 BY user;
r90 = FOREACH r90_1 GENERATE r60::user AS user, r60::time1d AS time1d, r60::time7d AS time7d, r60::time30d AS time30d, r60::time60d AS time60d, 
			    d90::time AS time90d;

---------------------------------------------------------------------------
-- Calculate for 365D
---------------------------------------------------------------------------
d360 = LOAD '$RESULT_DIR/$ENTITY/P365D' USING PigStorage() AS (user: chararray, count: long, time: long);
r360_1 = JOIN r90 BY user LEFT, d360 BY user;
r360 = FOREACH r360_1 GENERATE r90::user AS user, r90::time1d AS time1d, r90::time7d AS time7d, r90::time30d AS time30d, r90::time60d AS time60d, 
			    r90::time90d AS time90d, d360::time AS time360d;

---------------------------------------------------------------------------
-- Calculate for LIFETIME
---------------------------------------------------------------------------
dlt = LOAD '$RESULT_DIR/$ENTITY/P100Y' USING PigStorage() AS (user: chararray, count: long, time: long);
rlt_1 = JOIN r360 BY user LEFT, dlt BY user;
rlt = FOREACH rlt_1 GENERATE r360::user AS user, dlt::count AS count, r360::time1d AS time1d, r360::time7d AS time7d, r360::time30d AS time30d, 
			r360::time60d AS time60d, r360::time90d AS time90d, r360::time360d AS time360d, dlt::time AS timelt;

r1 = FOREACH rlt GENERATE user, count, (time1d IS NULL ? 0 : time1d) AS time1d, (time7d IS NULL ? 0 : time7d) AS time7d,
	(time30d IS NULL ? 0 : time30d) AS time30d, (time60d IS NULL ? 0 : time60d) AS time60d, (time90d IS NULL ? 0 : time90d) AS time90d,
	(time360d IS NULL ? 0 : time360d) AS time360d, (timelt IS NULL ? 0 : timelt) AS timelt;

result = FOREACH r1 GENERATE TOTUPLE(TOTUPLE(user), TOTUPLE(count), TOTUPLE(time1d/60), TOTUPLE(time7d/60), TOTUPLE(time30d/60), TOTUPLE(time60d/60), 
		TOTUPLE(time90d/60), TOTUPLE(time360d/60), TOTUPLE(timelt/60));




