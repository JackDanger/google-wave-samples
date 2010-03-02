#!/usr/bin/python2.4
#
# Copyright 2010 Google Inc. All Rights Reserved.

"""One-line documentation for models module.

A detailed description of models.
"""

__author__ = 'pamelafox@google.com (Pamela Fox)'

from google.appengine.ext import db
from django.utils import simplejson

class TriagePreset(db.Model):
  name = db.StringProperty()
  # each source should be a serialized list of dicts
  sources = db.StringProperty(multiline=True)

  def GetSourcesList(self):
    return simplejson.loads(self.sources)

  def SetSourcesFromList(self, list):
    self.sources = simplejson.dumps(list)

  def GetDict(self): 
    return {'name': self.name, 'sources': self.GetSourcesList(), 'key': str(self.key())}
