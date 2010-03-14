#!/usr/bin/python2.4
#  kjkjhjhjhhjj
# Copyright 2009 Google Inc. All Rights Reserved.
"""Implementation of the Everything bot

Yet another smiley robot
"""

__author__ = 'douwe@google.com (Douwe Osinga)'

import logging

from api import events
from api import robot


def OnBlipSubmitted(properties, context):
  blip = context.GetBlipById(properties['blipId'])
  contents = blip.GetDocument().GetText()
  contents = contents.replace(':-(', unichr(0x2639)).replace(':-)', unichr(0x263A))
  blip.GetDocument().SetText(contents)

if __name__ == '__main__':
  yasr = robot.Robot('Yasr',
                     image_url='http://wave-api-dmo.appspot.com/public/smiley.png',
                     profile_url='http://code.google.com')

  yasr.RegisterHandler(events.BLIP_SUBMITTED, OnBlipSubmitted)
  yasr.Run(debug=True)

