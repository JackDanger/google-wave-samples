#!/usr/bin/python2.4

import logging
import urllib
import re
import os

import models
import cred

from waveapi import events
from waveapi import robot
from waveapi import ops
from waveapi import element
from waveapi import appengine_robot_runner
from google.appengine.ext import db


def OnBlipSubmitted(event, wavelet):
  blip = event.blip
  addGadget(event, wavelet)

def OnSelfAdded(event, wavelet):
  addGadget(event, wavelet)

def addGadget(event, wavelet):
  blip = wavelet.root_blip
  body = blip.text.split('\n', 2)[2]
  id = wavelet.wave_id
  query = db.Query(models.WaveExport)
  query.filter('id =', id)
  waveExport = query.get()
  server = os.environ['SERVER_NAME']
  url = "http://" + server + "/export?waveId=" + id.replace("+", "%252B") + "&ext=.xml?nocache=true"
  if waveExport is None:
    waveExport = models.WaveExport()
  else:
    if waveExport.body == body:
      #Nothing changed, do nothing
      #Blip_submitted gets called when gadget state changes as well
      return
  blip.all(element.Gadget).delete()

  gadget = element.Gadget(url)
  blip.append(gadget)

  waveExport.id = id
  waveExport.title = wavelet.title
  waveExport.body = body
  waveExport.put()


if __name__ == '__main__':
  gadgitty = robot.Robot('Gadgitty',
      image_url='http://www.seoish.com/wp-content/uploads/2009/04/wrench.png',
      profile_url='')
  #gadgitty.set_verification_token_info(cred.VERIFICATION_TOKEN, cred.ST) 
  gadgitty.setup_oauth(cred.CONSUMER_KEY, cred.CONSUMER_SECRET,
      server_rpc_base='http://sandbox.gmodules.com/api/rpc') 
  gadgitty.register_handler(events.WaveletSelfAdded, OnSelfAdded)
  gadgitty.register_handler(events.BlipSubmitted, OnBlipSubmitted)
  appengine_robot_runner.run(gadgitty, debug=True)
