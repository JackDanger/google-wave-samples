#!/usr/bin/python2.4
import random
import logging

from waveapi import robot
from waveapi import events
from waveapi import element
from waveapi import appengine_robot_runner

def OnSelfAdded(event, wavelet):
  for blip_id in wavelet.blips:
    blip = wavelet.blips[blip_id]
    if blip.text.find('TREASURE') > -1:
      blip.append('\nYARRR, TIS MINE, SCALLYWAGS!')
      blip.append(element.Image('http://www.iconeasy.com/icon/thumbnails/Movie%20&%20TV/Jolly%20Roger/Pirate%20Treasure%20Icon.jpg'))

if __name__ == '__main__':
  robotty = robot.Robot('Treasure Finder',
                        image_url='http://www.sixthman.net/blog/wp-content/uploads/2009/09/cartoon-pirate.png',
                        profile_url='')
  robotty.register_handler(events.WaveletSelfAdded, OnSelfAdded, context = 'ALL')
  appengine_robot_runner.run(robotty, debug=True)
