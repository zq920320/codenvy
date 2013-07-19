IMPORT 'macros.pig';

%DEFAULT inactiveInterval '10';  -- in minutes

f1 = loadResources('$log');
--f2 = filterByDate(f1, '$FROM_DATE', '$TO_DATE');
f3 = extractUser(f1);
f = extractWs(f3);

g = GROUP f BY user;

--------------------------------------------------------------------
-- Calculates projects created, built and deployed numbers
--------------------------------------------------------------------
a = FOREACH g {
    pCreated = FILTER f BY INDEXOF('project-created', event, 0) >= 0;
    pBuilt = FILTER f BY INDEXOF('project-built,application-created,project-deployed', event, 0) >= 0;
    pDeployed = FILTER f BY INDEXOF('application-created,project-deployed', event, 0) >= 0;

    GENERATE group AS user, COUNT(pCreated) AS pCreatedNum, COUNT(pBuilt) AS pBuiltNum, COUNT(pDeployed) AS pDeployedNum;
}

--------------------------------------------------------------------
-- Calculates time usage numbers
--------------------------------------------------------------------
b1 = productUsageTimeList(f, '$inactiveInterval');
b2 = GROUP b1 BY user;
b = FOREACH b2 GENERATE group AS user, SUM(b1.delta) AS delta;

--------------------------------------------------------------------
-- Unions two results
--------------------------------------------------------------------
c = JOIN b BY user FULL, a BY user;
--r1 = FOREACH c GENERATE a::user AS user, a::pCreatedNum AS pCreatedNum1, a::pBuiltNum AS pBuiltNum1, a::pDeployedNum AS pDeployedNum1, ((b::user IS NULL ? 0 : b::delta) / 60) AS time1;
result = FOREACH c GENERATE TOTUPLE(TOTUPLE(a::user), TOTUPLE(a::pCreatedNum), TOTUPLE(a::pBuiltNum), TOTUPLE(a::pDeployedNum), TOTUPLE((b::user IS NULL ? 0 : b::delta / 60)));

--------------------------------------------------------------------
-- Loads and unions with previous data
--------------------------------------------------------------------
--r2 = LOAD '$RESULT_DIR/PREV_ACTON' USING PigStorage() AS (user : chararray, pCreatedNum2 : long, pBuiltNum2 : long, pDeployedNum2 : long, time2 : long);
--r3 = JOIN r1 BY user FULL, r2 BY user;
--r4 = FOREACH r3 GENERATE (r1::user IS NULL ? r2::user : r1::user) AS user,
--			(r1::user IS NULL ? 0 : r1::pCreatedNum1) AS pCreatedNum1,
--			(r1::user IS NULL ? 0 : r1::pBuiltNum1) AS pBuiltNum1,
--			(r1::user IS NULL ? 0 : r1::pDeployedNum1) AS pDeployedNum1,
--			(r1::user IS NULL ? 0 : r1::time1) AS time1,
--			(r2::user IS NULL ? 0 : r2::pCreatedNum2) AS pCreatedNum2,
--			(r2::user IS NULL ? 0 : r2::pBuiltNum2) AS pBuiltNum2,
--			(r2::user IS NULL ? 0 : r2::pDeployedNum2) AS pDeployedNum2,
--			(r2::user IS NULL ? 0 : r2::time2) AS time2;
--r = FOREACH r4 GENERATE user, (pCreatedNum1 + pCreatedNum2) AS pCreatedNum, (pBuiltNum1 + pBuiltNum2) AS pBuiltNum, (pDeployedNum1 + pDeployedNum2) AS pDeployedNum, (time1 + time2) AS time;
--STORE r INTO '$RESULT_DIR/ACTON' USING PigStorage();
--result = FOREACH r GENERATE TOTUPLE(TOTUPLE(user), TOTUPLE(pCreatedNum), TOTUPLE(pBuiltNum), TOTUPLE(pDeployedNum), TOTUPLE(time));

