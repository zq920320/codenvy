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

f1 = extractParam(l, 'PROJECT', project);
f2 = extractParam(f1, 'TYPE', project_type);
f = FOREACH f2 GENERATE dt, ws, project, project_type, user, ide;

r = calculateTime(l, '$EVENT-started', '$EVENT-finished');

u = JOIN r BY (dt,user,ws,ide), f BY (dt,user,ws,ide);
result = FOREACH u GENERATE UUID(), 
                            TOTUPLE('date', ToMilliSeconds(r::dt)), 
                            TOTUPLE('ws', r::ws), 
                            TOTUPLE('user', r::user),
                            TOTUPLE('project', f::project),
                            TOTUPLE('project_type', f::project_type),
                            TOTUPLE('time', r::delta), 
                            TOTUPLE('ide', r::ide);

STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
