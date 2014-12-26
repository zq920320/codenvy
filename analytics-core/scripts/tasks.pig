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

build_finished = filterByEvent(l, 'build-finished');
build_finished = extractParam(build_finished, 'ID', id);
build_finished = extractParam(build_finished, 'USAGE-TIME', usage_time_msec);
build_finished = extractParam(build_finished, 'MEMORY', memory_mb);
build_finished = extractParam(build_finished, 'FINISHED-NORMALLY', finished_normally);

build_finished = FOREACH build_finished GENERATE dt,
                                                 id,
                                                 (long) usage_time_msec,
                                                 (long) (memory_mb IS NOT NULL ? memory_mb : '$default_builder_memory_mb') AS memory_mb,
                                                 (finished_normally == '0' ? 'timeout' : 'normal')  AS shutdown_type;

builds = JOIN build_started BY id LEFT, build_finished BY id;

builds_table = FOREACH builds GENERATE UUID(),
                                      TOTUPLE('date', ToMilliSeconds(build_started::dt)),
                                      TOTUPLE('ws', LOWER(build_started::ws)),
                                      TOTUPLE('user', build_started::user),
                                      TOTUPLE('project', build_started::project),
                                      TOTUPLE('project_type', LOWER(build_started::project_type)),
                                      TOTUPLE('project_id', CreateProjectId(build_started::user, build_started::ws, build_started::project)),
                                      TOTUPLE('id', build_started::id),
                                      TOTUPLE('task_type', 'builder'),
                                      TOTUPLE('memory', build_finished::memory_mb),
                                      TOTUPLE('usage_time', build_finished::usage_time_msec),
                                      TOTUPLE('start_time', ToMilliSeconds(build_started::dt)),
                                      TOTUPLE('stop_time', ToMilliSeconds(build_finished::dt)),
                                      TOTUPLE('gigabyte_ram_hours', CalculateGigabyteRamHours(build_finished::memory_mb, build_finished::usage_time_msec)),
                                      TOTUPLE('is_factory', (IsTemporaryWorkspaceById(build_started::ws) ? 'yes' : 'no')),
                                      TOTUPLE('launch_type', build_started::launch_type),
                                      TOTUPLE('shutdown_type', build_finished::shutdown_type);


run_started = filterByEvent(l, 'run-started');
run_started = extractParam(run_started, 'PROJECT', project);
run_started = extractParam(run_started, 'TYPE', project_type);
run_started = extractParam(run_started, 'ID', id);
run_started = extractParam(run_started, 'LIFETIME', lifetime);

run_started = FOREACH run_started GENERATE dt,
                                           ws,
                                           user,
                                           project,
                                           project_type,
                                           id,
                                           (lifetime != '-1' ? 'timeout' : 'always-on')  AS launch_type;

run_finished = filterByEvent(l, 'run-finished');
run_finished = extractParam(run_finished, 'ID', id);
run_finished = extractParam(run_finished, 'USAGE-TIME', usage_time_msec);
run_finished = extractParam(run_finished, 'MEMORY', memory_mb);
run_finished = extractParam(run_finished, 'STOPPED-BY-USER', stopped_by_user);

run_finished = FOREACH run_finished GENERATE dt,
                                             id,
                                             (long) usage_time_msec,
                                             (long) memory_mb,
                                             (stopped_by_user == '0' ? 'timeout' : 'user')  AS shutdown_type;

runs = JOIN run_started BY id LEFT, run_finished BY id;

runs_table = FOREACH runs GENERATE UUID(),
                                  TOTUPLE('date', ToMilliSeconds(run_started::dt)),
                                  TOTUPLE('ws', LOWER(run_started::ws)),
                                  TOTUPLE('user', run_started::user),
                                  TOTUPLE('project', run_started::project),
                                  TOTUPLE('project_type', LOWER(run_started::project_type)),
                                  TOTUPLE('project_id', CreateProjectId(run_started::user, run_started::ws, run_started::project)),
                                  TOTUPLE('id', run_started::id),
                                  TOTUPLE('task_type', 'runner'),
                                  TOTUPLE('memory', run_finished::memory_mb),
                                  TOTUPLE('usage_time', run_finished::usage_time_msec),
                                  TOTUPLE('start_time', ToMilliSeconds(run_started::dt)),
                                  TOTUPLE('stop_time', ToMilliSeconds(run_finished::dt)),
                                  TOTUPLE('gigabyte_ram_hours', CalculateGigabyteRamHours(run_finished::memory_mb, run_finished::usage_time_msec)),
                                  TOTUPLE('is_factory', (IsTemporaryWorkspaceById(run_started::ws) ? 'yes' : 'no')),
                                  TOTUPLE('launch_type', run_started::launch_type),
                                  TOTUPLE('shutdown_type', run_finished::shutdown_type);


debug_started = filterByEvent(l, 'debug-started');
debug_started = extractParam(debug_started, 'PROJECT', project);
debug_started = extractParam(debug_started, 'TYPE', project_type);
debug_started = extractParam(debug_started, 'ID', id);
debug_started = extractParam(debug_started, 'LIFETIME', lifetime);

debug_started = FOREACH debug_started GENERATE dt,
                                               ws,
                                               user,
                                               project,
                                               project_type,
                                               id,
                                               (lifetime != '-1' ? 'timeout' : 'always-on')  AS launch_type;

debug_finished = filterByEvent(l, 'debug-finished');
debug_finished = extractParam(debug_finished, 'ID', id);
debug_finished = extractParam(debug_finished, 'USAGE-TIME', usage_time_msec);
debug_finished = extractParam(debug_finished, 'MEMORY', memory_mb);
debug_finished = extractParam(debug_finished, 'STOPPED-BY-USER', stopped_by_user);

debug_finished = FOREACH debug_finished GENERATE dt,
                                             id,
                                             (long) usage_time_msec,
                                             (long) memory_mb,
                                             (stopped_by_user == '0' ? 'timeout' : 'user')  AS shutdown_type;

debugs = JOIN debug_started BY id LEFT, debug_finished BY id;

debug_table = FOREACH debugs GENERATE UUID(),
                                  TOTUPLE('date', ToMilliSeconds(debug_started::dt)),
                                  TOTUPLE('ws', LOWER(debug_started::ws)),
                                  TOTUPLE('user', debug_started::user),
                                  TOTUPLE('project', debug_started::project),
                                  TOTUPLE('project_type', LOWER(debug_started::project_type)),
                                  TOTUPLE('project_id', CreateProjectId(debug_started::user, debug_started::ws, debug_started::project)),
                                  TOTUPLE('id', debug_started::id),
                                  TOTUPLE('task_type', 'debugger'),
                                  TOTUPLE('memory', debug_finished::memory_mb),
                                  TOTUPLE('usage_time', debug_finished::usage_time_msec),
                                  TOTUPLE('start_time', ToMilliSeconds(debug_started::dt)),
                                  TOTUPLE('stop_time', ToMilliSeconds(debug_finished::dt)),
                                  TOTUPLE('gigabyte_ram_hours', CalculateGigabyteRamHours(debug_finished::memory_mb, debug_finished::usage_time_msec)),
                                  TOTUPLE('is_factory', (IsTemporaryWorkspaceById(debug_started::ws) ? 'yes' : 'no')),
                                  TOTUPLE('launch_type', debug_started::launch_type),
                                  TOTUPLE('shutdown_type', debug_finished::shutdown_type);


edits = getSessions(l, 'session-usage');
edits = FOREACH edits GENERATE ws,
                               user,
                               sessionID,
                               startTime,
                               usageTime,
                               endTime;

edits_table = FOREACH edits GENERATE UUID(),
                                     TOTUPLE('date', startTime),
                                     TOTUPLE('ws', ws),
                                     TOTUPLE('user', user),
                                     TOTUPLE('id', sessionID),
                                     TOTUPLE('task_type', 'editor'),
                                     TOTUPLE('memory', $default_editor_memory_mb),
                                     TOTUPLE('usage_time', usageTime),
                                     TOTUPLE('start_time', startTime),
                                     TOTUPLE('stop_time', endTime),
                                     TOTUPLE('gigabyte_ram_hours', CalculateGigabyteRamHours((long) $default_editor_memory_mb, usageTime)),
                                     TOTUPLE('is_factory', 'no');


edits_in_factory = getSessions(l, 'session-factory-usage');
edits_in_factory = FOREACH edits_in_factory GENERATE ws,
                                                     user,
                                                     sessionID,
                                                     startTime,
                                                     usageTime,
                                                     endTime;

edits_in_factory_table = FOREACH edits_in_factory GENERATE UUID(),
                                     TOTUPLE('date', startTime),
                                     TOTUPLE('ws', ws),
                                     TOTUPLE('user', user),
                                     TOTUPLE('id', sessionID),
                                     TOTUPLE('task_type', 'editor'),
                                     TOTUPLE('memory', $default_editor_memory_mb),
                                     TOTUPLE('usage_time', usageTime),
                                     TOTUPLE('start_time', startTime),
                                     TOTUPLE('stop_time', endTime),
                                     TOTUPLE('gigabyte_ram_hours', CalculateGigabyteRamHours((long) $default_editor_memory_mb, usageTime)),
                                     TOTUPLE('is_factory', 'yes');

tasks_table = UNION builds_table, runs_table, debug_table, edits_table, edits_in_factory_table;
STORE tasks_table INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
