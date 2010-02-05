from django.utils import simplejson
from google.appengine.api import urlfetch

import data
import os
import logging

def GetGadgetUrl():
  return GetServer() + '/web/start/gadget?nocache=true'

def GetServer():
  server = os.environ['SERVER_NAME']
  port = os.environ['SERVER_PORT']

  if port and port != '80':
    return 'http://%s:%s' % (server, port)
  else:
    return 'http://%s' % (server)

def fetchJSON(url):
  result = urlfetch.fetch(url)
  if result.status_code == 200:
    result_obj = simplejson.loads(result.content)
    return result_obj
  return None

def fetchDapperJSON(url):
  logging.info('Fetching JSON for %s' % url)
  json = fetchJSON(url)
  if json is None:
    # log error
    logging.info('Error retrieving JSON %s' % simplejson.dumps(json))
    return None
  if 'dapper' not in json:
    logging.info('JSON not well formed %s' % simplejson.dumps(json))
    return None
  if json['dapper']['status'] != 'OK':
    # log error
    logging.info('Dapper error %s' % simplejson.dumps(json))
    return None
  logging.info('Got JSON %s' % simplejson.dumps(json))
  return json


def getDapperFieldValue(fields, field):
  if field in fields:
    return fields[field][0]['value']
  else:
    logging.info('field %s not found' % field)
    return ''

def createSessionFromDapper(url):
  logging.info('making session')
  json = fetchDapperJSON(url)
  if json is None:
    logging.info('problem')
    return None
  fields = json['fields']
  title = getDapperFieldValue(fields, 'title')
  time = getDapperFieldValue(fields, 'time')
  day = getDapperFieldValue(fields, 'day')
  location = getDapperFieldValue(fields, 'location')
  description = getDapperFieldValue(fields, 'description')
  author = getDapperFieldValue(fields, 'author')
  author_description = getDapperFieldValue(fields, 'author_description')
  author_image = getDapperFieldValue(fields, 'author_image')

  session = data.Session(title, speakers=author, speakers_description = author_description, speakers_image = author_image, description=description,
                         location=location, day=day, time=time)
  return session


def createConferenceFromDapper(url):
  json = fetchDapperJSON(url)
  if json is None:
    return None
  conf  = data.Conference()
  talks = json['groups']['talk']
  for talk in talks:
    title = getDapperFieldValue(talk, 'title')
    link = talk['title'][0]['href']
    speakers = getDapperFieldValue(talk, 'author')
    session = data.Session(title, link=link, speakers=speakers)
    conf.sessions.append(session)
  return conf
