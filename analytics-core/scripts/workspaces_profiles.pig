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

----------------------------------------------------------------------------------
------------------------------ ws creation processing ----------------------------
----------------------------------------------------------------------------------
a1 = filterByEvent(l, 'workspace-created');
a2 = extractParam(a1, 'WS', 'wsName');
a = FOREACH a2 GENERATE dt,
                        ws,
                        wsName;

resultA = FOREACH a GENERATE ws,
                             TOTUPLE('date', ToMilliSeconds(dt)),
                             TOTUPLE('ws_name', LOWER(wsName)),
                             TOTUPLE('persistent_ws', (IsTemporaryWorkspaceByName(wsName) ? 0 : 1));
STORE resultA INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

----------------------------------------------------------------------------------
------------------------------ ws updating processing ----------------------------
----------------------------------------------------------------------------------
b1 = filterByEvent(l, 'workspace-updated');
b2 = extractParam(b1, 'WS', 'wsName');
b = FOREACH b2 GENERATE dt, ws, wsName;

c1 = lastUpdate(b, 'ws');
c = FOREACH c1 GENERATE b::ws AS ws,
                        b::wsName AS wsName;

resultC = FOREACH c GENERATE ws,
                             TOTUPLE('ws_name', LOWER(wsName)),
                             TOTUPLE('persistent_ws', (IsTemporaryWorkspaceByName(wsName) ? 0 : 1));
STORE resultC INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
