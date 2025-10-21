#!/usr/bin/env bash

###
# Script generates seed file for CircleCI caching.
#
# Example:
#
#   PARAM_FIND_ARGS='. -name ".tool-versions" -o -name ".plugin-versions"' \
#     PARAM_OUTPUT=/tmp/asdf_cache_seed \
#     ./.circleci/scripts/cache_gen_key.bash
#
###

# Bash Strict Mode Settings
set -euo pipefail
# Path Initialization
SCRIPT_PATH_1="${BASH_SOURCE[0]:-$0}"
SCRIPT_PATH="$([[ ! "${SCRIPT_PATH_1}" =~ /bash$ ]] && readlink -f "${SCRIPT_PATH_1}" || echo "")"
SCRIPT_DIR="$([ -n "${SCRIPT_PATH}" ] && (cd "$(dirname "${SCRIPT_PATH}")" && pwd -P) || echo "")"
ROOT_DIR="$([ -n "${SCRIPT_DIR}" ] && (cd "${SCRIPT_DIR}/../.." && pwd -P) || echo "/tmp")"

fail() { echo "Error: $1" >&2; exit 1; }

function checksum_file() {
  openssl md5 "${1}" | awk '{print $2}'
}

create_fresh_file() {
  local -r filepath="$1"
  if [[ -f "${filepath}" ]]; then
    rm "${filepath}"
  fi
  touch "${filepath}"
}

function main() {
  [ -z "${PARAM_FIND_ARGS:-}" ] && fail "environment variable PARAM_FIND_ARGS is not set"
  [ -z "${PARAM_OUTPUT:-}" ] && fail "environment variable PARAM_OUTPUT is not set"

  create_fresh_file "${PARAM_OUTPUT}"

  eval find "${PARAM_FIND_ARGS}"
  eval find "${PARAM_FIND_ARGS}" | sort | xargs cat | shasum | awk '{print $1}' >"${PARAM_OUTPUT}"
  echo "Seed file saved at ${PARAM_OUTPUT}."
}

# don't run main, if the script is imported using `source`
if [[ "${BASH_SOURCE[0]}" != "${0}" ]]; then
  [[ -n "${BASH_SOURCE[0]}" ]] && printf "%s\n\n" "Loaded: ${BASH_SOURCE[0]}" >&2
else
  main "$@"
fi
