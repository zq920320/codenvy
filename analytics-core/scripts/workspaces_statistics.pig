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

l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

-- Calculates projects created, built and deployed numbers
a1 = GROUP l BY user;
a2 = FOREACH a1 {
    pCreated = FILTER l BY INDEXOF('project-created', event, 0) >= 0;
    pDestroyed = FILTER l BY INDEXOF('project-destroyed', event, 0) >= 0;
    pBuilt = FILTER l BY INDEXOF('project-built,application-created,project-deployed', event, 0) >= 0;
    pRun = FILTER l BY INDEXOF('run-started', event, 0) >= 0;
    pDebug = FILTER l BY INDEXOF('debug-started', event, 0) >= 0;
    pDeployed = FILTER l BY INDEXOF('application-created,project-deployed', event, 0) >= 0;
    pFactories = FILTER l BY INDEXOF('factory-created', event, 0) >= 0;

    GENERATE group AS id, COUNT(pCreated) AS pCreated, COUNT(pDestroyed) AS pDestroyed, COUNT(pRun) AS runs,
                            COUNT(pBuilt) AS builds, COUNT(pDeployed) AS deploys, COUNT(pDebug) AS debugs,
                            COUNT(pFactories) AS factories;
}
a3 = FOREACH a2 GENERATE id, (pCreated - pDestroyed) AS projects, builds, deploys, runs, debugs, factories;
a = FILTER a3 BY projects != 0 OR builds != 0 OR deploys != 0 OR runs != 0 OR debugs != 0 OR factories != 0;

b = LOAD '$STORAGE_URL.$STORAGE_TABLE' USING MongoLoader('$STORAGE_USER', '$STORAGE_PASSWORD', 'id: chararray, projects: long, builds: long, deploys: long, runs: long, debugs: long, factories: long');

c1 = JOIN a BY id LEFT, b BY id;
c2 = FOREACH c1 GENERATE a::id AS id,
                    (a::projects IS NULL ? 0 : a::projects) AS a::projects,
                    (b::projects IS NULL ? 0 : b::projects) AS b::projects,
                    (a::builds IS NULL ? 0 : a::builds) AS a::builds,
                    (b::builds IS NULL ? 0 : b::builds) AS b::builds,
                    (a::runs IS NULL ? 0 : a::runs) AS a::runs,
                    (b::runs IS NULL ? 0 : b::runs) AS b::runs,
                    (a::debugs IS NULL ? 0 : a::debugs) AS a::debugs,
                    (b::debugs IS NULL ? 0 : b::debugs) AS b::debugs,
                    (a::factories IS NULL ? 0 : a::factories) AS a::factories,
                    (b::factories IS NULL ? 0 : b::factories) AS b::factories,
                    (a::deploys IS NULL ? 0 : a::deploys) AS a::deploys,
                    (b::deploys IS NULL ? 0 : b::deploys) AS b::deploys;
c3 = FOREACH c2 GENERATE id,
                         (a::projects + b::projects) AS projects,
                         (a::builds + b::builds) AS builds,
                         (a::deploys + b::deploys) AS deploys,
                         (a::runs + b::runs) AS runs,
                         (a::factories + b::factories) AS factories,
                         (a::debugs + b::debugs) AS debugs;
c = FOREACH c3 GENERATE id, (projects < 0 ? 0 : projects) AS projects, builds, deploys, runs, debugs, factories;

result = FOREACH c GENERATE id, TOTUPLE('user_email', id), TOTUPLE('projects', projects), TOTUPLE('builds', builds),
        TOTUPLE('deploys', deploys), TOTUPLE('runs', runs), TOTUPLE('debugs', debugs), TOTUPLE('factories', factories);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');
