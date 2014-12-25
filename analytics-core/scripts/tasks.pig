/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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

%DEFAULT default_builder_memory_mb '1536';  /* setup default BUILDER MEMORY USAGE = 1.5 GB */
%DEFAULT default_editor_memory_mb '25';

l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

build_started = filterByEvent(l, 'build-started');
build_started = extractParam(build_started, 'PROJECT', project);
build_started = extractParam(build_started, 'TYPE', project_type);
build_started = extractParam(build_started, 'ID', id);
build_started = extractParam(build_started, 'TIMEOUT', timeout);

build_started = FOREACH build_started GENERATE dt,
                                               ws,
                                               user,
                                               project,
                                               project_type,
                                               id,
                                               (timeout != '-1' ? 'timeout' : 'always-on')  AS launch_type;
builds_started = FOREACH build_started GENERATE UUIDFrom(id),
                                              TOTUPLE('date', ToMilliSeconds(dt)),
                                              TOTUPLE('ws', ws),
                                              TOTUPLE('user', user),
                                              TOTUPLE('project', project),
                                              TOTUPLE('project_type', LOWER(project_type)),
                                              TOTUPLE('project_id', CreateProjectId(user, ws, project)),
                                              TOTUPLE('id', id),
                                              TOTUPLE('task_type', 'builder'),
                                              TOTUPLE('start_time', ToMilliSeconds(dt)),
                                              TOTUPLE('is_factory', (IsTemporaryWorkspaceById(ws) ? 1 : 0)),
                                              TOTUPLE('launch_type', launch_type);

build_finished = filterByEvent(l, 'build-finished');
build_finished = extractParam(build_finished, 'PROJECT', project);
build_finished = extractParam(build_finished, 'TYPE', project_type);
build_finished = extractParam(build_finished, 'ID', id);
build_finished = extractParam(build_finished, 'USAGE-TIME', usage_time_msec);
build_finished = extractParam(build_finished, 'MEMORY', memory_mb);
build_finished = extractParam(build_finished, 'FINISHED-NORMALLY', finished_normally);

build_finished = FOREACH build_finished GENERATE dt,
                                                 ws,
                                                 user,
                                                 project,
                                                 project_type,
                                                 id,
                                                 (long) usage_time_msec,
                                                 (long) (memory_mb IS NOT NULL ? memory_mb : '$default_builder_memory_mb') AS memory_mb,
                                                 (finished_normally == '0' ? 'timeout' : 'normal')  AS shutdown_type;

builds_finished = FOREACH build_finished GENERATE UUIDFrom(id),
                                                  TOTUPLE('ws', ws),
                                                  TOTUPLE('user', user),
                                                  TOTUPLE('project', project),
                                                  TOTUPLE('project_type', LOWER(project_type)),
                                                  TOTUPLE('project_id', CreateProjectId(user, ws, project)),
                                                  TOTUPLE('id', id),
                                                  TOTUPLE('task_type', 'builder'),
                                                  TOTUPLE('memory', memory_mb),
                                                  TOTUPLE('usage_time', usage_time_msec),
                                                  TOTUPLE('stop_time', ToMilliSeconds(dt)),
                                                  TOTUPLE('gigabyte_ram_hours', CalculateGigabyteRamHours(memory_mb, usage_time_msec)),
                                                  TOTUPLE('shutdown_type', shutdown_type);


runs_started = filterByEvent(l, 'run-started');
runs_started = extractParam(runs_started, 'PROJECT', project);
runs_started = extractParam(runs_started, 'TYPE', project_type);
runs_started = extractParam(runs_started, 'ID', id);
runs_started = extractParam(runs_started, 'LIFETIME', lifetime);

runs_started = FOREACH runs_started GENERATE dt,
                                           ws,
                                           user,
                                           project,
                                           project_type,
                                           id,
                                           (lifetime != '-1' ? 'timeout' : 'always-on')  AS launch_type;

runs_started = FOREACH runs_started GENERATE UUIDFrom(id),
                                              TOTUPLE('date', ToMilliSeconds(dt)),
                                              TOTUPLE('ws', ws),
                                              TOTUPLE('user', user),
                                              TOTUPLE('project', project),
                                              TOTUPLE('project_type', LOWER(project_type)),
                                              TOTUPLE('project_id', CreateProjectId(user, ws, project)),
                                              TOTUPLE('id', id),
                                              TOTUPLE('task_type', 'runner'),
                                              TOTUPLE('start_time', ToMilliSeconds(dt)),
                                              TOTUPLE('is_factory', (IsTemporaryWorkspaceById(ws) ? 1 : 0)),
                                              TOTUPLE('launch_type', launch_type);

runs_finished = filterByEvent(l, 'run-finished');
runs_finished = extractParam(runs_finished, 'ID', id);
runs_finished = extractParam(runs_finished, 'PROJECT', project);
runs_finished = extractParam(runs_finished, 'TYPE', project_type);
runs_finished = extractParam(runs_finished, 'USAGE-TIME', usage_time_msec);
runs_finished = extractParam(runs_finished, 'MEMORY', memory_mb);
runs_finished = extractParam(runs_finished, 'STOPPED-BY-USER', stopped_by_user);

runs_finished = FOREACH runs_finished GENERATE dt,
                                             id,
                                             ws,
                                             user,
                                             project,
                                             project_type,
                                             (long) usage_time_msec,
                                             (long) memory_mb,
                                             (stopped_by_user == '0' ? 'timeout' : 'user')  AS shutdown_type;

runs_finished = FOREACH runs_finished GENERATE UUIDFrom(id),
                                              TOTUPLE('ws', ws),
                                              TOTUPLE('user', user),
                                              TOTUPLE('project', project),
                                              TOTUPLE('project_type', LOWER(project_type)),
                                              TOTUPLE('project_id', CreateProjectId(user, ws, project)),
                                              TOTUPLE('id', id),
                                              TOTUPLE('task_type', 'runner'),
                                              TOTUPLE('memory', memory_mb),
                                              TOTUPLE('usage_time', usage_time_msec),
                                              TOTUPLE('stop_time', ToMilliSeconds(dt)),
                                              TOTUPLE('gigabyte_ram_hours', CalculateGigabyteRamHours(memory_mb, usage_time_msec)),
                                              TOTUPLE('shutdown_type', shutdown_type);


debugs_started = filterByEvent(l, 'debug-started');
debugs_started = extractParam(debugs_started, 'PROJECT', project);
debugs_started = extractParam(debugs_started, 'TYPE', project_type);
debugs_started = extractParam(debugs_started, 'ID', id);
debugs_started = extractParam(debugs_started, 'LIFETIME', lifetime);

debugs_started = FOREACH debugs_started GENERATE dt,
                                               ws,
                                               user,
                                               project,
                                               project_type,
                                               id,
                                               (lifetime != '-1' ? 'timeout' : 'always-on')  AS launch_type;

debugs_started = FOREACH debugs_started GENERATE UUIDFrom(id),
                                                  TOTUPLE('date', ToMilliSeconds(dt)),
                                                  TOTUPLE('ws', ws),
                                                  TOTUPLE('user', user),
                                                  TOTUPLE('project', project),
                                                  TOTUPLE('project_type', LOWER(project_type)),
                                                  TOTUPLE('project_id', CreateProjectId(user, ws, project)),
                                                  TOTUPLE('id', id),
                                                  TOTUPLE('task_type', 'debugger'),
                                                  TOTUPLE('start_time', ToMilliSeconds(dt)),
                                                  TOTUPLE('is_factory', (IsTemporaryWorkspaceById(ws) ? 1 : 0)),
                                                  TOTUPLE('launch_type', launch_type);

debugs_finished = filterByEvent(l, 'debug-finished');
debugs_finished = extractParam(debugs_finished, 'ID', id);
debugs_finished = extractParam(debugs_finished, 'PROJECT', project);
debugs_finished = extractParam(debugs_finished, 'TYPE', project_type);
debugs_finished = extractParam(debugs_finished, 'USAGE-TIME', usage_time_msec);
debugs_finished = extractParam(debugs_finished, 'MEMORY', memory_mb);
debugs_finished = extractParam(debugs_finished, 'STOPPED-BY-USER', stopped_by_user);

debugs_finished = FOREACH debugs_finished GENERATE dt,
                                                 id,
                                                 ws,
                                                 user,
                                                 project,
                                                 project_type,
                                                 (long) usage_time_msec,
                                                 (long) memory_mb,
                                                 (stopped_by_user == '0' ? 'timeout' : 'user')  AS shutdown_type;

debugs_finished = FOREACH debugs_finished GENERATE UUIDFrom(id),
                                                  TOTUPLE('ws', ws),
                                                  TOTUPLE('user', user),
                                                  TOTUPLE('project', project),
                                                  TOTUPLE('project_type', LOWER(project_type)),
                                                  TOTUPLE('project_id', CreateProjectId(user, ws, project)),
                                                  TOTUPLE('id', id),
                                                  TOTUPLE('task_type', 'debugger'),
                                                  TOTUPLE('memory', memory_mb),
                                                  TOTUPLE('usage_time', usage_time_msec),
                                                  TOTUPLE('stop_time', ToMilliSeconds(dt)),
                                                  TOTUPLE('gigabyte_ram_hours', CalculateGigabyteRamHours(memory_mb, usage_time_msec)),
                                                  TOTUPLE('shutdown_type', shutdown_type);


edits = getSessions(l, 'session-usage');
edits = extractParam(edits, 'PROJECT', project);
edits = extractParam(edits, 'TYPE', project_type);
edits = FOREACH edits GENERATE ws,
                               user,
                               project,
                               project_type,
                               sessionID,
                               (long) startTime,
                               (long) usageTime,
                               (long) endTime;

edits = FOREACH edits GENERATE sessionID,
                             TOTUPLE('date', startTime),
                             TOTUPLE('ws', ws),
                             TOTUPLE('user', user),
                             TOTUPLE('project', project),
                             TOTUPLE('project_type', LOWER(project_type)),
                             TOTUPLE('project_id', CreateProjectId(user, ws, project)),
                             TOTUPLE('id', sessionID),
                             TOTUPLE('task_type', 'editor'),
                             TOTUPLE('memory', $default_editor_memory_mb),
                             TOTUPLE('usage_time', usageTime),
                             TOTUPLE('start_time', startTime),
                             TOTUPLE('stop_time', endTime),
                             TOTUPLE('gigabyte_ram_hours', CalculateGigabyteRamHours((long) $default_editor_memory_mb, usageTime)),
                             TOTUPLE('is_factory', 0);

edits_in_factory = getSessions(l, 'session-factory-usage');
edits_in_factory = extractParam(edits_in_factory, 'PROJECT', project);
edits_in_factory = extractParam(edits_in_factory, 'TYPE', project_type);

edits_in_factory = FOREACH edits_in_factory GENERATE ws,
                                                     user,
                                                     project,
                                                     project_type,
                                                     sessionID,
                                                     (long) startTime,
                                                     (long) usageTime,
                                                     (long) endTime;

edits_in_factory = FOREACH edits_in_factory GENERATE sessionID,
                                                   TOTUPLE('date', startTime),
                                                   TOTUPLE('ws', ws),
                                                   TOTUPLE('user', user),
                                                   TOTUPLE('project', project),
                                                   TOTUPLE('project_type', LOWER(project_type)),
                                                   TOTUPLE('project_id', CreateProjectId(user, ws, project)),
                                                   TOTUPLE('id', sessionID),
                                                   TOTUPLE('task_type', 'editor'),
                                                   TOTUPLE('memory', $default_editor_memory_mb),
                                                   TOTUPLE('usage_time', usageTime),
                                                   TOTUPLE('start_time', startTime),
                                                   TOTUPLE('stop_time', endTime),
                                                   TOTUPLE('gigabyte_ram_hours', CalculateGigabyteRamHours((long) $default_editor_memory_mb, usageTime)),
                                                   TOTUPLE('is_factory', 1);

STORE edits INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
STORE edits_in_factory INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
STORE builds_started INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
STORE builds_finished INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
STORE runs_started INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
STORE runs_finished INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
STORE debugs_started INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
STORE debugs_finished INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
