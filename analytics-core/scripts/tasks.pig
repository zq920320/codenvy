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
                                                 (long) (memory_mb IS NOT NULL ? memory_mb : '1536'),  /* setup default BUILDER MEMORY USAGE = 1.5 GB */
                                                 (finished_normally == '0' ? 'timeout' : 'normal')  AS shutdown_type;

builds = JOIN build_started BY id LEFT, build_finished BY id;

build_table = FOREACH builds GENERATE UUID(),
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
                                      TOTUPLE('started_time', ToMilliSeconds(build_started::dt)),
                                      TOTUPLE('stopped_time', ToMilliSeconds(build_finished::dt)),
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

run_table = FOREACH runs GENERATE UUID(),
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
                                  TOTUPLE('started_time', ToMilliSeconds(run_started::dt)),
                                  TOTUPLE('stopped_time', ToMilliSeconds(run_finished::dt)),
                                  TOTUPLE('gigabyte_ram_hours', CalculateGigabyteRamHours(run_finished::memory_mb, run_finished::usage_time_msec)),
                                  TOTUPLE('is_factory', (IsTemporaryWorkspaceById(run_started::ws) ? 'yes' : 'no')),
                                  TOTUPLE('launch_type', run_started::launch_type),
                                  TOTUPLE('shutdown_type', run_finished::shutdown_type);

task_table = UNION build_table, run_table;
STORE task_table INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
