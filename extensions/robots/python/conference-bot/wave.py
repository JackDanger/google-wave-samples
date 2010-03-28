#!/usr/bin/python2.5
#
# Copyright 2009 Google Inc. Apache License 2.0

import cgi
import logging

from waveapi import appengine_robot_runner
from waveapi import element
from waveapi import events
from waveapi import ops
from waveapi import robot
from django.utils import simplejson
from google.appengine.ext import deferred

import credentials
import util
import text
import model
import importer_html
import wavemaker

# the robot
myrobot = None
domain = 'wavesandbox.com'

SESSION_WAVES = 'conference-bot/main-wave/sessions'
EXTRA_WAVES = 'conference-bot/main-wave/extras'
SESSION_ID = 'conference-bot/session-wave/id'

def MakeAdminWave(event, wavelet):
  wavelet.title = 'Admin Wave'
  gadget = element.Gadget(url=util.GetGadgetUrl())
  wavelet.root_blip.append(gadget)
  collection = model.ConferenceCollection()
  collection.owner = wavelet.creator 
  collection.admin_wave = wavelet.wave_id
  collection.put()


def OnSelfAdded(event, wavelet):
  domain = wavelet.domain
  robot_address = wavelet.robot_address
  if IsAdminWave(wavelet):
    MakeAdminWave(event, wavelet)
    return
  proxy_for = GetWaveType(wavelet)
  id = proxy_for.split('-')[0]
  logging.info('id %s' % id)
  collection = model.ConferenceCollection.get_by_id(int(id))
  if collection.tags:
    wavelet.tags.append(collection.tags[0])
  if collection.groups:
    wavelet.participants.add(collection.groups[0])
  if collection.make_public:
    wavelet.participants.add('public@a.gwave.com')
  if IsBlankWave(wavelet):
    wavelet.title = collection.name + ' Wave: Topic'
    event.blip.append_markup(text.session_html) 
  if IsEventWave(wavelet):
    wavelet.title = collection.name + ' Event Wave: EventName'
    event.blip.append('\nWhen is it?\n\n')
    event.blip.append('Who\'s coming?\n')
    event.blip.append(element.Gadget(url='http://wave-api.appspot.com/public/gadgets/areyouin/gadget.xml'))
    event.blip.append('\n\n Where is it?')
    event.blip.append(element.Gadget(url='http://google-wave-resources.googlecode.com/svn/trunk/samples/extensions/gadgets/mappy/mappy.xml'))

def OnGadgetChanged(event, wavelet):
  domain = wavelet.domain
  if IsAdminWave(wavelet):
    gadget = event.blip.first(element.Gadget, url=util.GetGadgetUrl())
    if gadget is None:
      logging.info('Error: No gadget found in Admin Wave.')
      return
    try:
      collection = GetCollectionForWave(wavelet)
    except:
      logging.info('Error: Couldnt retrieve collection.')
      return
    if collection is None:
      logging.info('Error: No collection found for Admin Wave.')
      return
    StoreGadgetChanges(wavelet, gadget, collection)
    create_main = gadget.get('createmain')
    if create_main == 'clicked' and collection.toc_wave is None:
      logging.info('toc %s' % str(collection.toc_wave))
      logging.info('Making new wave')
      MakeMainWave(collection)
    create_sessions = gadget.get('createsessions')
    if create_sessions == 'clicked':
      MakeSessionWaves(myrobot, collection)

def StoreGadgetChanges(wavelet, gadget, collection):
  collection.name = gadget.get('name')
  make_public = gadget.get('public')
  if make_public and make_public == 'on':
    collection.make_public = True
  #todo split CSV, make into proper list property
  groups = gadget.get('groups')
  if groups:
    collection.groups = [groups]
  tags = gadget.get('tags')
  if tags:
    collection.tags = [tags]
  collection.template = gadget.get('template')
  collection.datasource_type = gadget.get('datasource_type')
  collection.datasource_url = gadget.get('datasource_url')
  collection.put()

def MakeMainWave(collection):
  new_wave = myrobot.new_wave(domain=domain, submit=True,
                              participants=[collection.owner])
  new_wave.title = collection.name + ' Main Wave'
  blip = new_wave.root_blip
  blip.append(text.main_html)
  blip.range(len(blip.text)-2, len(blip.text)-1).annotate(SESSION_WAVES, '')
  blip.append(text.main_html_2)
  installer = element.Installer(manifest=util.GetInstallerUrl(collection.key().id()))
  blip.append(installer)
  myrobot.submit(new_wave)

  collection.toc_wave = new_wave.wave_id
  collection.toc_wave_ser = simplejson.dumps(new_wave.serialize())
  collection.put()

def GetCollectionForWave(wavelet):
  query = model.ConferenceCollection.all()
  query.filter('admin_wave =', wavelet.wave_id)
  return query.get()

def MakeSessionWaves(myrobot, collection):
  logging.info(collection.datasource_type)
  if collection.datasource_type == 'Spreadsheet':
    MakeSessionWavesFromSpreadsheet(myrobot, collection)

def MakeSessionWavesFromSpreadsheet(myrobot, collection):
  conf = util.createConferenceFromSpreadsheet(collection.datasource_url)
  if conf is None:
    logging.info('Error: Couldnt create conference object from Spreadsheet.')
    return
  # Only do 2 now, for quota reasons
  for session in conf.sessions[0:2]:
    #todo: defer this
    new_wave = wavemaker.MakeSessionWave(myrobot, session, collection)
    markup += session.name + '\n'
  blind_wave = myrobot.blind_wavelet(collection.toc_wave_ser)
  blind_wave.root_blip.appendMarkup()

def IsAdminWave(wavelet):
  return GetWaveType(wavelet).find('admin') > -1

def IsBlankWave(wavelet):
  return GetWaveType(wavelet).find('newwave-blank') > -1

def IsEventWave(wavelet):
  return GetWaveType(wavelet).find('newwave-event') > -1

def GetWaveType(wavelet):
  robot_address = wavelet.robot_address.split('@')[0]
  split_addy = robot_address.split('+')
  if len(split_addy) > 1:
    wave_type = split_addy[1]
  else:
    wave_type = ''
  logging.info('wave_type %s' % wave_type)
  return wave_type

if __name__ == '__main__':

  myrobot = robot.Robot('Conference-bot',
      image_url='http://kitchenmyrobot.appspot.com/public/avatar.png')
  myrobot.register_handler(events.WaveletSelfAdded, OnSelfAdded)
  myrobot.register_handler(events.GadgetStateChanged, OnGadgetChanged)
  myrobot.set_verification_token_info(credentials.VERIFICATION_TOKEN, credentials.ST) 
  myrobot.setup_oauth(credentials.CONSUMER_KEY, credentials.CONSUMER_SECRET,
    server_rpc_base=credentials.RPC_BASE[domain])
  appengine_robot_runner.run(myrobot, debug=True)
