#! /bin/bash

set -e

docker-compose -f docker-compose-${DATABASE?}-${MODE?}.yml stop proxy proxy-follower
docker-compose -f docker-compose-${DATABASE?}-${MODE?}.yml rm -f proxy proxy-follower
