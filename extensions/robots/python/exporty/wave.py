from waveapi import events
from waveapi import model
from waveapi import robot
from waveapi import document
from google.appengine.ext import db
import logging
import models

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
  title = rootWavelet.GetTitle()
  id = rootWavelet.GetWaveId()
  body = rootBlip.GetDocument().GetText().split('\n', 1)[1]

  query = db.Query(models.WaveExport)
  query.filter('id =', id)
  waveExport = query.get()
  if waveExport is None:
    addBlip(context, "Exported Wave to: " + "http://spammmybot.appspot.com/export?waveId=" + id.replace("+", "%252B"))
    waveExport = models.WaveExport()

  waveExport.id = id
  waveExport.title = title
  waveExport.body = body
  waveExport.participants = [p for p in rootWavelet.GetParticipants()]
  waveExport.put()

if __name__ == '__main__':
  myRobot = robot.Robot('Exporty v3',
      image_url='http://www.wavy-robot.appspot.com/assets/icon.png',
      version='1',
      profile_url='')
  myRobot.RegisterHandler(events.BLIP_SUBMITTED, OnBlipSubmitted)
  myRobot.RegisterHandler(events.WAVELET_SELF_ADDED, OnRobotAdded)
  myRobot.RegisterHandler(events.PARTICIPANTS_ADDED, OnRobotAdded)
  myRobot.Run()
