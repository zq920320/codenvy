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

l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

u1 = filterByEvent(l, 'factory-url-accepted');
u2 = extractUrlParam(u1, 'REFERRER', 'referrer');
u3 = extractUrlParam(u2, 'FACTORY-URL', 'factory');
u4 = extractUrlParam(u3, 'ORG-ID', 'orgId');
u5 = extractUrlParam(u4, 'AFFILIATE-ID', 'affiliateId');
u = FOREACH u5 GENERATE ws AS tmpWs, referrer, factory, orgId, affiliateId;

---- finds out all imported projects
i1 = filterByEvent(l, 'factory-project-imported');
i2 = FOREACH i1 GENERATE dt, ws AS tmpWs, user;
i3 = GROUP i2 BY (tmpWs, user);
i = FOREACH i3 GENERATE MIN(i2.dt) AS dt, group.tmpWs AS tmpWs, group.user AS user;

-- users could work anonymously and their factory sessions associated with those names
-- so, lets find their name before 'user-changed-name' has been occurred
c1 = filterByEvent(l, 'user-changed-name');
c2 = extractParam(c1, 'OLD-USER', oldUser);
c3 = extractParam(c2, 'NEW-USER', newUser);
c = FOREACH c3 GENERATE oldUser AS anomUser, newUser AS user;

-- associating anonymous names with 'factory-project-imported' events
d1 = JOIN i BY user LEFT, c BY user;
d2 = FOREACH d1 GENERATE i::dt AS dt, i::tmpWs AS tmpWs, (c::user IS NULL ? i::user : c::anomUser) AS user;

-- combining all possible combination, redundant ones will be screened later
d3 = UNION d2, i;
d = DISTINCT d3;

-- factory sessions themselves
s1 = combineSmallSessions(l, 'session-factory-started', 'session-factory-stopped');
s2 = FOREACH s1 GENERATE dt, ws AS tmpWs, user AS tmpUser, delta, (INDEXOF(UPPER(user), 'ANONYMOUSUSER_', 0) == 0 ? 0 : 1) AS auth;

-- founds out the corresponding referrer and factory
s3 = JOIN s2 BY tmpWs LEFT, u BY tmpWs;
s4 = FOREACH s3 GENERATE s2::dt AS dt, s2::tmpWs AS tmpWs, s2::tmpUser AS user, s2::delta AS delta, s2::auth AS auth,
        u::factory AS factory, u::referrer AS referrer, u::orgId AS orgId, u::affiliateId AS affiliateId;

-- founds out if factory session was converted or wasn't
-- (if importing operation was inside a session)
s5 = JOIN s4 BY (tmpWs, user) LEFT, d BY (tmpWs, user);
s = FOREACH s5 GENERATE s4::dt AS dt, s4::delta AS delta, s4::factory AS factory, s4::referrer AS referrer, s4::user AS user,
                        s4::orgId AS orgId, s4::affiliateId AS affiliateId, s4::auth AS auth, s4::tmpWs AS ws,
			            (d::tmpWs IS NULL ? 0
			                              : (SecondsBetween(s4::dt, d::dt) + s4::delta + (long) $inactiveInterval * 60  > 0 ? 1 : 0 )) AS conv;

-- sessions with events
k1 = addEventIndicator(s, l,  'run-started', 'run', '$inactiveInterval');
k = FOREACH k1 GENERATE t::s::dt AS dt, t::s::delta AS delta, t::s::factory AS factory, t::s::referrer AS referrer,
                        t::s::orgId AS orgId, t::s::affiliateId AS affiliateId, t::s::auth AS auth, t::s::ws AS ws,
                        t::s::user AS user, t::s::conv AS conv, t::run AS run;

m1 = addEventIndicator(k, l,  'project-deployed,application-created', 'deploy', '$inactiveInterval');
m = FOREACH m1 GENERATE t::k::dt AS dt, t::k::delta AS delta, t::k::factory AS factory, t::k::referrer AS referrer,
                        t::k::orgId AS orgId, t::k::affiliateId AS affiliateId, t::k::auth AS auth, t::k::ws AS ws,
                        t::k::user AS user, t::k::conv AS conv, t::k::run AS run, t::deploy AS deploy;

n1 = addEventIndicator(m, l,  'project-built,project-deployed,application-created,build-started', 'build', '$inactiveInterval');
n = FOREACH n1 GENERATE t::m::dt AS dt, t::m::delta AS delta, t::m::factory AS factory, t::m::referrer AS referrer,
                        t::m::orgId AS orgId, t::m::affiliateId AS affiliateId, t::m::auth AS auth, t::m::ws AS ws,
                        t::m::user AS user, t::m::conv AS conv, t::m::run AS run, t::m::deploy AS deploy, t::build AS build;

r1 = FOREACH n GENERATE dt, ws, user, factory, referrer, auth, conv,
                        orgId, affiliateId, delta, deploy, build, run;

result = FOREACH r1 GENERATE ToMilliSeconds(dt), TOTUPLE('ws', ws), TOTUPLE('user', user),
                        TOTUPLE('run', run), TOTUPLE('deploy', deploy), TOTUPLE('build', build),
                        TOTUPLE('factory', factory), TOTUPLE('referrer', referrer), TOTUPLE('org_id', orgId), TOTUPLE('affiliate_id', affiliateId),
                        TOTUPLE('authenticated_factory_session', auth), TOTUPLE('converted_factory_session', conv), TOTUPLE('time', delta);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');