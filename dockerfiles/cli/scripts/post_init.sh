#!/bin/bash
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

post_init() {
  GLOBAL_HOST_IP=${GLOBAL_HOST_IP:=$(docker_run --net host ${BOOTSTRAP_IMAGE_CHEIP})}
  DEFAULT_CODENVY_HOST=$GLOBAL_HOST_IP
  CODENVY_HOST=${CODENVY_HOST:-${DEFAULT_CODENVY_HOST}}
  CODENVY_PORT=80
  CHE_PORT=80
  CHE_SERVER_CONTAINER_NAME="${CHE_MINI_PRODUCT_NAME}_${CHE_MINI_PRODUCT_NAME}_1"
  CHE_MIN_RAM=1.5
  CHE_MIN_DISK=100
  CHE_COMPOSE_PROJECT_NAME=codenvy
  CHE_CONTAINER_NAME=codenvy_codenvy_1
}
