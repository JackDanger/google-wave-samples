#!/bin/bash
#
# $Id$
# Copyright 2009 Google Inc. All Rights Reserved.
# Author: davidbyttow@google.com (David Byttow)

cd `dirname $0`

if [ -f app.yaml ]; then
  rm app.yaml
fi
ln -s app-prod.yaml app.yaml

/home/build/static/projects/apphosting/devtools/appcfg.py --no_cookies \
    --server="appengine.google.com" update ./

rm app.yaml
cd -
