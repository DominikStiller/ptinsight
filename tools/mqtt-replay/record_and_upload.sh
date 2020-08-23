#!/usr/bin/env bash

root=$(realpath "$(dirname "$0")")
(
  cd $root
  pipenv run python record.py "$@"
  find recordings -type f -exec lbzip2 {} \;
  aws s3 sync recordings s3://mqtt-recordings
)
