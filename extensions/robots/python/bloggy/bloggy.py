#!/usr/bin/python2.4
#
# Copyright 2009 Google Inc. All Rights Reserved.

"""Bloggy: External Participant implementing a simple blogging platform"""

__authors__ =   ['douwe@google.com (Douwe Osinga)',
                 'davidbyttow@google.com (David Byttow)']


import logging
import os
import urllib

from api import document
from api import events
from api import robot
import model

# Globals
ROBOT_NAME = 'bloggy'

# If set to true, skip the confirmation dialog
PUBLISH_IMMEDIATELY = True


def IsProd():
  # If hosting from prod, use the public wave sandbox.
  return os.environ['HTTP_HOST'].endswith('.appspot.com')


def GetKnownDomains():
  if IsProd():
    return set(['wavesandbox.com'])
  return set(['google.com', 'gwave.com', 'gmail.com'])


def GetRobotId():
  if IsProd():
      return 'blog-wave'
  return 'bloggy'


def StripKnownDomains(p):
  address, domain = p.split('@', 1)
  if domain in GetKnownDomains():
    return address
  else:
    return p


def IsBloggy(participant):
  return (participant.startswith(GetRobotId())
      or participant.endswith('@corp.google.com'))


def PublishBlog(wavelet):
  author = StripKnownDomains(wavelet.GetCreator())
  title = 'Untitled'
  if wavelet.GetTitle():
    title = wavelet.GetTitle().splitlines()[0]
  post = model.BlogPost(title=title,
                        author=author,
                        waveid=wavelet.GetWaveId())
  post.put()
  url = 'http://' + os.environ['HTTP_HOST'] + '/' + urllib.quote(author)
  wavelet.SetDataDocument('/published/blog-wave@appspot.com', url)
  wavelet.AddParticipant("public@a.gwave.com")


def InsertPublishForm(blip, title):
  form = blip.GetDocument().InsertInlineBlip(1).GetDocument()
  form.AppendText('\nDo you want to publish this wave to a blog '
                  'and that way share it with the entire world?\n')
  form.AppendElement(
      document.FormElement(
          document.ELEMENT_TYPE.BUTTON,
          'publish',
          value='Publish!',
          ))
  form.AppendElement(
      document.FormElement(
          document.ELEMENT_TYPE.BUTTON,
          'nothanks',
          value='No thanks',
          ))


def OnParticipantsChanged(properties, context):
  """Invoked when any participants have been added/removed from the wavelet."""
  added = properties['participantsAdded']
  for participant in added:
    if IsBloggy(participant):
      wavelet = context.GetRootWavelet()
      if PUBLISH_IMMEDIATELY:
        PublishBlog(wavelet)
      else:
        title = 'Untitled'
        if wavelet.GetTitle():
          title = wavelet.GetTitle().splitlines()[0]
        blip = context.GetBlipById(wavelet.GetRootBlipId())
        InsertPublishForm(blip, title)
      break;


def OnButtonClicked(properties, context):
  wavelet = context.GetRootWavelet()
  blip = context.GetBlipById(properties['blipId'])
  blip.Delete()
  if properties['button'] == 'publish':
    PublishBlog(wavelet)


if __name__ == '__main__':
  bloggy = robot.Robot(ROBOT_NAME.capitalize(),
                       image_url='http://blog-wave.appspot.com/inc/blogger.png',
                       profile_url='http://www.blogger.com')
  bloggy.RegisterHandler(events.WAVELET_PARTICIPANTS_CHANGED,
                         OnParticipantsChanged)
  bloggy.RegisterHandler(events.FORM_BUTTON_CLICKED,
                         OnButtonClicked)
  bloggy.Run(debug=True)
