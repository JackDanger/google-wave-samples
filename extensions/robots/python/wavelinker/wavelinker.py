#!/usr/bin/python2.5
#
"""Robot that auto links to first google results
"""


import logging
import urllib

from waveapi import events
from waveapi import model
from waveapi import robot
from waveapi import robot_abstract
from waveapi import document

from django.utils import simplejson
from google.appengine.api import urlfetch


ROBOT_NAME = 'wavelinker'

def GoogleSearch(q):
  url = ('http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=%s') % (urllib.quote(q))
  js = urlfetch.fetch(url=url).content
  results = simplejson.loads(js)['responseData']['results']
  if not results:
    return None
  res = results[0]
  return res['url']


def Execute(properties, context):
  """Actual linking."""
  blipId = properties['blipId']
  blip = context.GetBlipById(blipId)
  doc = blip.GetDocument()
  text = doc.GetText()
  # construct the todo outside of the loop to avoid
  # influencing what we're observing:
  todo = []
  for ann in blip.annotations:
    if ann.name == 'robot.wavelinker.request':
      todo.append((ann.range.start, ann.range.end, ann.value))
  # now call GoogleSearch for all values and insert a link if we get a matching
  # url:
  for start, end, value in todo:
    payload = text[start:end]
    if value == 'google':
      url = GoogleSearch(payload)
    else:
      continue
    range = document.Range(start, end)
    doc.DeleteAnnotationsInRange(range, 'robot.wavelinker.request')
    if url:
      doc.SetAnnotation(range, 'link/manual', url)


def OnSelfAdded(properties, context):
  """Invoked when any participants have been added/removed from the wavelet."""
  logging.info('OnSelfAdded')
  Execute(properties, context)


def OnDocumentChanged(properties, context):
  """Called when the document changes."""
  # We only care about new docs which are indicated by datadocs:
  logging.info('OnDocumentChanged')
  Execute(properties, context)


if __name__ == '__main__':
  linker = robot.Robot(ROBOT_NAME.capitalize(),
      image_url='http://wavelinker.appspot.com/avatar.png',
      profile_url='http://wavelinker.appspot.com/')
  linker.RegisterHandler(events.WAVELET_SELF_ADDED,
                        OnSelfAdded)
  linker.RegisterHandler(events.DOCUMENT_CHANGED, OnDocumentChanged)
  linker.Run(debug=True)
