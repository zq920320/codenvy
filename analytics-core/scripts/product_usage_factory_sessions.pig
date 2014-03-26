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
u4 = extractOrgAndAffiliateId(u3);
u = FOREACH u4 GENERATE ws AS tmpWs, ExtractDomain(referrer) AS referrer, factory, orgId, affiliateId;

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
c = FOREACH c3 GENERATE LOWER(oldUser) AS anomUser, LOWER(newUser) AS user;

-- associating anonymous names with 'factory-project-imported' events
d1 = JOIN i BY user LEFT, c BY user;
d2 = FOREACH d1 GENERATE i::dt AS dt, i::tmpWs AS tmpWs, (c::user IS NULL ? i::user : c::anomUser) AS user;

-- combining all possible combination, redundant ones will be screened later
d3 = UNION d2, i;
d = DISTINCT d3;

-- factory sessions themselves
s1 = combineSmallSessions(l, 'session-factory-started', 'session-factory-stopped');
s2 = FOREACH s1 GENERATE dt, id, ws AS tmpWs, user AS tmpUser, delta, (INDEXOF(user, 'anonymoususer_', 0) == 0 ? 0 : 1) AS auth, ide;

-- founds out the corresponding referrer and factory
s3 = JOIN s2 BY tmpWs LEFT, u BY tmpWs;
s4 = FOREACH s3 GENERATE s2::dt AS dt, s2::tmpWs AS tmpWs, s2::tmpUser AS user, s2::delta AS delta, s2::auth AS auth, s2::ide AS ide, s2::id AS id,
        (u::tmpWs IS NULL ? '' : u::factory) AS factory, (u::tmpWs IS NULL ? '' : u::referrer) AS referrer,
        (u::tmpWs IS NULL ? '' : u::orgId) AS orgId,  (u::tmpWs IS NULL ? '' : u::affiliateId) AS affiliateId;

-- founds out if factory session was converted or wasn't
-- (if importing operation was inside a session)
s5 = JOIN s4 BY (tmpWs, user) LEFT, d BY (tmpWs, user);
s = FOREACH s5 GENERATE s4::dt AS dt, s4::delta AS delta, s4::factory AS factory, s4::referrer AS referrer, s4::user AS user,
                        s4::orgId AS orgId, s4::affiliateId AS affiliateId, s4::auth AS auth, s4::tmpWs AS ws, s4::ide AS ide, s4::id AS id,
                        (d::tmpWs IS NULL ? 0
                                          : (MilliSecondsBetween(s4::dt, d::dt) + s4::delta + (long) $inactiveInterval*60*1000  > 0 ? 1 : 0 )) AS conv;

-- sessions with events
k1 = addEventIndicator(s, l,  'run-started', 'run', '$inactiveInterval');
k = FOREACH k1 GENERATE t::s::dt AS dt, t::s::delta AS delta, t::s::factory AS factory, t::s::referrer AS referrer,
                        t::s::orgId AS orgId, t::s::affiliateId AS affiliateId, t::s::auth AS auth, t::s::ws AS ws,
                        t::s::user AS user, t::s::conv AS conv, t::s::ide AS ide, t::run AS run, t::s::id AS id;

m1 = addEventIndicator(k, l,  'project-deployed,application-created', 'deploy', '$inactiveInterval');
m = FOREACH m1 GENERATE t::k::dt AS dt, t::k::delta AS delta, t::k::factory AS factory, t::k::referrer AS referrer,
                        t::k::orgId AS orgId, t::k::affiliateId AS affiliateId, t::k::auth AS auth, t::k::ws AS ws, t::k::id AS id,
                        t::k::user AS user, t::k::conv AS conv, t::k::run AS run, t::k::ide AS ide, t::deploy AS deploy;

n1 = addEventIndicator(m, l,  'project-built,project-deployed,application-created,build-started', 'build', '$inactiveInterval');
n = FOREACH n1 GENERATE t::m::dt AS dt, t::m::delta AS delta, t::m::factory AS factory, t::m::referrer AS referrer, t::m::id AS id,
                        t::m::orgId AS orgId, t::m::affiliateId AS affiliateId, t::m::auth AS auth, t::m::ws AS ws, t::m::ide AS ide,
                        t::m::user AS user, t::m::conv AS conv, t::m::run AS run, t::m::deploy AS deploy, t::build AS build;

-- add created temporary session indicator
w = createdTemporaryWorkspaces(l);
z1 = JOIN n BY (ws, user) FULL, w BY (ws, user);
z2 = FOREACH z1 GENERATE (n::ws IS NULL ? w::dt : n::dt) AS dt,
    (n::ws IS NULL ? w::ide : n::ide) AS ide,
    (n::ws IS NULL ? '' : n::id) AS id,
    (n::ws IS NULL ? 0 : n::delta) AS delta,
    (n::ws IS NULL ? w::factory : n::factory) AS factory,
    (n::ws IS NULL ? w::referrer : n::referrer) AS referrer,
    (n::ws IS NULL ? w::orgId : n::orgId) AS orgId,
    (n::ws IS NULL ? w::affiliateId : n::affiliateId) AS affiliateId,
    (n::ws IS NULL ? (INDEXOF(w::user, 'anonymoususer_', 0) == 0 ? 0 : 1) : n::auth) AS auth,
    (n::ws IS NULL ? w::ws : n::ws) AS ws,
    (n::ws IS NULL ? w::user : n::user) AS user,
    (n::ws IS NULL ? 0 : n::conv) AS conv,
    (n::ws IS NULL ? 0 : n::run) AS run,
    (n::ws IS NULL ? 0 : n::deploy) AS deploy,
    (n::ws IS NULL ? 0 : n::build) AS build,
    (w::ws IS NULL ? 0 : 1) AS ws_created;

-- finds the first started sessions and keep indicator only there
z3 = GROUP z2 BY (ws, user);
z4 = FOREACH z3 GENERATE group.ws AS ws, group.user AS user, MIN(z2.dt) AS minDT, FLATTEN(z2);
z5 = FOREACH z4 GENERATE ws, user, z2::dt AS dt, z2::delta AS delta, z2::factory AS factory, z2::id AS id,
    z2::referrer AS referrer, z2::orgId AS orgId, z2::affiliateId AS affiliateId, z2::ide AS ide,
    z2::auth AS auth, z2::conv AS conv, z2::run AS run, z2::deploy AS deploy, z2::build AS build,
    (z2::dt == minDT ? z2::ws_created : 0) AS ws_created;
z = FOREACH z5 GENERATE ws, user AS user, dt, delta, factory, referrer, orgId, affiliateId, auth, conv, run, deploy, build, ws_created, ide, id;

-- add user created from factory indicator
ls1 = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', 'ANY', 'ANY');
ls2 = usersCreatedFromFactory(ls1);
ls = FOREACH ls2 GENERATE dt, ws, user, factory, referrer, orgId, affiliateId, tmpUser AS tmpUser, ide;

p1 = JOIN z BY (ws, user) FULL, ls BY (ws, tmpUser);
p2 = FOREACH p1 GENERATE (z::ws IS NULL ? ls::dt : z::dt) AS dt,
    (z::ws IS NULL ? 0 : z::delta) AS delta,
    (z::ws IS NULL ? ls::ide : z::ide) AS ide,
    (z::ws IS NULL ? '' : z::id) AS id,
    (z::ws IS NULL ? ls::factory : z::factory) AS factory,
    (z::ws IS NULL ? ls::referrer : z::referrer) AS referrer,
    (z::ws IS NULL ? ls::orgId : z::orgId) AS orgId,
    (z::ws IS NULL ? ls::affiliateId : z::affiliateId) AS affiliateId,
    (z::ws IS NULL ? 0 : z::auth) AS auth,
    (z::ws IS NULL ? ls::ws : z::ws) AS ws,
    (z::ws IS NULL ? ls::tmpUser : z::user) AS user,
    (z::ws IS NULL ? 0 : z::conv) AS conv,
    (z::ws IS NULL ? 0 : z::run) AS run,
    (z::ws IS NULL ? 0 : z::deploy) AS deploy,
    (z::ws IS NULL ? 0 : z::build) AS build,
    (z::ws IS NULL ? 0 : z::ws_created) AS ws_created,
    (ls::ws IS NULL ? 0 : 1) AS user_created;


-- finds the first started sessions and keep indicator only there
p3 = GROUP p2 BY (ws, user);
p4 = FOREACH p3 GENERATE group.ws AS ws, group.user AS user, MIN(p2.dt) AS minDT, FLATTEN(p2);
p = FOREACH p4 GENERATE ws, user, p2::dt AS dt, p2::delta AS delta, p2::factory AS factory, p2::id AS id,
    p2::referrer AS referrer, p2::orgId AS orgId, p2::affiliateId AS affiliateId, p2::ide AS ide,
    p2::auth AS auth, p2::conv AS conv, p2::run AS run, p2::deploy AS deploy, p2::build AS build,
    p2::ws_created AS ws_created, (p2::dt == minDT ? p2::user_created : 0) AS user_created,
    (INDEXOF(factory, 'factory?id=', 0) > 0 ? 1 : 0) AS encodedFactory;

result = FOREACH p GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('ws', ws), TOTUPLE('user', user), TOTUPLE('ide', ide),
                        TOTUPLE('run', run), TOTUPLE('deploy', deploy), TOTUPLE('build', build), TOTUPLE('ws_created', ws_created),
                        TOTUPLE('factory', factory), TOTUPLE('referrer', referrer), TOTUPLE('org_id', orgId), TOTUPLE('affiliate_id', affiliateId),
                        TOTUPLE('authenticated_factory_session', auth), TOTUPLE('converted_factory_session', conv), TOTUPLE('time', delta),
                        TOTUPLE('session_id', id), TOTUPLE('user_created', user_created), TOTUPLE('encoded_factory', encodedFactory);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

