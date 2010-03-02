#!/usr/bin/python2.4
import logging
import os

from waveapi import events
from waveapi import robot
from waveapi import element
from waveapi import ops
from waveapi import element
from waveapi import appengine_robot_runner

import gdata.projecthosting.client
import gdata.projecthosting.data
import gdata.gauth
import gdata.client
import gdata.data
import atom.http_core
import atom.core

import item
import models
import util

def AddItems(blip, source):
  items = GetItems(source['project'], source['label'])
  blip.append('Open issues for: %s, %s \n' % (source['project'], source['label']))
  for item in items:
    blip.append('\n')
    blip.append(item.title)
    blip.append('\n')
    blip.append('Looking at this? ')
    blip.append(element.Button(name=(item.id + '-looking'), caption='No'))
    blip.append('Responded? ')
    blip.append(element.Button(name=(item.id + '-responded'), caption='No'))
    blip.all(item.title).annotate('link/manual', item.link)
  blip.append('\n')

def GetItems(project, label):
  issues_client = gdata.projecthosting.client.ProjectHostingClient()
  project_name = project
  query = gdata.projecthosting.client.Query(label=label, status='New')
  feed = issues_client.get_issues(project_name, query=query)
  items = []
  for issue in feed.entry:
    issue_id = issue.id.text.split('/')[-1]
    issue_link = 'http://code.google.com/p/%s/issues/detail?id=%s' % (project_name, issue_id)
    items.append(item.TriageItem(issue_id, issue.title.text, issue_link))
  return items

def OnButtonClicked(event, wavelet):
  clicker = event.modified_by
  clicked = event.properties['button']
  blip = event.blip
  button = blip.first(element.Button, name=clicked)
  if button:
    logging.info('Found button')
    button.update_element({'value': 'Yes'})

def OnSelfAdded(event, wavelet):
  blip = event.blip
  gadget = element.Gadget(url=util.GetGadgetUrl())
  blip.append(gadget)

def OnGadgetChanged(event, wavelet):
  blip = event.blip
  logging.info('gadget changed')
  gadget = blip.first(element.Gadget, url=util.GetGadgetUrl())
  preset_key = gadget.preset_key
  gadget.delete()
  preset = models.TriagePreset.get(preset_key)
  if preset:
    sources = preset.GetSourcesList()
    for source in sources:
      AddItems(blip, source)

if __name__ == '__main__':
  removey = robot.Robot('Bug Triagey',
      image_url='http://bug-triagey.appspot.com/static/avatar.jpg',
      profile_url='')
  removey.register_handler(events.WaveletSelfAdded, OnSelfAdded)
  removey.register_handler(events.GadgetStateChanged, OnGadgetChanged)
  removey.register_handler(events.FormButtonClicked, OnButtonClicked)
  appengine_robot_runner.run(removey, debug=True)
