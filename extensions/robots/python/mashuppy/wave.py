from waveapi import events
from waveapi import model
from waveapi import robot
from waveapi import document
import logging
from google.appengine.api import urlfetch
from xml.dom import minidom

def logString(context, string):
  logging.info(string)
  addBlip(context, string)

def addBlip(context, string):
  context.GetRootWavelet().CreateBlip().GetDocument().SetText(string)

def OnRobotAdded(properties, context):
  rootWavelet = context.GetRootWavelet()
  waveTitle = rootWavelet.GetTitle()
  rootBlip = context.GetBlipById(rootWavelet.GetRootBlipId())
  bodyWords = rootBlip.GetDocument().GetText().split(' ')
  response = urlfetch.fetch('http://www.abc.net.au/ra/rss/asiapacific.rss')
  if response.status_code == 200:
    dom = minidom.parseString(response.content)
    for node in dom.getElementsByTagName('title'):
      title = node.firstChild.data
      for word in bodyWords:
        logging.info("word: " + word + "|word")
        if title.find(word) > -1:
          addBlip(context, title)

if __name__ == '__main__':
  myRobot = robot.Robot('FoxBot',
      version='3',
      image_url='http://pamela.fox.googlepages.com/foxbot.JPG',
      profile_url='')
  myRobot.RegisterHandler(events.WAVELET_SELF_ADDED, OnRobotAdded)
  myRobot.Run()
