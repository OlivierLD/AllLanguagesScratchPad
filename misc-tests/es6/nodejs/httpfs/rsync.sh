#!/bin/bash
# From client to server
rsync -ave ssh --include="*/" \
      --include="*.json" \
      --include="*.yaml" \
      --include="*.java" \
      --include="*.sh" \
      --include="*.template" \
      --include="*.properties" \
      --include="*.gradle" \
      --include="Makefile" \
      --include="*config" \
      --include="*.ejs" \
      --include="*.md" \
      --include="*.cfg" \
      --include="*.tpl" \
      --include="imgs" \
      --include="*.py" \
      --exclude="*" \
      --prune-empty-dirs . 100.111.136.104:/home/opc/git/httpfs
