#!/usr/bin/python2.4
#

import logging
import urllib
import re

from waveapi import events
from waveapi import robot
from waveapi import element
from waveapi import ops
from waveapi import element 
from waveapi import appengine_robot_runner

from django.utils import simplejson
from google.appengine.api import urlfetch
from google.appengine.api import mail

import message
import installerchecker

INSTALLER_STATUS = 'submitty/installer/status'
INSTALLER_URL = 'submitty/installer/url'
CHECKBOX_ROBOT = 'submitty/question/robot'
CHECKBOX_GADGET = 'submitty/question/gadget'
STATUS_ADDEDREVIEWERS = 'submitty/status/addedreviewers'
STATUS_APPROVED = 'submitty/status/approved'
GADGET_URL = 'http://submitty-bot.appspot.com/gadget.xml?nocache=1234567'
REVIEWERS = {'wavesandbox.com': 'extensions-review@wavesandbox.com',
               'googlewave.com': 'google-wave-extensions-review@googlegroups.com',
               'google.com': 'wave-extensions-gallery-moderators@google.com'}

def lookForInstaller(wavelet):
  blip = wavelet.root_blip
  if INSTALLER_STATUS in wavelet.data_documents:
    return
  key = 'link/auto'
  if key not in blip.annotations:
    return
  for annotation in blip.annotations[key]:
     if annotation.value.find('.xml') > -1:
       errors = installerchecker.check(annotation.value)
       response_blip = blip.insert_inline_blip(annotation.end)
       wavelet.data_documents[INSTALLER_URL] = annotation.value
       if len(errors) > 0:
         wavelet.data_documents[INSTALLER_STATUS] = 'errors'
         response_blip.append('\n'.join(errors))
       else:
         wavelet.data_documents[INSTALLER_STATUS] = 'success'
         response_blip.append('Installer looks good!')
         response_blip.append(element.Installer(annotation.value))

def lookForAnswer(wavelet, checkbox_name, message):
  if checkbox_name in wavelet.data_documents:
    return
  blip = wavelet.root_blip
  checkbox = blip.first(element.Check, name=checkbox_name)
  if checkbox and getattr(checkbox.value(), 'value') == 'true':
    wavelet.data_documents[checkbox_name] = 'Responded'
    for pos, elem in blip._elements.items():
      if elem == checkbox:
        inline_blip = blip.insert_inline_blip(pos+3)
        inline_blip.append(message)

def lookForAnswers(wavelet):
  lookForAnswer(wavelet, CHECKBOX_ROBOT, message.robot)
  lookForAnswer(wavelet, CHECKBOX_GADGET, message.gadget)

def ModifiedByApprover(modifier):
  sandbox_approvers = ['pamela.fox@wavesandbox.com']
  preview_approvers = ['pamela.fox@googlewave.com']
  dogfood_approvers = ['pamelafox@google.com']
  approvers = sandbox_approvers + preview_approvers + dogfood_approvers
  if modifier in approvers:
    return True
  else:
    logging.info('Someone else tried to approve: %s' % modifier)
    return False

def OnGadgetStateChanged(event, wavelet):
  # if new status is review, add peeps
  # if new status is approve, add to gallery
  gadget = event.blip.first(element.Gadget, url=GADGET_URL)
  if gadget.status == 'review' and STATUS_ADDEDREVIEWERS not in wavelet.data_documents:
    AddReviewers(wavelet)
    wavelet.data_documents[STATUS_ADDEDREVIEWERS] = 'yes'
  if gadget.status == 'approved' and STATUS_APPROVED not in wavelet.data_documents:
    # Check for maldoers
    if ModifiedByApprover(event.modified_by):
      # make new gallery wave
      AddToGallery(wavelet)
      wavelet.data_documents[STATUS_APPROVED] = 'yes'
      wavelet.tags.append('status-approved')

def AddToGallery(wavelet):
  galleries = {'wavesandbox.com': {
                 'group': 'google-wave-extension-gallery-all@wavesandbox.com',
                 'creator': 'pamela.fox@wavesandbox.com'
                },
               'googlewave.com': {
                   'group': 'google-wave-extension-gallery-all@googlegroups.com',
                   'creator': 'doctorwave.gallery@googlewave.com'
                },
               'google.com': {
                   'group': 'google-wave-extension-gallery-all@google.com',
                   'creator': 'pamelafox@google.com'
                }}
  #todo: add galleries
  participants = [galleries[wavelet.domain]['creator']]
  new_wavelet = submitty.new_wave(wavelet.domain, participants)
  extension_url = wavelet.data_documents[INSTALLER_URL]
  extension_name = installerchecker.get_name(extension_url)
  new_wavelet.title = extension_name
  blip = new_wavelet.root_blip
  blip.append(element.Installer(extension_url))
  new_wavelet.submit_with(wavelet)

def AddReviewers(wavelet):
  wavelet.participants.add(REVIEWERS[wavelet.domain])
  wavelet.participants.add('pamela.fox@'+ wavelet.domain)

def OnBlipSubmitted(event, wavelet):
  blip = event.blip
  lookForInstaller(wavelet)
  lookForAnswers(wavelet)

def OnSelfAdded(event, wavelet):
  logging.info('domain: %s' % wavelet.domain)
  blip = wavelet.root_blip
  old_wavelet = wavelet
  new_wavelet = None
  if len(blip.text) > 2:
    logging.info('Creating new wave')
    new_wavelet = submitty.new_wave(wavelet.domain, wavelet.participants)
    wavelet = new_wavelet
    blip = wavelet.root_blip
    # todo if new wave link to it

  #sendMail(wavelet.GetWaveId())

  wavelet.title = 'Extension Submission'
  blip.append('\n')
  gadget = element.Gadget(GADGET_URL)
  blip.append(gadget)
  blip.append('\n')
  blip.append_markup(message.prelim)
  AddQuestion(blip, message.prelim_robotq, CHECKBOX_ROBOT)
  AddQuestion(blip, message.prelim_gadgetq, CHECKBOX_GADGET)

  if new_wavelet:
    new_wavelet.submit_with(old_wavelet)

def AddQuestion(blip, question, name):
  blip.append(question)
  checkbox = element.Check(name, value=False)
  blip.append(checkbox)
  blip.append('.....\n')

def sendMail(wavelet):
  # todo can be on any domain
  mail.send_mail('pamela.fox@gmail.com',
                 REVIEWERS[wavelet.domain],
                 'Extension Submission', 
                 'There is a new extension to review here: \
                 https://wave.google.com/a/%s/#restored:wave:%s' %
                 (wavelet.domain, wavelet.wave_id.replace('+', '%252B')))

if __name__ == '__main__':
  submitty = robot.Robot('Submitty',
                         image_url='http://submitty-bot.appspot.com/img/submitty_avatar.png',
                         profile_url='http://code.google.com/apis/wave/')
  verification_token = ('AOijR2cCA9NekLaoS80yXoHJX4Sy7q7iehU_l_iWho3uck1h8st1d9E8JqLf1L7tL297UCJLHI4SQTgXDn-6yJbaETjNc3QtsJBAQC66vMgqPPpUqFgc1xyWQPKe7N-pXc7JGX6JtUMIyhjt9mttDKTckw7PO9KrhQ==')
  submitty.set_verification_token_info(verification_token, '7186')
  submitty.register_handler(events.WaveletSelfAdded, OnSelfAdded)
  submitty.register_handler(events.DocumentChanged, OnBlipSubmitted)
  submitty.register_handler(events.BlipSubmitted, OnBlipSubmitted)
  submitty.register_handler(events.GadgetStateChanged, OnGadgetStateChanged)
  appengine_robot_runner.run(submitty, debug=True)
