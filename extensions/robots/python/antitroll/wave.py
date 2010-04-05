#!/usr/bin/python2.4
import re
import logging
import random

from waveapi import events
from waveapi import robot
from waveapi import element
from waveapi import ops
from waveapi import element
from google.appengine.ext import webapp
from waveapi import appengine_robot_runner
from django.utils import simplejson
from google.appengine.ext import db

import credentials
import models
import content

domain = 'googlewave.com'

def OnParticipantsChanged(event, wavelet):
  logging.info(event.participants_added)
  if 'public@a.gwave.com' in event.participants_added  or wavelet.robot_address in event.participants_added or 'pamela.fox@googlewave.com' in event.participants_added:
    return
  if wavelet.creator in event.participants_added:
    return
  participants = ['pamela.fox@googlewave.com']
  wave = wavey.fetch_wavelet('googlewave.com!w+t5UPEcAFE',
                             'googlewave.com!conv+root', proxy_for_id='notouch')
  url = 'https://wave.google.com/wave/#restored:wave:%s' % wavelet.wave_id.replace('+', '%252B')
  title = wavelet.title[0:20] + '...'
  markup = '<p><a href="%s">%s</a>: %s</p><p></p>' % (url, title, event.participants_added[0])
  wave.root_blip.append_markup(markup)
  wavey.submit(wave)

def OnSelfAdded(event, wavelet):
  if wavelet.robot_address.find('notouch') > -1:
    logging.info(wavelet.serialize())
    return

  wavelet.participants.add('public@a.gwave.com')
  wave = models.SearchWave()
  wave.wave_json = simplejson.dumps(wavelet.serialize())
  wave.creator = wavelet.creator
  wave.put()
  ProcessWavelet(wavelet)

def ProcessWavelet(wavelet):
  num = random.randint(0, len(content.messages)-1) 
  wavelet.root_blip.all().delete()
  wavelet.title = content.messages[num]['title']
  wavelet.root_blip.append(content.messages[num]['body'])


class CronHandler(webapp.RequestHandler):
  robot  = None

  # override the constructor
  def __init__(self, robot):
    self.robot = robot
    webapp.RequestHandler.__init__(self)

  def get(self):
    query = db.Query(models.SearchWave)
    waves = query.fetch(limit=40)
    self.PickOneWave(waves)
    self.PickOneWave(waves)
    self.PickOneWave(waves)

  def PickOneWave(self, waves):
    num = random.randint(0, len(waves)-1)
    wave = waves[num]
    blind_wave = self.robot.blind_wavelet(wave.wave_json)
    ProcessWavelet(blind_wave)
    self.robot.submit(blind_wave)

if __name__ == '__main__':
  wavey = robot.Robot('April ONeal',
                      image_url='http://wave.google.com/wave/static/images/unknown.jpg')
  wavey.set_verification_token_info(credentials.VERIFICATION_TOKEN, credentials.ST) 
  wavey.setup_oauth(credentials.CONSUMER_KEY, credentials.CONSUMER_SECRET,
    server_rpc_base=credentials.RPC_BASE[domain])
  wavey.register_handler(events.WaveletSelfAdded, OnSelfAdded)
  wavey.register_handler(events.WaveletParticipantsChanged,
                           OnParticipantsChanged)
  appengine_robot_runner.run(wavey, debug=True, extra_handlers=[('/web/cron',
                                                                   lambda:
                                                                   CronHandler(wavey))])
