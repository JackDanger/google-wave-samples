#!/usr/bin/python2.4
#

import logging
import urllib
import re

from waveapi import events
from waveapi import robot
from waveapi import document
from waveapi import robot_abstract

from django.utils import simplejson
from google.appengine.api import urlfetch

import message

def OnBlipSubmitted(properties, context):
  blip = context.GetBlipById(properties['blipId'])
  blip.GetDocument().SetText(res[0][0])

def OnSelfAdded(properties, context):
  blip = context.GetBlipById(properties['blipId'])
  blipDoc = blip.GetDocument()
  text = blipDoc.GetText()
  if len(text) > 2: 
    newWave = robot_abstract.NewWave(context, context.GetRootWavelet().GetParticipants())
    newWave.SetTitle("Extension Submission")
    newRootBlipId = newWave.GetRootBlipId()
    newRootBlip  = context.GetBlipById(_newRootBlipId)
    blipDoc = newRootBlip.GetDocument()
  #installerInput = document.FormElement(ELEMENT_TYPE.INPUT, 'input', label='Installer XML')
  blipDoc.SetText('Welcome to submitty bot')

def OnDocumentChanged(properties, context):
  wavelet = context.GetRootWavelet()
  dataDoc = wavelet.GetDataDocument('_new_ids_')
  waveId = dataDoc.split(' ')[0][1:]
  newDoc = wavelet.CreateBlip().GetDocument()
  message = 'This wave had content, so I\'ve created a new wave:'
  newDoc.SetText(message)
  newDoc.AppendText('Go')
  newDoc.SetAnnotation(document.Range(len(message), len(message) + 2), 'link/wave', waveId)

if __name__ == '__main__':
  submitty = robot.Robot('Submitty',
                         image_url='http://complete-wave.appspot.com/inc/submitty.png',
                         profile_url='http://www.google.com',
                         version='3')

  #submitty.RegisterHandler(events.BLIP_SUBMITTED, OnBlipSubmitted)
  submitty.RegisterHandler(events.WAVELET_SELF_ADDED, OnSelfAdded)
  submitty.RegisterHandler(events.DOCUMENT_CHANGED, OnDocumentChanged)
  submitty.Run(debug=True)
