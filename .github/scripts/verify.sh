#!/bin/bash

set -euo pipefail

echo "Verify script syntax..."
for file in .github/scripts/*.sh; do
  bash -n "$file"
done

if [[ $(command -v shellcheck) ]]; then
  echo "Verify script styles..."
  shellcheck .github/scripts/*.sh
fi
