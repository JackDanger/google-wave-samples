#!/usr/bin/python2.4
#
# Copyright 2010 Google Inc. All Rights Reserved.
import logging
import os

from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app

from django.utils import simplejson

import util
import model

collection_type = 'Conference'

class AdminHandler(webapp.RequestHandler):
  def get(self):
    is_gadget = self.request.get('gadget')
    if len(is_gadget) > 0:
      filename = 'admin.xml'
      content_type = 'text/xml'
    else:
      filename = 'admin.html'
      content_type = 'text/html'
    template_values = {'server': util.GetServer(), 'type': collection_type}
    path = os.path.join(os.path.dirname(__file__), 'templates/' + filename)
    self.response.headers['Content-Type'] = content_type
    self.response.out.write(template.render(path, template_values))

class InstallerHandler(webapp.RequestHandler):
  def get(self):
    id = self.request.get('id')
    collection = model.ConferenceCollection.get_by_id(int(id))
    template_values = {'name': collection.name, 'id': collection.key().id()}
    path = os.path.join(os.path.dirname(__file__), 'templates/installer.xml')
    self.response.headers['Content-Type'] = 'text/xml' 
    self.response.out.write(template.render(path, template_values))


application = webapp.WSGIApplication(
                                     [
                                     ('/web/admin', AdminHandler),
                                     ('/web/installer', InstallerHandler),
                                     ],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()
