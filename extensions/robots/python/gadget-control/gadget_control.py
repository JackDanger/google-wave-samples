## -*- coding: utf-8 -*-

"""
Copyright 2009 Google Inc.
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

"""
This robot adds counter gadget automatically to each blip
(except for root blip which is top one), and put summary of
counts of all blips to root blip every time gadget is clicked.

KNOWN ISSUE:
The count in the summary (in root blip) is often actual count - 1.
This is because gadget state robot receives is the state before
the last change was applied. This is probably bug of current Wave.

In this robot I use content of root blip of the wave to keep state,
but it would be cleaner to use DataDocument, which is key-value pairs
associated with each wavelet hidden to the user. You can access it with
Wavelet.GetDataDocument() and OpBasedWavelet.SetDataDocument().
"""

import logging
import re
import time
from waveapi import events
from waveapi import model
from waveapi import robot
from waveapi import document

GADGET_URL = 'http://wave-api.appspot.com/public/gadgets/hellowave.xml'

def OnBlipSubmitted(properties, context):
  """This is called when blip is created/updated.
  This is also called when state of gadget in blip is changed."""
  root_wavelet = context.GetRootWavelet()
  root_blip = context.GetBlipById(root_wavelet.GetRootBlipId())
  edited_blip = context.GetBlipById(properties['blipId'])
  text = edited_blip.GetDocument().GetText()
  if edited_blip.GetId() == root_blip.GetId(): return
  edited_gadget = (edited_blip.GetGadgetByUrl(GADGET_URL))
  if edited_gadget:
    # Parses summary in root blip and update it with new count.
    summary = root_blip.GetDocument().GetText()
    counts = {}
    subjects = {}
    for line in summary.split('\n'):
      if not line: continue
      m = re.search(r'^(.*) \((.*)\): (\d+)$', line)
      if not m: continue
      counts[m.group(2)] = int(m.group(3))
      subjects[m.group(2)] = m.group(1)
    if hasattr(edited_gadget, 'count'):
      # You can read "state" data of gadget like this.
      # edited_gadget.count corresponds to value for key "count"
      # in gadget state.
      counts[edited_blip.GetId()] = int(edited_gadget.count)
    else:
      counts[edited_blip.GetId()] = 0
    subjects[edited_blip.GetId()] = (
        edited_blip.GetDocument().GetText().strip().split('\n')[0])
    summary = ''
    for blip_id, count in sorted(counts.iteritems(), key=lambda (k, v): -v):
      summary += '%s (%s): %s\n' % (subjects[blip_id], blip_id, count)
    root_blip.GetDocument().SetText(summary)
  else:
    # Adds gadget to the blip.
    edited_gadget = document.Gadget(GADGET_URL)
    edited_blip.GetDocument().AppendElement(edited_gadget)

if __name__ == '__main__':
  # Change this to your registered application name.
  app_name = 'your-app-name'
  my_robot = robot.Robot(app_name, 
      image_url='http://%s.appspot.com/images/icon.png' % app_name,
      # Forces reloading every time, useful for debugging.
      version=str(int(time.time())),
      profile_url='http://%s.appspot.com/' % app_name)
  my_robot.RegisterHandler(events.BLIP_SUBMITTED, OnBlipSubmitted)
  my_robot.Run()

