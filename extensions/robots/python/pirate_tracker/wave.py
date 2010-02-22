#!/usr/bin/python2.4
import re
import logging

from waveapi import events
from waveapi import robot
from waveapi import element
from waveapi import ops
from waveapi import element
from google.appengine.ext import webapp
from waveapi import appengine_robot_runner
from django.utils import simplejson
from google.appengine.api import urlfetch

import credentials
import models

domain = 'wavesandbox.com'
SEARCH_TERM = 'twitter-searchy/search_term'

def fetchJSON(url):
  logging.info('Fetching JSON for %s' % url)
  result = urlfetch.fetch(url, headers={'Referer': 'FoxBotRef', 'User-Agent':'FoxBot'})
  if result.status_code == 200:
    result_obj = simplejson.loads(result.content)
    return result_obj
  else:
    logging.info('Error retrieving JSON %s')
    return None

def OnBlipSubmitted(event, wavelet):
  pass

def OnSelfAdded(event, wavelet):
  wavelet.data_documents[SEARCH_TERM] = 'pirate'
  id = AddTweets(wavelet)
  wave = models.SearchWave()
  wave.wave_json = simplejson.dumps(wavelet.serialize())
  wave.last_id = id
  wave.put()

def AddTweets(wavelet, id=None):
  blip = wavelet.root_blip
  search_term = wavelet.data_documents[SEARCH_TERM]
  tweets = GetTweets(search_term, id)
  # for other date order
  tweets.reverse()
  text = ''
  annotations = []
  tweets_info = []
  if len(tweets) > 0:
    #text += '<p></p>'
    #text += '\n'
    pass
  insert_point = len(wavelet.title)+1
  for tweet in tweets:
    id = str(tweet['id'])
    tweet_user = tweet['from_user'].encode('utf-8')
    tweet_text = tweet['text'].encode('utf-8')
    tweet_text_all = '\n%s: %s' % (tweet_user, tweet_text)
    url = 'http://twitter.com/%s' % tweet_user
    blip.at(insert_point).insert(tweet_text_all)
    blip.range(insert_point, insert_point+len(tweet_user)+1).annotate('link/manual', url)
  return id


def GetTweets(search_term, id):
  url = 'http://imagine-it.org/google/twitterproxy.php?search_term=%s' % search_term
  if id:
    url += '&since_id=%s' % id

  json = fetchJSON(url)
  return json['results']

class CronHandler(webapp.RequestHandler):
  robot  = None

  # override the constructor
  def __init__(self, robot):
    self.robot = robot
    webapp.RequestHandler.__init__(self)

  def get(self):
    waves = models.SearchWave.all()
    for wave in waves:
      logging.info(wave.wave_json)
      blind_wave = self.robot.blind_wavelet(wave.wave_json)
      if wave.last_id:
        last_id = wave.last_id
      else:
        last_id = None
      id = AddTweets(blind_wave, last_id)
      wave.last_id = id
      wave.put()
      self.robot.submit(blind_wave)

if __name__ == '__main__':
  removey = robot.Robot('Pirate Tracker',
      image_url='http://www.seoish.com/wp-content/uploads/2009/04/wrench.png',
      profile_url='')
  removey.set_verification_token_info(credentials.VERIFICATION_TOKEN, credentials.ST) 
  removey.setup_oauth(credentials.CONSUMER_KEY, credentials.CONSUMER_SECRET,
    server_rpc_base=credentials.RPC_BASE[domain])
  removey.register_handler(events.BlipSubmitted, OnBlipSubmitted)
  removey.register_handler(events.WaveletSelfAdded, OnSelfAdded)
  appengine_robot_runner.run(removey, debug=True, extra_handlers=[('/web/cron',
                                                                   lambda:
                                                                   CronHandler(removey))])
