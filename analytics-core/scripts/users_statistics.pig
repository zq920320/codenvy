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
    pDeployed = FILTER l BY INDEXOF('application-created,project-deployed', event, 0) >= 0;

    GENERATE group AS id, COUNT(pCreated) AS pCreated, COUNT(pDestroyed) AS pDestroyed,
                            COUNT(pBuilt) AS builds, COUNT(pDeployed) AS deploys;
}
a3 = FOREACH a2 GENERATE id, (pCreated - pDestroyed) AS projects, builds, deploys;
a = FILTER a3 BY projects != 0 OR builds != 0 OR deploys != 0;

b = LOAD '$STORAGE_URL.$STORAGE_TABLE' USING MongoLoader('id: chararray, projects: long, builds: long, deploys: long');

c1 = JOIN a BY id LEFT, b BY id;
c2 = FOREACH c1 GENERATE a::id AS id,
                    (a::projects IS NULL ? 0 : a::projects) AS a::projects,
                    (b::projects IS NULL ? 0 : b::projects) AS b::projects,
                    (a::builds IS NULL ? 0 : a::builds) AS a::builds,
                    (b::builds IS NULL ? 0 : b::builds) AS b::builds,
                    (a::deploys IS NULL ? 0 : a::deploys) AS a::deploys,
                    (b::deploys IS NULL ? 0 : b::deploys) AS b::deploys;
c3 = FOREACH c2 GENERATE id,
                         (a::projects + b::projects) AS projects,
                         (a::builds + b::builds) AS builds,
                         (a::deploys + b::deploys) AS deploys;
c = FOREACH c3 GENERATE id, (projects < 0 ? 0 : projects) AS projects, builds, deploys;

result = FOREACH c GENERATE id, TOTUPLE('user_email', id), TOTUPLE('projects', projects), TOTUPLE('builds', builds), TOTUPLE('deploys', deploys);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage();
