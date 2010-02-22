#!/usr/bin/python2.4
import random

from waveapi import robot
from waveapi import events
from waveapi import element
from waveapi import appengine_robot_runner

def OnSelfAdded(event, wavelet):
  wavelet.title = ('Land Ho!')
  wavelet.root_blip.append('Arr, only a scurvy dog would hide his booty in the root blip.')

  teasure_blip = random.randint(1, 9)
  shark_blip = random.randint(1, 9)
  for i in range(10):
    if i == teasure_blip:
      blip = wavelet.reply('TREASURE!')
    elif i == shark_blip:
      blip = wavelet.reply('SHARK!!!')
      blip.append(element.Gadget('http://google-wave-resources.googlecode.com/svn/trunk/samples/extensions/gadgets/sharkattack/sharkattack.xml'))
    else:
      wavelet.reply('Nothing to see here, matey!')

if __name__ == '__main__':
  robotty = robot.Robot('Treasure Hider',
      image_url='',
      profile_url='')
  robotty.register_handler(events.WaveletSelfAdded, OnSelfAdded)
  appengine_robot_runner.run(robotty, debug=True)
