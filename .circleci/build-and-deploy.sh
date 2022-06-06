#! /bin/bash -e

export DOCKER_HOST_IP=$(hostname -I | sed -e 's/ .*//g')

./build-and-test-all-mysql-binlog.sh

./deploy-artifacts.sh
