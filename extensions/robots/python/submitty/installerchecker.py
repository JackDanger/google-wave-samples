#!/usr/bin/python2.4
#
# Copyright 2009 Google Inc. All Rights Reserved.
from google.appengine.api import urlfetch
from xml.dom import minidom

def check(installer_url):
  errors = []
  response = urlfetch.fetch(installer_url)
  if response.status_code == 200:
    dom = minidom.parseString(response.content)
    extension_tag = dom.getElementsByTagName('extension')
    if extension_tag:
      required_tagnames = ["name", "description", "thumbnailUrl", "author"]
      required_tagerrors = []
      for required_tagname in required_tagnames:
        required_tag = extension_tag[0].getAttribute(required_tagname)
        if not required_tag:
          required_tag = dom.getElementsByTagName(required_tagname)
        if not required_tag:
          required_tagerrors.append(required_tagname)
        if len(required_tagerrors) > 0:
          errors.append('Installer XML is missing required tag(s): ' + ', '.join(required_tagerrors)) 
    if len(errors) == 0:
      errors.append('No errors found. Installer looks good.')
  return errors
