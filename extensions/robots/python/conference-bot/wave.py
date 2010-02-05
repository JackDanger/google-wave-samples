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

import credentials
import util

# the robot
myrobot = None
domain = 'googlewave.com'
DATADOC_PROCESSED = 'conference-bot/session/processed'
DATADOC_CONF_PROCESSED = 'conference-bot/conference/processed'
DATADOC_URL = 'conference-bot/session/url'

def OnBlipSubmitted(event, wavelet):
  """Invoked when any blip we are interested in is submitted."""
  if wavelet.title.find('admin wave') > -1:
    day = wavelet.title.split(':')[1]
    wavelet.tags.append('lca2010')
    #wavelet.participants.add('public@a.gwave.com')
    wavelet.data_documents[DATADOC_CONF_PROCESSED] = 'yes'
    doStuff(event.blip, event.modified_by, day)
    return
  if DATADOC_URL in wavelet.data_documents and DATADOC_PROCESSED not in wavelet.data_documents:
    url = wavelet.data_documents[DATADOC_URL]
    session_id = url.rsplit('/', 1)[1]
    session = util.createSessionFromDapper('http://www.dapper.net/transform.php?dappName=LCAConferenceSession'
                                 + '&transformer=JSON&v_sessionid='
                                 + session_id)
    if session is None:
      return None
    else:
      html = """

<b>Time:</b> %s
<b>Day:</b> %s
<b>Location:</b> %s

<b>Description:</b>
%s

<b>Speaker(s):</b> %s
%s


<b>Session Materials:</b>
<i>Link to any slides, URLs, etc.</i>


<b>Session Notes:</b>
<i>It usually works best if a few people self-elect themselves as note-takers, and edit this bit with running notes.</i>


<b>Wave Tips:</b>
- To reply inline to a particular part of a blip, double click + select 'Reply' in the doodad that appears.
- To have a general discussion, reply below this root blip.
"""

      event.blip.append_markup(html % (session.time, session.day,
                                       session.location,
                                       cgi.escape(session.description),
                                       session.speakers,
                                       cgi.escape(session.speakers_description)))
      wavelet.data_documents[DATADOC_PROCESSED] = 'yes'
      wavelet.participants.add('public@a.gwave.com')
      wavelet.tags.append('lca2010')


def OnSelfAdded(event, wavelet):
  robot_address = wavelet.robot_address
  if robot_address.find('newwave') > -1:
    wavelet.tags.append('lca2010')
    wavelet.participants.add('public@a.gwave.com')
  if robot_address.find('newwave-event') > -1:
    wavelet.title = 'LCA Planning Wave: EventName'
    event.blip.append('\nWhen is it?\n\n')
    event.blip.append('Who\'s coming?\n')
    event.blip.append(element.Gadget(url='http://wave-api.appspot.com/public/gadgets/areyouin/gadget.xml'))
    event.blip.append('\n\n Where is it?')
    event.blip.append(element.Gadget(url='http://google-wave-resources.googlecode.com/svn/trunk/samples/extensions/gadgets/mappy/mappy.xml'))


def doStuff(blip, modifier, day):
  # for a given conference object,
  # create a TOC wave
  # create each session wave
  # add all session waves to TOC wave
  # whenever a new wave is created with this robot (onselfadded),
  # robot should check if it's already got the wave in the TOC,
  # else add it
  #conf = util.createConferenceFromDapper('http://www.dapper.net/transform.php?dappName=LCA2010Talks&transformer=JSON&v_day=' + day)
  conf = util.createConferenceFromDapper('http://imagine-it.org/google/wave/dapper.json')
  if conf is None:
    return
  end = len(blip.text)
  #sessions = conf.sessions
  sessions = conf.sessions[0:10]
  for session in sessions:
    wave_id = createNewWave(session.title, modifier, url=session.link)
    if wave_id:
      blip.append(session.title + "\n")
      blip.range(end, end+len(session.title)+1).annotate('link/wave', wave_id)
      end += len(session.title)+1
  pass

def createNewWave(title, participant, url=None):
  try:
    new_wave = myrobot.new_wave(domain=domain, submit=True, participants=[participant]) 
  except Exception, e:
    return None
  new_wave.title = title
  if url:
    new_wave.data_documents[DATADOC_URL] = url

  # set data doc
  new_wave.root_blip.append('A new day and a new wave')
  #new_wave.root_blip.append_markup(
  #    '<p>Some stuff!</p><p>Not the <b>beautiful</b></p>')
  # if session has link, try to fill in content from the link
  myrobot.submit(new_wave)
  return new_wave.wave_id

if __name__ == '__main__':

  myrobot = robot.Robot('Conference-bot',
      image_url='http://kitchenmyrobot.appspot.com/public/avatar.png')
  myrobot.register_handler(events.WaveletSelfAdded, OnSelfAdded)
  myrobot.register_handler(events.BlipSubmitted, OnBlipSubmitted)
  myrobot.set_verification_token_info(credentials.VERIFICATION_TOKEN[domain], credentials.ST[domain]) 
  myrobot.setup_oauth(credentials.CONSUMER_KEY[domain], credentials.CONSUMER_SECRET[domain],
    server_rpc_base=credentials.RPC_BASE[domain])
  appengine_robot_runner.run(myrobot, debug=True)
