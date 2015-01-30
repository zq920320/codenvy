/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2015] Codenvy, S.A.
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

a1 = filterByEvent(l, 'factory-created');
a2 = extractUrlParam(a1, 'FACTORY-URL', 'factory');
a3 = extractParam(a2, 'TYPE', 'projectType');
a4 = extractUrlParam(a3, 'REPO-URL', 'repository');
a5 = extractOrgAndAffiliateId(a4);
a6 = extractParam(a5, 'PROJECT', 'project');
a7 = extractFactoryId(a6);
a = FOREACH a7 GENERATE dt,
                        ws,
                        user,
                        factory,
                        repository,
                        project,
                        projectType,
                        orgId,
                        affiliateId,
                        factoryId,
                        (factoryId IS NULL ? 0 : 1) AS encodedFactory;

result = FOREACH a GENERATE UUID(),
					TOTUPLE('date', ToMilliSeconds(dt)), 
					TOTUPLE('ws', ws), 
					TOTUPLE('user', user),
                    TOTUPLE('org_id', orgId), 
                    TOTUPLE('affiliate_id', affiliateId), 
                    TOTUPLE('project', project),
                    TOTUPLE('repository', repository), 
                    TOTUPLE('project_type', LOWER(projectType)), 
                    TOTUPLE('project_id', CreateProjectId(user, ws, project)),
                    TOTUPLE('factory', factory),
                    TOTUPLE('factory_id', factoryId),
                    TOTUPLE('encoded_factory', encodedFactory);

dump result;
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
