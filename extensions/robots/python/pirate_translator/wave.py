#!/usr/bin/python2.4

from waveapi import robot
from waveapi import events
from waveapi import appengine_robot_runner

def OnSelfAdded(event, wavelet):
  wavelet.reply('Ready to replace, matey!')

def OnBlipSubmitted(event, wavelet):
  translations = {'yes': 'Yarr!', 'lol': 'Yo-ho-ho!'}
  for english, piratish in translations.items():
    event.blip.all(english).replace(piratish)

if __name__ == '__main__':
  robotty = robot.Robot('Pirate Speaky',
      image_url='',
      profile_url='')
  robotty.register_handler(events.WaveletSelfAdded, OnSelfAdded)
  robotty.register_handler(events.BlipSubmitted, OnBlipSubmitted)
  appengine_robot_runner.run(robotty, debug=True)
