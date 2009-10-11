#!/usr/bin/python2.4
#
# Copyright 2009 Google Inc. All Rights Reserved.

"""One-line documentation for models module.

A detailed description of models.
"""

__author__ = 'pamelafox@google.com (Pamela Fox)'

from google.appengine.ext import db

class WaveExport(db.Model):
  title = db.StringProperty()
  body = db.TextProperty()
  id = db.StringProperty()
  participants = db.StringListProperty()
  created = db.DateTimeProperty(auto_now_add=True)
