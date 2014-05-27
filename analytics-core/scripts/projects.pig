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

l = loadResources('$STORAGE_URL', '$STORAGE_TABLE_USERS_PROFILES', '$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

f1 = filterByEvent(l, 'project-created');
f2 = extractParam(f1, 'PROJECT', project);
f3 = extractParam(f2, 'TYPE', project_type);

f = FOREACH f3 GENERATE dt, user, ws, event, project, project_type, ide;

result = FOREACH f GENERATE UUID(),
                            TOTUPLE('date', ToMilliSeconds(dt)),
                            TOTUPLE('user', user),
                            TOTUPLE('ws', ws),
                            TOTUPLE('project', project),
                            TOTUPLE('project_type', LOWER(project_type)),
                            TOTUPLE('project_id', CreateProjectId(user, ws, project)),
                            TOTUPLE('ide', ide);

STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;