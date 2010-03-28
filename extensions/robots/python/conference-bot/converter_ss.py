import data
import os
import logging

from django.utils import simplejson
from google.appengine.api import urlfetch

import converter

class SpreadsheetConverter(converter.SourceConverter):
  def __init__(self, url):
    self._url = url
    self._json = None
    converter.SourceConverter.__init__(self)

  def getJSON(self):
    json = converter.fetchJSON(self._url)
    if 'feed' not in json:
      logging.info('JSON not well formed %s' % simplejson.dumps(json))
      return False
    self._json = json
    return True

  def createConference(self):
    self.getJSON()
    if not self._json:
      logging.info('Cant create')
      return
    self._conference  = data.Conference()
    talks = self._json['feed']['entry']
    for talk in talks:
      name = getSpreadsheetFieldValue(talk, 'talkname')
      talk_link = getSpreadsheetFieldValue(talk, 'talklink')
      speaker_name = getSpreadsheetFieldValue(talk, 'speakername')
      speaker_link = getSpreadsheetFieldValue(talk, 'speakerlink')
      speaker = data.Speaker(speaker_name, link=speaker_link)
      session = data.Session(name, link=talk_link, speakers=[speaker])
      self._conference.sessions.append(session)

def getSpreadsheetFieldValue(entry, field):
  field = 'gsx$' + field
  if field in entry:
    return entry[field]['$t']
  else:
    logging.info('field %s not found' % field)
    return ''
