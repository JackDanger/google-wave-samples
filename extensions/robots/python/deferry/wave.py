#!/usr/bin/python2.5
#
# Copyright 2009 Google Inc. Apache License 2.0

import logging

from waveapi import appengine_robot_runner
from waveapi import events
from waveapi import robot
from google.appengine.ext import deferred

import credentials
import actions

def OnSelfAdded(event, wavelet):
  wavelet.title = 'Deferred Stuff'
  for i in range(20):
    deferred.defer(actions.addLine, wavelet.serialize(), i, _countdown=i)

if __name__ == '__main__':
  myrobot = robot.Robot('Deferry',
                        image_url='http://code.google.com/apis/wave/images/wavelogo.png')
  myrobot.register_handler(events.WaveletSelfAdded, OnSelfAdded)
  appengine_robot_runner.run(myrobot, debug=True)
