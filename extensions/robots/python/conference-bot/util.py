from django.utils import simplejson
from google.appengine.api import urlfetch

import data
import os
import logging

def GetGadgetUrl():
  return '%s/web/admin?gadget=true&nocache=true' % GetServer()

def GetInstallerUrl(id):
  return '%s/web/installer?id=%s' % (GetServer(), id)

def GetServer():
  server = os.environ['SERVER_NAME']
  port = os.environ['SERVER_PORT']

  if port and port != '80':
    return 'http://%s:%s' % (server, port)
  else:
    return 'http://%s' % (server)
