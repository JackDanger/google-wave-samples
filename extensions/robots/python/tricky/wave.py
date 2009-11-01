from waveapi import events
from waveapi import model
from waveapi import robot
from waveapi import document
import logging
from google.appengine.api import urlfetch
from xml.dom import minidom
from django.utils import simplejson
import urllib
import random
import thingies

def logString(context, string):
  logging.info(string)
  addBlip(context, string)

def addBlip(context, string):
  context.GetRootWavelet().CreateBlip().GetDocument().SetText(string)

def OnBlipSubmitted(properties, context):
  blipId = properties['blipId']
  blip = context.GetBlipById(blipId)
  if blip.GetDocument().GetText().lower().find('trick or treat') > -1:
    randInt = random.randint(0, 10)
    if randInt == 6:
      text = 'TRICK!'
      things = thingies.tricks
      searchHint = ''
    else:
      text = 'TREAT!'
      things = thingies.treats
      searchHint = 'candy'
    randInt = random.randint(0, (len(things)-1))
    thing = things[randInt]
    url = 'http://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=' + urllib.quote('"' + thing + '" ' + searchHint) + ''
    logging.info(url)
    js = urlfetch.fetch(url=url).content
    results = simplejson.loads(js)['responseData']['results']
    randInt = random.randint(0, (len(results)-1))
    result = results[randInt]
    image = document.Image(result['tbUrl'], int(result['tbWidth']), int(result['tbHeight']))
    childBlip = blip.CreateChild()
    childBlip.GetDocument().SetText(text + '\n' + 'Mmm... ' + thing + '\n')
    childBlip.GetDocument().AppendElement(image)

def OnRobotAdded(properties, context):
  rootWavelet = context.GetRootWavelet()
  rootWavelet.SetTitle('Welcome to the Spooky House!')
  rootBlip = context.GetBlipById(rootWavelet.GetRootBlipId())
  rootDoc = rootBlip.GetDocument()
  rootDoc.AppendElement(document.Gadget('http://tricky-bot.appspot.com/gadget.xml'))

if __name__ == '__main__':
  myRobot = robot.Robot('Tricky',
      version='2',
      image_url='http://tricky-bot.appspot.com/images/avatar.png',
      profile_url='http://tricky-bot.appspot.com/')
  myRobot.RegisterHandler(events.WAVELET_SELF_ADDED, OnRobotAdded)
  myRobot.RegisterHandler(events.BLIP_SUBMITTED, OnBlipSubmitted)
  myRobot.Run()
