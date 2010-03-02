#!/usr/bin/python2.4
#
# Copyright 2010 Google Inc. All Rights Reserved.

"""Tests for models."""

__author__ = 'pamelafox@google.com (Pamela Fox)'

import unittest
import pickle
import os

from django.utils import simplejson
from google.appengine.api import datastore_file_stub
from google.appengine.api import apiproxy_stub_map
import models

APP_ID = 'triagey'
os.environ['APPLICATION_ID'] = APP_ID
apiproxy_stub_map.apiproxy.RegisterStub('datastore_v3', datastore_file_stub.DatastoreFileStub(APP_ID, '/dev/null', '/dev/null'))

class ModelsTest(unittest.TestCase):

  def testTriagePresetCreate(self):
    preset = models.TriagePreset(name='wavedev',
                                 creator='pamelafox@google.com',
                                 share_mode='world')
    sources = [{'type': 'code',  'project': 'google-wave-resources'}]
    preset.sources = simplejson.dumps(sources)
    preset.put()
    q = models.TriagePreset.all()
    q.filter('name =', 'wavedev')
    self.assert_(q.count() >= 1)
    pass


if __name__ == '__main__':
  unittest.main()
