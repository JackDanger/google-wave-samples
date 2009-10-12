from waveapi import events
from waveapi import model
from waveapi import robot
from waveapi import document
import logging

def logString(context, string):
  logging.info(string)
  addBlip(context, string)

def addBlip(context, string):
  context.GetRootWavelet().CreateBlip().GetDocument().SetText(string)

def OnRobotAdded(properties, context):
  addBlip(context, 'Pamela is currently experiencing a massive influx of waves across multiple servers, and is not able to respond to all of them in a timely manner. So, she has written an auto-reply bot that she hopes will respond adequately on her behalf. \n\n If you are waving about an API question, please post in the API forum: http://groups.google.com/group/google-wave-api \n\n If you are waving about a Client question, please post in the users help forum: http://www.google.com/support/forum/p/Wave?hl=en&utm_source=HC&utm_medium=leftnav&utm_campaign=wave \n\n If you are lonley and looking for people to wave with, try some of the public waves (search for "with:public"). \n\n If you are wondering how to install extensions, search for "Extensions Gallery" and use the installers on that wave. \n\n If you are waving for a personal reason, well, I\'ll do my best to respond. \n\n Generally, if you need a guaranteed response from me in a timely manner, please email me (or email me a link to a Wave). \n\n Thanks for understanding, and happy wave-ing!')

if __name__ == '__main__':
  myRobot = robot.Robot('FoxBot',
      version='3',
      image_url='http://pamela.fox.googlepages.com/foxbot.JPG',
      profile_url='')
  myRobot.RegisterHandler(events.WAVELET_SELF_ADDED, OnRobotAdded)
  myRobot.Run()
