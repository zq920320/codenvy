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

r = filterByEvent(l, '$EVENT');
r = extractParam(r, '$PARAM', param);
r = FOREACH r GENERATE dt, ws, user, LOWER(param) AS param, event, message;
r = FILTER r BY param IS NOT NULL AND param != '';

r = FOREACH r GENERATE  UUID(),
                        TOTUPLE('date', ToMilliSeconds(dt)),
                        TOTUPLE('ws', ws),
                        TOTUPLE('user', user),
                        TOTUPLE('event', event),
                        TOTUPLE('params-only-remove-event', message),
                        TOTUPLE(param, 1L);

STORE r INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
