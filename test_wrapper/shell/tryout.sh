#!/bin/sh

curl -X POST -d '{"flag_kind": "boolean", "flag_key": "bool_flag", "target": {"target_identifier": "region", "target_name": "region"}}' \
 -H "content-type: application/json" http://localhost:4000/api/1.0/check_flag