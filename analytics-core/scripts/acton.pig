IMPORT 'macros.pig';

%DEFAULT inactiveInterval '10';  -- in minutes

f1 = loadResources('$log');
f2 = filterByDate(f1, '$FROM_DATE', '$TO_DATE');

f = extractUser(f2);
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

t = extractWs(f);

--------------------------------------------------------------------
-- Calculates time usage numbers
--------------------------------------------------------------------
b1 = productUsageTimeList(t, '$inactiveInterval');
b2 = GROUP b1 BY user;
b = FOREACH b2 GENERATE group AS user, SUM(b1.delta) AS delta;

--------------------------------------------------------------------
-- Unions two results
--------------------------------------------------------------------
c = JOIN b BY user FULL, a BY user;
result = FOREACH c GENERATE TOTUPLE(TOTUPLE(a::user), TOTUPLE(a::pCreatedNum), TOTUPLE(a::pBuiltNum), TOTUPLE(a::pDeployedNum), TOTUPLE((b::user IS NULL ? 0 : b::delta) / 60));
