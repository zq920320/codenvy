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

cmd_upgrade() {
  debug $FUNCNAME

  if [ $# -eq 0 ]; then
    info "upgrade" "Missing version to upgrade to - rerun with 'upgrade <version>'."
    return 2;
  fi

  if ! can_upgrade $(get_installed_version) ${1}; then
    info "upgrade" "Your current version $(get_installed_version) is not upgradeable to $1."
    info "upgrade" "Run '${CHE_MINI_PRODUCT_NAME} version' to see your upgrade options."
    return 2;
  fi

  # If here, this version is validly upgradeable.  You can upgrade from
  # $(get_installed_version) to $1
  ## Download version images
  info "upgrade" "Downloading $1 images..."
  get_image_manifest ${1}
  SAVEIFS=$IFS
  IFS=$'\n'
  for SINGLE_IMAGE in ${IMAGE_LIST}; do
    VALUE_IMAGE=$(echo ${SINGLE_IMAGE} | cut -d'=' -f2)
    update_image_if_not_found ${VALUE_IMAGE}
  done
  IFS=$SAVEIFS
  info "upgrade" "Downloading done."

  if get_server_container_id "${CODENVY_SERVER_CONTAINER_NAME}" >> "${LOGS}" 2>&1; then
    info "upgrade" "Stopping currently running instance..."
    CURRENT_CODENVY_SERVER_CONTAINER_ID=$(get_server_container_id ${CODENVY_SERVER_CONTAINER_NAME})
    if server_is_booted ${CURRENT_CODENVY_SERVER_CONTAINER_ID}; then
      cmd_stop
    fi
  fi
  info "upgrade" "Preparing backup..."
  cmd_backup
  ## Try start
  if ! cmd_start; then
    info "upgrade" "Startup failed, restoring latest backup..."
    cmd_restore;
  fi

}
