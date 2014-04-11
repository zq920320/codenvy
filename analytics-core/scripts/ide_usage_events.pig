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
f = filterByEvent(l, 'ide-usage');

a1 = extractParam(f, 'ACTION', 'action');
a2 = extractParam(a1, 'SOURCE', 'source');
a3 = extractParam(a2, 'PROJECT', 'project');
a4 = extractParam(a3, 'TYPE', 'projectType');
a5 = extractParam(a4, 'PARAMETERS', 'parameters');
a = FOREACH a5 GENERATE dt, ws, user, action, source, project, projectType, parameters, ide;

result = FOREACH a GENERATE UUID(),
                            TOTUPLE('date', ToMilliSeconds(dt)),
                            TOTUPLE('ws', ws),
                            TOTUPLE('user', user),
                            TOTUPLE('action', action),
                            TOTUPLE('source', source),
                            TOTUPLE('project', project),
                            TOTUPLE('project_type', projectType),
                            TOTUPLE('parameters', parameters), -- every key-value pair will be stored separately instead of whole parameter
                            TOTUPLE('ide', ide);

STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
