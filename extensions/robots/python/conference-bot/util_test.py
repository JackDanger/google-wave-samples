#!/usr/bin/python2.5
#
# Copyright 2009 Google Inc. All Rights Reserved.

"""Tests for util."""

import unittest

from google.appengine.api import urlfetch
from google.appengine.api import apiproxy_stub_map
from google.appengine.api import urlfetch_stub

import util


class UtilTest(unittest.TestCase):

  def setUp(self):
    apiproxy_stub_map.apiproxy = apiproxy_stub_map.APIProxyStubMap()
    apiproxy_stub_map.apiproxy.RegisterStub('urlfetch', urlfetch_stub.URLFetchServiceStub())

  def testFetchGoodJSON(self):
    json_obj = util.fetchJSON('http://www.xul.fr/ajax-json-menu.json')
    self.assertEqual(json_obj['menu'], 'File')

  def testFetchBadJSON(self):
    json_obj = util.fetchJSON('http://www.xul.fr/badfile.xml')
    self.assertEqual(json_obj, None)
  
  def testCreateConferenceFromDapper(self):
    conf = util.createConferenceFromDapper('http://www.dapper.net/transform.php?dappName=LCA2010Talks&transformer=JSON&v_day=thursday')
    self.assertEqual(len(conf.sessions), 27)

if __name__ == '__main__':
  unittest.main()
