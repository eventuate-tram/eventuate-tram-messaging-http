#! /bin/bash

set -e

docker-compose -f docker-compose-${DATABASE?}-${MODE?}.yml stop proxy
docker-compose -f docker-compose-${DATABASE?}-${MODE?}.yml rm -f proxy
