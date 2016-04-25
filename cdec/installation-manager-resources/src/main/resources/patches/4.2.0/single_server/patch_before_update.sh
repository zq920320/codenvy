#!/bin/bash

#### fix mongoDB
CURRENT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

mongo -u$mongo_admin_user_name -p$mongo_admin_pass --authenticationDatabase admin "${CURRENT_DIR}/update_mongo.js" &> update_mongo.log
