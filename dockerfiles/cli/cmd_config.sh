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

cmd_config() {

  # If the system is not initialized, initalize it.
  # If the system is already initialized, but a user wants to update images, then re-download.
  FORCE_UPDATE=${1:-"--no-force"}
  if ! is_initialized; then
    cmd_init $FORCE_UPDATE
  elif [[ "${FORCE_UPDATE}" == "--pull" ]] || \
       [[ "${FORCE_UPDATE}" == "--force" ]]; then
    cmd_download $FORCE_UPDATE
  fi

  # If the CODENVY_VERSION set by an environment variable does not match the value of
  # the codenvy.ver file of the installed instance, then do not proceed as there is a
  # confusion between what the user has set and what the instance expects.
  INSTALLED_VERSION=$(get_installed_version)
  if [[ $CODENVY_VERSION != $INSTALLED_VERSION ]]; then
    info "config" "CODENVY_VERSION=$CODENVY_VERSION does not match ${CODENVY_ENVIRONMENT_FILE}=$INSTALLED_VERSION. Aborting."
    info "config" "This happens if the <version> of your Docker image is different from ${CODENVY_HOST_CONFIG}/${CODENVY_ENVIRONMENT_FILE}"
    return 1
  fi

  if [ -z ${IMAGE_PUPPET+x} ]; then
    get_image_manifest $CODENVY_VERSION
  fi

  # Development mode
  if [ "${CODENVY_DEVELOPMENT_MODE}" = "on" ]; then
    # if dev mode is on, pick configuration sources from repo.
    # please note that in production mode update of configuration sources must be only on update.
    docker_run -v "${CODENVY_HOST_CONFIG}":/copy \
               -v "${CODENVY_HOST_DEVELOPMENT_REPO}":/files \
                  $IMAGE_INIT

    # in development mode to avoid permissions issues we copy tomcat assembly to ${CODENVY_INSTANCE}
    # if codenvy development tomcat exist we remove it
    if [[ -d "${CODENVY_CONTAINER_INSTANCE}/dev" ]]; then
        log "docker_run -v \"${CODENVY_HOST_INSTANCE}/dev\":/root/dev alpine:3.4 sh -c \"rm -rf /root/dev/*\""
        docker_run -v "${CODENVY_HOST_INSTANCE}/dev":/root/dev alpine:3.4 sh -c "rm -rf /root/dev/*"
        log "rm -rf \"${CODENVY_HOST_INSTANCE}/dev\" >> \"${LOGS}\""
        rm -rf "${CODENVY_CONTAINER_INSTANCE}/dev"
    fi
    # copy codenvy development tomcat to ${CODENVY_INSTANCE} folder
    cp -r "$(get_mount_path $(echo $CODENVY_CONTAINER_DEVELOPMENT_REPO/$DEFAULT_CODENVY_DEVELOPMENT_TOMCAT-*/))" \
        "${CODENVY_CONTAINER_INSTANCE}/dev"
  fi

  info "config" "Generating $CHE_MINI_PRODUCT_NAME configuration..."
  # Run the docker configurator
  if [ "${CODENVY_DEVELOPMENT_MODE}" = "on" ]; then
    # generate configs and print puppet output logs to console if dev mode is on
    generate_configuration_with_puppet
  else
    generate_configuration_with_puppet >> "${LOGS}"
  fi

  # Replace certain environment file lines with their container counterparts
  info "config" "Customizing docker-compose for running in a container"
  # If this is windows, we need to add a special volume for postgres
  if has_docker_for_windows_client; then
    sed "s|^.*postgresql\/data.*$|\ \ \ \ \ \ \-\ \'codenvy-postgresql-volume\:\/var\/lib\/postgresql\/data\:Z\'|" -i "${REFERENCE_CONTAINER_COMPOSE_FILE}"

    echo "" >> "${REFERENCE_CONTAINER_COMPOSE_FILE}"
    echo "volumes:" >> "${REFERENCE_CONTAINER_COMPOSE_FILE}"
    echo "  codenvy-postgresql-volume:" >> "${REFERENCE_CONTAINER_COMPOSE_FILE}"
    echo "     external: true" >> "${REFERENCE_CONTAINER_COMPOSE_FILE}"

    # On Windows, it is not possible to volume mount postgres data folder directly
    # This creates a named volume which will store postgres data in docker for win VM
    # TODO - in future, we can write synchronizer utility to copy data from win VM to host
    log "docker volume create --name=codenvy-postgresql-volume >> \"${LOGS}\""
    docker volume create --name=codenvy-postgresql-volume >> "${LOGS}"
  fi
}
