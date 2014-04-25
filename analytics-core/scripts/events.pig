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
f1 = filterByEvent(l, '$EVENT');
f2 = extractParam(f1, 'PROJECT', project);
f = extractParam(f2, 'TYPE', project_type);

r1 = FOREACH f GENERATE dt, ws, project, project_type, user, ide;
result = FOREACH r1 GENERATE UUID(), 
                             TOTUPLE('date', ToMilliSeconds(dt)), 
                             TOTUPLE('ws', ws), 
                             TOTUPLE('user', user),                            
                             TOTUPLE('project', project),
                             TOTUPLE('project_type', LOWER(project_type)),
                             TOTUPLE('project_id', CreateProjectId(user, ws, project)),
                             TOTUPLE('value', 1L), 
                             TOTUPLE('ide', ide);

STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;