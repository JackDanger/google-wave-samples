#!/usr/bin/python2.4
#

import logging
import urllib
import re

from waveapi import events
from waveapi import robot
from waveapi import document
from waveapi import robot_abstract
from waveapi import ops

from django.utils import simplejson
from google.appengine.api import urlfetch
from google.appengine.api import mail

import message
import installerchecker

SUBMITTY_KEY = 'submitty/installer'

def lookForInstaller(blip):
  if blip.GetDocument().HasAnnotation(SUBMITTY_KEY):
    return
  annotations = blip.GetAnnotations()
  for annotation in annotations:
    if annotation.name.find('link/') > -1:
      if annotation.value.find('.xml') > -1:
        errors = installerchecker.check(annotation.value)
        if errors:
          blip.GetDocument().SetAnnotation(annotation.range, SUBMITTY_KEY, annotation.value)
          errorsBlip = blip.GetDocument().InsertInlineBlip(annotation.range.end)
          errorsBlip.GetDocument().SetText('\n'.join(errors))

def lookForChecks(blip):
  elements = blip.GetElements()
  for el in elements.values():
    logging.info('name: ' + getattr(el, 'name', ''))
    if (el.type == document.ELEMENT_TYPE.CHECK
      and getattr(el, 'name', None) == 'includeRobot'):
        logging.info('Got robots')
  for element in elements:
    logging.info("found element: " + str(element))

def OnBlipSubmitted(properties, context):
  #wavelet = context.GetRootWavelet()
  #wavelet.CreateBlip().GetDocument().SetText("Working")
  blip = context.GetBlipById(properties['blipId'])
  lookForInstaller(blip)

def OnSelfAdded(properties, context):
  wavelet = context.GetRootWavelet()
  blipId = properties['blipId']
  blip = context.GetBlipById(blipId)
  blipDoc = blip.GetDocument()
  text = blipDoc.GetText()
  if len(text) > 2:
    logging.info('Creating new wave')
    wavelet = robot_abstract.NewWave(context, context.GetRootWavelet().GetParticipants())
    blipId = wavelet.GetRootBlipId()
    blip  = context.GetBlipById(blipId)
    blipDoc = blip.GetDocument()
    return
  else:
    i = 2
    #sendMail(wavelet.GetWaveId())

  reviewers = ['pamela.fox']
  for reviewer in reviewers:
    wavelet.AddParticipant(reviewer + '@wavesandbox.com')

  wavelet.SetTitle('Extension Submission')
  #context.builder.DocumentAppendMarkup(wavelet.GetWaveId(), wavelet.GetId(), blipId, message.questions)
  AddQuestion(blipDoc, 'Does your extension include a robot?', 'includeRobot')
  AddQuestion(blipDoc, 'Does your extension include a gadget?', 'includeGadget')

def AddQuestion(blipDoc, question, name):
  blipDoc.AppendText(question)
  checkbox = document.FormElement(document.ELEMENT_TYPE.CHECK, name, label=question)
  blipDoc.AppendElement(checkbox)
  blipDoc.AppendText('\n')

def OnDocumentChanged(properties, context):
  wavelet = context.GetRootWavelet()
  blipId = properties['blipId']
  blip = context.GetBlipById(blipId)
  lookForInstaller(blip)
  lookForChecks(blip)
  wavelet = context.GetRootWavelet()
  dataDoc = wavelet.GetDataDocument('_new_ids_')
  if dataDoc and len(dataDoc) > 1:
    logging.info('Found data doc: ' + dataDoc)
    waveId = dataDoc.split(' ')[0][1:]
    if waveId != wavelet.GetWaveId():
      newDoc = wavelet.CreateBlip().GetDocument()
      message = 'This wave had content, so I\'ve created a new wave:'
      newDoc.SetText(message)
      newDoc.AppendText('Go')
      newDoc.SetAnnotation(document.Range(len(message), len(message) + 2), 'link/wave', waveId)
      wavelet.SetDataDocument('_new_ids_', '')
      #sendMail(waveId)
  
def sendMail(waveId):
  mail.send_mail('pamela.fox@gmail.com',
                 'wave-extensions-gallery-moderators@google.com',
                 'Extension Submission', 
                 'There is a new extension to review here: https://wave.google.com/a/wavesandbox.com/#restored:wave:' + waveId.replace('+', '%252B'))

if __name__ == '__main__':
  submitty = robot.Robot('Submitty',
                         image_url='http://complete-wave.appspot.com/inc/submitty.png',
                         profile_url='http://www.google.com',
                         version='23')

  submitty.RegisterHandler(events.WAVELET_SELF_ADDED, OnSelfAdded)
  submitty.RegisterHandler(events.DOCUMENT_CHANGED, OnDocumentChanged)
  submitty.RegisterHandler(events.BLIP_SUBMITTED, OnBlipSubmitted)
  submitty.Run(debug=True)
