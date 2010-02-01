from waveapi import events
from waveapi import model
from waveapi import robot
from waveapi import document
from google.appengine.ext import db
import logging
import os
import models
import htmler

def logString(context, string):
  logging.info(string)
  addBlip(context, string)

def addBlip(context, string):
  context.GetRootWavelet().CreateBlip().GetDocument().SetText(string)

def OnBlipSubmitted(properties, context):
  exportRootBlip(context)

def OnRobotAdded(properties, context):
  exportRootBlip(context)

def exportRootBlip(context):
  rootWavelet = context.GetRootWavelet()
  rootBlip = context.GetBlipById(rootWavelet.GetRootBlipId())
  html = htmler.convert_to_html(rootBlip)

  title = rootWavelet.GetTitle()
  id = rootWavelet.GetWaveId()
  body = rootBlip.GetDocument().GetText().split('\n', 1)[1]
  query = db.Query(models.WaveExport)
  query.filter('id =', id)
  waveExport = query.get()
  if waveExport is None:
    server = os.environ['SERVER_NAME']
    url = "http://" + server + "/export?waveId=" + id.replace("+", "%252B")
    addBlip(context, "View exported Wave: \n HTML: " + url + "\n XML: " + url + "&template=xml")
    waveExport = models.WaveExport()

  waveExport.id = id
  waveExport.title = title
  waveExport.body = body
  waveExport.html = html
  waveExport.participants = [p for p in rootWavelet.GetParticipants()]
  waveExport.put()

if __name__ == '__main__':
  myRobot = robot.Robot('Exporty',
      image_url='http://exporty-bot.appspot.com/avatar.png',
      version='2',
      profile_url='')
  myRobot.RegisterHandler(events.BLIP_SUBMITTED, OnBlipSubmitted)
  myRobot.RegisterHandler(events.WAVELET_SELF_ADDED, OnRobotAdded)
  myRobot.RegisterHandler(events.WAVELET_PARTICIPANTS_CHANGED, OnRobotAdded)
  myRobot.Run()
