#!/usr/bin/python2.5
#
# Copyright 2009 Google Inc. Apache License 2.0

import logging

from waveapi import robot

import model

domain = 'wavesandbox.com'
SESSION_ID = 'confrenzy/session/id'

def MakeSessionWave(myrobot, session, collection):
  try:
    new_wave = myrobot.new_wave(domain, submit=True,
                                participants=[collection.owner])
  except Exception, e:
    logging.info('Error creating new wave: %s' % str(e))
    return None
  new_wave.title = session.name
  if session.id:
    new_wave.data_documents[SESSION_ID] = session.id
  collection.session_waves.append(new_wave.wave_id)
  collection.put()
  myrobot.submit(new_wave)
  return new_wave.wave_id
