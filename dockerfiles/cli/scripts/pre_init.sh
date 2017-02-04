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

pre_init() {
  ADDITIONAL_MANDATORY_PARAMETERS=""
  ADDITIONAL_OPTIONAL_DOCKER_PARAMETERS="
  -e CODENVY_HOST=<YOUR_HOST>          IP address or hostname where codenvy will serve its users"
  ADDITIONAL_OPTIONAL_DOCKER_MOUNTS=""
  ADDITIONAL_COMMANDS="
  add-node                             Adds a physical node to serve workspaces intto the ${CHE_FORMAL_PRODUCT_NAME} cluster
  list-nodes                           Lists all physical nodes that are part of the ${CHE_FORMAL_PRODUCT_NAME} cluster
  remove-node <ip>                     Removes the physical node from the ${CHE_FORMAL_PRODUCT_NAME} cluster"
  ADDITIONAL_GLOBAL_OPTIONS=""

  # This must be incremented when BASE is incremented by an API developer
  CHE_CLI_API_VERSION=1
}
