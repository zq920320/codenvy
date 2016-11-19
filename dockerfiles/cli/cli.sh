#!/bin/bash
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

cli_init() {

  # Constants
  CODENVY_MANIFEST_DIR="/version"
  CODENVY_CONTAINER_OFFLINE_FOLDER="/${CHE_MINI_PRODUCT_NAME}/backup"
  CODENVY_VERSION_FILE="${CHE_MINI_PRODUCT_NAME}.ver"
  CODENVY_ENVIRONMENT_FILE="${CHE_MINI_PRODUCT_NAME}.env"
  CODENVY_COMPOSE_FILE="docker-compose-container.yml"
  CODENVY_SERVER_CONTAINER_NAME="${CHE_MINI_PRODUCT_NAME}_${CHE_MINI_PRODUCT_NAME}_1"
  CODENVY_CONFIG_BACKUP_FILE_NAME="${CHE_MINI_PRODUCT_NAME}_config_backup.tar"
  CODENVY_INSTANCE_BACKUP_FILE_NAME="${CHE_MINI_PRODUCT_NAME}_instance_backup.tar"
  DOCKER_CONTAINER_NAME_PREFIX="${CHE_MINI_PRODUCT_NAME}_"

  grab_offline_images "$@"
  grab_initial_images

  DEFAULT_CODENVY_CLI_ACTION="help"
  CODENVY_CLI_ACTION=${CODENVY_CLI_ACTION:-${DEFAULT_CODENVY_CLI_ACTION}}
  
  CODENVY_LICENSE=true

  init_host_ip
  DEFAULT_CODENVY_HOST=$GLOBAL_HOST_IP
  CODENVY_HOST=${CODENVY_HOST:-${DEFAULT_CODENVY_HOST}}

  if [[ "${CODENVY_HOST}" = "" ]]; then
    info "Welcome to Codenvy!"
    info ""
    info "We did not auto-detect a valid HOST or IP address."
    info "Pass CODENVY_HOST with your hostname or IP address."
    info ""
    info "Rerun the CLI:"
    info "  docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock"
    info "                      -v <local-path>:/codenvy"
    info "                      -e CODENVY_HOST=<your-ip-or-host>"
    info "                         codenvy/cli:${CODENVY_VERSION} $@"
    return 2;
  fi

  REFERENCE_HOST_ENVIRONMENT_FILE="${CODENVY_HOST_CONFIG}/${CODENVY_ENVIRONMENT_FILE}"
  REFERENCE_HOST_COMPOSE_FILE="${CODENVY_HOST_INSTANCE}/${CODENVY_COMPOSE_FILE}"
  REFERENCE_CONTAINER_ENVIRONMENT_FILE="${CODENVY_CONTAINER_CONFIG}/${CODENVY_ENVIRONMENT_FILE}"
  REFERENCE_CONTAINER_COMPOSE_FILE="${CODENVY_CONTAINER_INSTANCE}/${CODENVY_COMPOSE_FILE}"

  CODENVY_HOST_CONFIG_MANIFESTS_FOLDER="$CODENVY_HOST_CONFIG/manifests"
  CODENVY_CONTAINER_CONFIG_MANIFESTS_FOLDER="$CODENVY_CONTAINER_CONFIG/manifests"

  CODENVY_HOST_CONFIG_MODULES_FOLDER="$CODENVY_HOST_CONFIG/modules"
  CODENVY_CONTAINER_CONFIG_MODULES_FOLDER="$CODENVY_CONTAINER_CONFIG/modules"

  # TODO: Change this to use the current folder or perhaps ~?
  if is_boot2docker && has_docker_for_windows_client; then
    if [[ "${CODENVY_HOST_INSTANCE,,}" != *"${USERPROFILE,,}"* ]]; then
      CODENVY_HOST_INSTANCE=$(get_mount_path "${USERPROFILE}/.${CHE_MINI_PRODUCT_NAME}/")
      warning "Boot2docker for Windows - CODENVY_INSTANCE set to $CODENVY_HOST_INSTANCE"
    fi
    if [[ "${CODENVY_HOST_CONFIG,,}" != *"${USERPROFILE,,}"* ]]; then
      CODENVY_HOST_CONFIG=$(get_mount_path "${USERPROFILE}/.${CHE_MINI_PRODUCT_NAME}/")
      warning "Boot2docker for Windows - CODENVY_CONFIG set to $CODENVY_HOST_CONFIG"
    fi
  fi
}

init_host_ip() {
  GLOBAL_HOST_IP=${GLOBAL_HOST_IP:=$(docker_run --net host eclipse/che-ip:nightly)}
}

grab_offline_images(){
  # If you are using codenvy in offline mode, images must be loaded here
  # This is the point where we know that docker is working, but before we run any utilities
  # that require docker.
  if [[ "$@" == *"--offline"* ]]; then
    info "init" "Importing ${CHE_MINI_PRODUCT_NAME} Docker images from tars..."

    if [ ! -d ${CODENVY_CONTAINER_OFFLINE_FOLDER} ]; then
      info "init" "You requested offline image loading, but '${CODENVY_CONTAINER_OFFLINE_FOLDER}' folder not found"
      return 2;
    fi

    IFS=$'\n'
    for file in "${CODENVY_CONTAINER_OFFLINE_FOLDER}"/*.tar 
    do
      if ! $(docker load < "${CODENVY_CONTAINER_OFFLINE_FOLDER}"/"${file##*/}" > /dev/null); then
        error "Failed to restore ${CHE_MINI_PRODUCT_NAME} Docker images"
        return 2;
      fi
      info "init" "Loading ${file##*/}..."
    done
  fi
}

grab_initial_images() {
  # Prep script by getting default image
  if [ "$(docker images -q alpine:3.4 2> /dev/null)" = "" ]; then
    info "cli" "Pulling image alpine:3.4"
    log "docker pull alpine:3.4 >> \"${LOGS}\" 2>&1"
    TEST=""
    docker pull alpine:3.4 >> "${LOGS}" 2>&1 || TEST=$?
    if [ "$TEST" = "1" ]; then
      error "Image alpine:3.4 unavailable. Not on dockerhub or built locally."
      return 2;
    fi
  fi

  if [ "$(docker images -q appropriate/curl 2> /dev/null)" = "" ]; then
    info "cli" "Pulling image appropriate/curl:latest"
    log "docker pull appropriate/curl:latest >> \"${LOGS}\" 2>&1"
    TEST=""
    docker pull appropriate/curl >> "${LOGS}" 2>&1 || TEST=$?
    if [ "$TEST" = "1" ]; then
      error "Image appropriate/curl:latest unavailable. Not on dockerhub or built locally."
      return 2;
    fi
  fi

  if [ "$(docker images -q eclipse/che-ip:nightly 2> /dev/null)" = "" ]; then
    info "cli" "Pulling image eclipse/che-ip:nightly"
    log "docker pull eclipse/che-ip:nightly >> \"${LOGS}\" 2>&1"
    TEST=""
    docker pull eclipse/che-ip:nightly >> "${LOGS}" 2>&1 || TEST=$?
    if [ "$TEST" = "1" ]; then
      error "Image eclipse/che-ip:nightly unavailable. Not on dockerhub or built locally."
      return 2;
    fi
  fi
}

cli_parse () {
  debug $FUNCNAME
  COMMAND="cmd_$1"
  COMMAND_CONTAINER_FILE="${SCRIPTS_CONTAINER_SOURCE_DIR}"/$COMMAND.sh

  if [ ! -f "${COMMAND_CONTAINER_FILE}" ]; then
    error "You passed an unknown command line option."
    return 2;
  fi

  # Need to load all files in advance so commands can invoke other commands.
  for COMMAND_FILE in "${SCRIPTS_CONTAINER_SOURCE_DIR}"/cmd_*.sh
  do
    source "${COMMAND_FILE}"
  done

  shift
  eval $COMMAND "$@"
}

get_mount_path() {
  debug $FUNCNAME
  FULL_PATH=$(get_full_path "${1}")
  POSIX_PATH=$(convert_windows_to_posix "${FULL_PATH}")
  CLEAN_PATH=$(get_clean_path "${POSIX_PATH}")
  echo $CLEAN_PATH
}

get_full_path() {
  debug $FUNCNAME
  # create full directory path
  echo "$(cd "$(dirname "${1}")"; pwd)/$(basename "$1")"
}

convert_windows_to_posix() {
  debug $FUNCNAME
  echo "/"$(echo "$1" | sed 's/\\/\//g' | sed 's/://')
}

convert_posix_to_windows() {
  debug $FUNCNAME
  # Remove leading slash
  VALUE="${1:1}"

  # Get first character (drive letter)
  VALUE2="${VALUE:0:1}"

  # Replace / with \
  VALUE3=$(echo ${VALUE} | tr '/' '\\' | sed 's/\\/\\\\/g')

  # Replace c\ with c:\ for drive letter
  echo "$VALUE3" | sed "s/./$VALUE2:/1"
}

get_clean_path() {
  debug $FUNCNAME
  INPUT_PATH=$1
  # \some\path => /some/path
  OUTPUT_PATH=$(echo ${INPUT_PATH} | tr '\\' '/')
  # /somepath/ => /somepath
  OUTPUT_PATH=${OUTPUT_PATH%/}
  # /some//path => /some/path
  OUTPUT_PATH=$(echo ${OUTPUT_PATH} | tr -s '/')
  # "/some/path" => /some/path
  OUTPUT_PATH=${OUTPUT_PATH//\"}
  echo ${OUTPUT_PATH}
}

get_docker_host_ip() {
  debug $FUNCNAME
#  case $(get_docker_install_type) in
#   boot2docker)
#     NETWORK_IF="eth1"
#   ;;
#   native)
#     NETWORK_IF="docker0"
#   ;;
#   *)
#     NETWORK_IF="eth0"
#   ;;
#  esac

  echo $GLOBAL_HOST_IP
}

get_docker_install_type() {
  debug $FUNCNAME
  if is_boot2docker; then
    echo "boot2docker"
  elif is_docker_for_windows; then
    echo "docker4windows"
  elif is_docker_for_mac; then
    echo "docker4mac"
  else
    echo "native"
  fi
}


has_docker_for_windows_client(){
  debug $FUNCNAME
  if [[ $(get_docker_host_ip) = "10.0.75.2" ]]; then
    return 0
  else
    return 1
  fi
}

is_boot2docker() {
  debug $FUNCNAME
  if uname -r | grep -q 'boot2docker'; then
    return 0
  else
    return 1
  fi
}

is_docker_for_windows() {
  debug $FUNCNAME
  if uname -r | grep -q 'moby' && has_docker_for_windows_client; then
    return 0
  else
    return 1
  fi
}

is_docker_for_mac() {
  debug $FUNCNAME
  if uname -r | grep -q 'moby' && ! has_docker_for_windows_client; then
    return 0
  else
    return 1
  fi
}

is_native() {
  debug $FUNCNAME
  if [ $(get_docker_install_type) = "native" ]; then
    return 0
  else
    return 1
  fi
}


has_env_variables() {
  debug $FUNCNAME
  PROPERTIES=$(env | grep CODENVY_)

  if [ "$PROPERTIES" = "" ]; then
    return 1
  else
    return 0
  fi
}

update_image_if_not_found() {
  debug $FUNCNAME

  text "${GREEN}INFO:${NC} (${CHE_MINI_PRODUCT_NAME} download): Checking for image '$1'..."
  CURRENT_IMAGE=$(docker images -q "$1")
  if [ "${CURRENT_IMAGE}" == "" ]; then
    text "not found\n"
    update_image $1
  else
    text "found\n"
  fi
}

update_image() {
  debug $FUNCNAME

  if [ "${1}" == "--force" ]; then
    shift
    info "download" "Removing image $1"
    log "docker rmi -f $1 >> \"${LOGS}\""
    docker rmi -f $1 >> "${LOGS}" 2>&1 || true
  fi

  if [ "${1}" == "--pull" ]; then
    shift
  fi

  info "download" "Pulling image $1"
  text "\n"
  log "docker pull $1 >> \"${LOGS}\" 2>&1"
  TEST=""
  docker pull $1 || TEST=$?
  if [ "$TEST" = "1" ]; then
    error "Image $1 unavailable. Not on dockerhub or built locally."
    return 2;
  fi
  text "\n"
}

port_open(){

#  log "netstat -an | grep 0.0.0.0:$1 >> \"${LOGS}\" 2>&1"
#  netstat -an | grep 0.0.0.0:$1 >> "${LOGS}" 2>&1
#  docker run --rm --net host alpine netstat -an | grep ${CODENVY_HOST}:$1 >> "${LOGS}" 2>&1

  docker run -d -p $1:$1 --name fake alpine:3.4 httpd -f -p $1 -h /etc/ > /dev/null 2>&1
  NETSTAT_EXIT=$?
  docker rm -f fake > /dev/null 2>&1

  if [ $NETSTAT_EXIT = 125 ]; then
    return 1
  else
    return 0
  fi
}

container_exist_by_name(){
  docker inspect ${1} > /dev/null 2>&1
  if [ "$?" == "0" ]; then
    return 0
  else
    return 1
  fi
}

get_server_container_id() {
  log "docker inspect -f '{{.Id}}' ${1}"
  docker inspect -f '{{.Id}}' ${1}
}

wait_until_container_is_running() {
  CONTAINER_START_TIMEOUT=${1}

  ELAPSED=0
  until container_is_running ${2} || [ ${ELAPSED} -eq "${CONTAINER_START_TIMEOUT}" ]; do
    log "sleep 1"
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}

container_is_running() {
  if [ "$(docker ps -qa -f "status=running" -f "id=${1}" | wc -l)" -eq 0 ]; then
    return 1
  else
    return 0
  fi
}

wait_until_server_is_booted () {
  SERVER_BOOT_TIMEOUT=${1}

  ELAPSED=0
  until server_is_booted ${2} || [ ${ELAPSED} -eq "${SERVER_BOOT_TIMEOUT}" ]; do
    log "sleep 2"
    sleep 2
    # Total hack - having to restart haproxy for some reason on windows
    if is_docker_for_windows || is_docker_for_mac; then
      log "docker restart codenvy_haproxy_1 >> \"${LOGS}\" 2>&1"
      docker restart codenvy_haproxy_1 >> "${LOGS}" 2>&1
    fi
    ELAPSED=$((ELAPSED+1))
  done
}

server_is_booted() {
  HTTP_STATUS_CODE=$(curl -I -k $CODENVY_HOST/api/ \
                     -s -o "${LOGS}" --write-out "%{http_code}")
  if [[ "${HTTP_STATUS_CODE}" = "200" ]] || [[ "${HTTP_STATUS_CODE}" = "302" ]]; then
    return 0
  else
    return 1
  fi
}

check_if_booted() {
  CURRENT_CODENVY_SERVER_CONTAINER_ID=$(get_server_container_id $CODENVY_SERVER_CONTAINER_NAME)
  wait_until_container_is_running 20 ${CURRENT_CODENVY_SERVER_CONTAINER_ID}
  if ! container_is_running ${CURRENT_CODENVY_SERVER_CONTAINER_ID}; then
    error "(${CHE_MINI_PRODUCT_NAME} start): Timeout waiting for ${CHE_MINI_PRODUCT_NAME} container to start."
    return 2
  fi

  info "start" "Server logs at \"docker logs -f ${CODENVY_SERVER_CONTAINER_NAME}\""
  info "start" "Server booting..."
  wait_until_server_is_booted 60 ${CURRENT_CODENVY_SERVER_CONTAINER_ID}

  if server_is_booted ${CURRENT_CODENVY_SERVER_CONTAINER_ID}; then
    info "start" "Booted and reachable"
    info "start" "Ver: $(get_installed_version)"
    if ! is_docker_for_mac; then
      info "start" "Use: http://${CODENVY_HOST}"
      info "start" "API: http://${CODENVY_HOST}/swagger"
    else
      info "start" "Use: http://localhost"
      info "start" "API: http://localhost/swagger"
    fi
  else
    error "(${CHE_MINI_PRODUCT_NAME} start): Timeout waiting for server. Run \"docker logs ${CODENVY_SERVER_CONTAINER_NAME}\" to inspect the issue."
    return 2
  fi
}

#TODO - is_initialized will return as initialized with empty directories
is_initialized() {
  debug $FUNCNAME
  if [[ -d "${CODENVY_CONTAINER_CONFIG_MANIFESTS_FOLDER}" ]] && \
     [[ -d "${CODENVY_CONTAINER_CONFIG_MODULES_FOLDER}" ]] && \
     [[ -f "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}" ]] && \
     [[ -f "${CODENVY_CONTAINER_CONFIG}/${CODENVY_VERSION_FILE}" ]]; then
    return 0
  else
    return 1
  fi
}

has_version_registry() {
  if [ -d /version/$1 ]; then
    return 0;
  else
    return 1;
  fi
}

list_versions(){
  # List all subdirectories and then print only the file name
  for version in /version/* ; do
    text " ${version##*/}\n"
  done
}

version_error(){
  text "\nWe could not find version '$1'. Available versions:\n"
  list_versions
  text "\nSet CODENVY_VERSION=<version> and rerun.\n\n"
}

### Returns the list of Codenvy images for a particular version of Codenvy
### Sets the images as environment variables after loading from file
get_image_manifest() {
  info "cli" "Checking registry for version '$1' images"
  if ! has_version_registry $1; then
    version_error $1
    return 1;
  fi

  IMAGE_LIST=$(cat /version/$1/images)
  IFS=$'\n'
  for SINGLE_IMAGE in $IMAGE_LIST; do
    log "eval $SINGLE_IMAGE"
    eval $SINGLE_IMAGE
  done
}

can_upgrade() {
  #  4.7.2 -> 5.0.0-M2-SNAPSHOT  <insert-syntax>
  #  4.7.2 -> 4.7.3              <insert-syntax>
  while IFS='' read -r line || [[ -n "$line" ]]; do
    VER=$(echo $line | cut -d ' ' -f1)
    UPG=$(echo $line | cut -d ' ' -f2)

    # Loop through and find all matching versions
    if [[ "${VER}" == "${1}" ]]; then
      if [[ "${UPG}" == "${2}" ]]; then
        return 0
      fi
    fi
  done < "$CODENVY_MANIFEST_DIR"/upgrades

  return 1
}

print_upgrade_manifest() {
  #  4.7.2 -> 5.0.0-M2-SNAPSHOT  <insert-syntax>
  #  4.7.2 -> 4.7.3              <insert-syntax>
  while IFS='' read -r line || [[ -n "$line" ]]; do
    VER=$(echo $line | cut -d ' ' -f1)
    UPG=$(echo $line | cut -d ' ' -f2)
    text "  "
    text "%s" $VER
    for i in `seq 1 $((25-${#VER}))`; do text " "; done
    text "%s" $UPG
    text "\n"
  done < "$CODENVY_MANIFEST_DIR"/upgrades
}

print_version_manifest() {
  while IFS='' read -r line || [[ -n "$line" ]]; do
    VER=$(echo $line | cut -d ' ' -f1)
    CHA=$(echo $line | cut -d ' ' -f2)
    UPG=$(echo $line | cut -d ' ' -f3)
    text "  "
    text "%s" $VER
    for i in `seq 1 $((25-${#VER}))`; do text " "; done
    text "%s" $CHA
    for i in `seq 1 $((18-${#CHA}))`; do text " "; done
    text "%s" $UPG
    text "\n"
  done < "$CODENVY_MANIFEST_DIR"/versions
}

get_installed_version() {
  if ! is_initialized; then
    echo "<not-installed>"
  else
    cat "${CODENVY_CONTAINER_CONFIG}"/$CODENVY_VERSION_FILE
  fi
}

get_installed_installdate() {
  if ! is_initialized; then
    echo "<not-installed>"
  else
    cat "${CODENVY_CONFIG}"/$CODENVY_VERSION_FILE
  fi
}

# Usage:
#   confirm_operation <Warning message> [--force|--no-force]
confirm_operation() {
  debug $FUNCNAME

  FORCE_OPERATION=${2:-"--no-force"}

  if [ ! "${FORCE_OPERATION}" == "--quiet" ]; then
    # Warn user with passed message
    info "${1}"
    text "\n"
    read -p "      Are you sure? [N/y] " -n 1 -r
    text "\n\n"
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
      return 1;
    else
      return 0;
    fi
  fi
}

# Runs puppet image to generate codenvy configuration
generate_configuration_with_puppet() {
  debug $FUNCNAME

  if is_docker_for_windows; then
    REGISTRY_ENV_FILE=$(convert_posix_to_windows "${CODENVY_HOST_INSTANCE}/config/registry/registry.env")
    POSTGRES_ENV_FILE=$(convert_posix_to_windows "${CODENVY_HOST_INSTANCE}/config/postgres/postgres.env")
    CODENVY_ENV_FILE=$(convert_posix_to_windows "${CODENVY_HOST_INSTANCE}/config/codenvy/$CHE_MINI_PRODUCT_NAME.env")
  else
    REGISTRY_ENV_FILE="${CODENVY_HOST_INSTANCE}/config/registry/registry.env"
    POSTGRES_ENV_FILE="${CODENVY_HOST_INSTANCE}/config/postgres/postgres.env"
    CODENVY_ENV_FILE="${CODENVY_HOST_INSTANCE}/config/codenvy/$CHE_MINI_PRODUCT_NAME.env"
  fi
  # Note - bug in docker requires relative path for env, not absolute
  log "docker_run -it --env-file=\"${REFERENCE_CONTAINER_ENVIRONMENT_FILE}\" \
                  --env-file=/version/$CODENVY_VERSION/images \
                  -v \"${CODENVY_HOST_INSTANCE}\":/opt/codenvy:rw \
                  -v \"${CODENVY_HOST_CONFIG_MANIFESTS_FOLDER}\":/etc/puppet/manifests:ro \
                  -v \"${CODENVY_HOST_CONFIG_MODULES_FOLDER}\":/etc/puppet/modules:ro \
                  -e "REGISTRY_ENV_FILE=${REGISTRY_ENV_FILE}" \
                  -e "POSTGRES_ENV_FILE=${POSTGRES_ENV_FILE}" \
                  -e "CODENVY_ENV_FILE=${CODENVY_ENV_FILE}" \
                      $IMAGE_PUPPET \
                          apply --modulepath \
                                /etc/puppet/modules/ \
                                /etc/puppet/manifests/codenvy.pp --show_diff \"$@\""
  docker_run -it  --env-file="${REFERENCE_CONTAINER_ENVIRONMENT_FILE}" \
                  --env-file=/version/$CODENVY_VERSION/images \
                  -v "${CODENVY_HOST_INSTANCE}":/opt/codenvy:rw \
                  -v "${CODENVY_HOST_CONFIG_MANIFESTS_FOLDER}":/etc/puppet/manifests:ro \
                  -v "${CODENVY_HOST_CONFIG_MODULES_FOLDER}":/etc/puppet/modules:ro \
                  -e "REGISTRY_ENV_FILE=${REGISTRY_ENV_FILE}" \
                  -e "POSTGRES_ENV_FILE=${POSTGRES_ENV_FILE}" \
                  -e "CODENVY_ENV_FILE=${CODENVY_ENV_FILE}" \
                      $IMAGE_PUPPET \
                          apply --modulepath \
                                /etc/puppet/modules/ \
                                /etc/puppet/manifests/codenvy.pp --show_diff "$@"
}

# return date in format which can be used as a unique file or dir name
# example 2016-10-31-1477931458
get_current_date() {
    date +'%Y-%m-%d-%s'
}

require_license() {
  if [[ "${CODENVY_LICENSE}" = "true" ]]; then
    return 0
  else
    return 1
  fi
}
