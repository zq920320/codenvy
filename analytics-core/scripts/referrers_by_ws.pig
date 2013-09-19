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

%DEFAULT inactiveInterval '10';

r = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

u1 = LOAD '$LOAD_DIR' USING PigStorage() AS (tmpWs : chararray, referrer : chararray, factoryUrl : chararray);
u = FOREACH u1 GENERATE tmpWs, (referrer IS NULL ? '' : referrer) AS referrer, factoryUrl;

s1 = combineSmallSessions(r, 'session-factory-started', 'session-factory-stopped');
s2 = FOREACH s1 GENERATE id AS id, ws AS ws, user AS user, dt AS dt, delta AS delta, (INDEXOF(UPPER(user), 'ANONYMOUSUSER_', 0) == 0 ? 0 : 1) AS auth;

-- founds out the corresponding referrer and factoryUrl
s3 = JOIN s2 BY ws, u BY tmpWs;
s4 = FOREACH s3 GENERATE s2::id AS id, s2::dt AS dt, s2::ws AS ws, s2::user AS user, s2::delta AS delta, u::factoryUrl AS factoryUrl, u::referrer AS referrer, s2::auth AS auth;
s = removeEmptyField(s4, 'referrer');

-- add mark if session was converted or wasn't
c1 = addEventIndicator(s, r, 'factory-project-imported', 'conv', '$inactiveInterval');
c = FOREACH c1 GENERATE t::s::id AS id, t::s::dt AS dt, t::s::ws AS ws, t::s::user AS user, t::s::delta AS delta,
			t::s::factoryUrl AS factoryUrl, t::s::referrer AS referrer, t::s::auth AS auth, t::conv AS conv;

-- add mark if build action was occurred or wasn't
d1 = addEventIndicator(c, r, 'project-built,build-started,project-deployed,application-created', 'bld', '$inactiveInterval');
d = FOREACH d1 GENERATE t::c::id AS id, t::c::dt AS dt, t::c::ws AS ws, t::c::user AS user, t::c::delta AS delta, 
			t::c::factoryUrl AS factoryUrl, t::c::referrer AS referrer, t::c::auth AS auth, t::c::conv AS conv, t::bld AS bld;

-- add mark if run action was occurred or wasn't
e1 = addEventIndicator(d, r, 'run-started', 'run', '$inactiveInterval');
e = FOREACH e1 GENERATE t::d::id AS id, t::d::dt AS dt, t::d::ws AS ws, t::d::user AS user, t::d::delta AS delta, 
			t::d::factoryUrl AS factoryUrl, t::d::referrer AS referrer, t::d::auth AS auth, t::d::conv AS conv, t::d::bld AS bld, t::run AS run;

-- add mark if deployment action was occurred or wasn't
f1 = addEventIndicator(e, r, 'application-created,project-deployed', 'dpl', '$inactiveInterval');
f = FOREACH f1 GENERATE t::e::id AS id, t::e::dt AS dt, t::e::ws AS ws, t::e::user AS user, t::e::delta AS delta, 
			t::e::factoryUrl AS factoryUrl, t::e::referrer AS referrer, t::e::auth AS auth, t::e::conv AS conv, t::e::bld AS bld, t::e::run AS run, t::dpl AS dpl;

-- groups sessions by referrer url;
g1 = GROUP f BY (ws, referrer);
g2 = FOREACH g1 GENERATE group.ws AS ws, group.referrer AS referrer,
                    SUM(f.delta) AS delta, 1L AS wsCount, COUNT(f) AS sesCount,
                    SUM(f.auth) AS auth, SUM(f.conv) AS conv, SUM(f.bld) AS bld, SUM(f.run) AS run, SUM(f.dpl) AS dpl;
g3 = GROUP g2 BY ws;
g4 = FOREACH g3 GENERATE group, FLATTEN(g2);
result = FOREACH g4 GENERATE group, TOBAG(TOTUPLE(g2::referrer, TOBAG(g2::delta, g2::wsCount, g2::sesCount,
                                                    g2::auth, g2::conv, g2::bld, g2::run, g2::dpl)));
