# -*- coding: utf-8 -*-
import re
import htmlentitydefs

from waveapi import events
from waveapi import model
from waveapi import robot
from waveapi import document

import logging

def OnRobotAdded(properties, context):
  """Invoked when the robot has been added."""
  root_wavelet = context.GetRootWavelet()
 
#def OnDocumentChanged(properties, context):
def OnBlipSubmitted(properties, context):
  """Scan the wave to look for any special characters we should convert."""
  blip = context.GetBlipById(properties['blipId']) 
  blipDoc = blip.GetDocument()
  text = blipDoc.GetText()

  match = text.find('<3')
  if match > -1:
    blipDoc.DeleteRange(document.Range(match, match+2))
    blipDoc.InsertElement(match, document.Gadget('http://supasmiley-bot.appspot.com/gadget.xml'))

if __name__ == '__main__':
	myRobot = robot.Robot('Supa Smiley', 
	  image_url='',
	  version='1')  
	myRobot.RegisterHandler(events.WAVELET_SELF_ADDED, OnRobotAdded)
	myRobot.RegisterHandler(events.BLIP_SUBMITTED, OnBlipSubmitted)
	myRobot.RegisterHandler(events.DOCUMENT_CHANGED, OnBlipSubmitted)
	myRobot.Run()
