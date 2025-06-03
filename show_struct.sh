#!/bin/bash

# Cherche le dossier "feedup" dans app/src
FEEDUP_PATH=$(find app/src -type d -name "feedup" | head -n 1)

if [ -z "$FEEDUP_PATH" ]; then
  echo "Le dossier 'feedup' n'a pas été trouvé dans app/src."
  exit 1
fi

echo "Arborescence du dossier : $FEEDUP_PATH"
echo "----------------------------------------"

# Affiche l'arborescence à partir de feedup uniquement (répertoires uniquement)
find "$FEEDUP_PATH" -type d | sed -e "s#$FEEDUP_PATH##" -e 's#[^/][^/]*/#  |#g' -e 's#/|#-- |#'
