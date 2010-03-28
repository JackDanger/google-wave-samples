import data
import os
import logging

from django.utils import simplejson
from google.appengine.api import urlfetch

class SourceConverter():

  def __init__(self):
    self._conference = None
    pass

  def createConference(self):
    pass

def fetchJSON(url):
  logging.info('Fetching JSON for %s' % url)
  result = urlfetch.fetch(url)
  if result.status_code == 200:
    result_obj = simplejson.loads(result.content)
    return result_obj
  else:
    logging.info('Error retrieving JSON %s')
    return None

def fetchHTML(url):
  logging.info('Fetching HTML for %s' % url)
  result = urlfetch.fetch(url)
  if result.status_code == 200:
    return result.content
  else:
    logging.info('Error retrieving HTML %s')
    return None
