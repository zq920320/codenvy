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

cmd_version() {
  debug $FUNCNAME

  error "!!! this information is experimental - upgrade not yet available !!!"
  echo ""
  text "$CHE_PRODUCT_NAME:\n"
  text "  Version:      %s\n" $(get_installed_version)
  text "  Installed:    %s\n" $(get_installed_installdate)

  if is_initialized; then
    text "\n"
    text "Upgrade Options:\n"
    text "  INSTALLED VERSION        UPRADEABLE TO\n"
    print_upgrade_manifest $(get_installed_version)
  fi

  text "\n"
  text "Available:\n"
  text "  VERSION                  CHANNEL           UPGRADEABLE FROM\n"
  if is_initialized; then
    print_version_manifest $(get_installed_version)
  else
    print_version_manifest $CODENVY_VERSION
  fi
}
