#!/usr/bin/python2.5
#
# Copyright 2009 Google Inc. All Rights Reserved.

"""Tests for util."""

import unittest

from google.appengine.api import urlfetch
from google.appengine.api import apiproxy_stub_map
from google.appengine.api import urlfetch_stub

import importer_html

class HTMLImporterTest(unittest.TestCase):

  def setUp(self):
    apiproxy_stub_map.apiproxy = apiproxy_stub_map.APIProxyStubMap()
    apiproxy_stub_map.apiproxy.RegisterStub('urlfetch', urlfetch_stub.URLFetchServiceStub())

  def testCreateConferenceFromHTML(self):
    url = 'http://my.sxsw.com/events?event[track]=All%20Events&event[category]=&event[sub_category]=&event[conference_day]=2010-03-13&logged_in=false'
    importer_html.createConferenceFromHTML(url)

if __name__ == '__main__':
  unittest.main()
