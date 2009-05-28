import os
import logging
import wsgiref.handlers

from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app


class IndexHandler(webapp.RequestHandler):
  pass


def main():
  application = webapp.WSGIApplication([
    ('/', IndexHandler),
  ], debug=True)
  wsgiref.handlers.CGIHandler().run(application)

if __name__ == '__main__':
  main()
