#!/usr/bin/python2.5
#
# Copyright 2009 Google Inc. Apache License 2.0

import logging

from waveapi import robot
from waveapi import element
from waveapi import blip
import credentials

def logSomething(str):
  logging.info(str)

def addLine(wavelet_ser, num):
  # Construct robot
  myrobot = robot.Robot('Scrolly')

  # Construct wavelet
  wavelet = myrobot.blind_wavelet(wavelet_ser)

  # Setup Oauth
  myrobot.setup_oauth(credentials.CONSUMER_KEY, credentials.CONSUMER_SECRET,
    server_rpc_base=credentials.RPC_BASE[wavelet.domain])

  pattern = '>>>'
  line = ''
  for x in range(num):
    line += pattern
  line += '\n'

  wavelet.root_blip.append(line)
  myrobot.submit(wavelet)
