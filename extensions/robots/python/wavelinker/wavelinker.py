#!/usr/bin/python2.5
#
"""Robot that auto links to first google results
"""

import logging
import urllib

from waveapi import events
from waveapi import robot
from waveapi import element
from waveapi import ops
from waveapi import element
from waveapi import appengine_robot_runner

from django.utils import simplejson
from google.appengine.api import urlfetch

ROBOT_KEY = 'robot.wavelinker.request'

def GoogleSearch(q, site=''):
  q += ' site:%s' % site
  logging.info('q' + q)
  url = ('http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=%s') % (urllib.quote(q))
  js = urlfetch.fetch(url=url).content
  results = simplejson.loads(js)['responseData']['results']
  if not results:
    return None
  res = results[0]
  return res['url']


def Execute(event, wavelet):
  """Actual linking."""
  blip = event.blip
  text = blip.text
  # construct the todo outside of the loop to avoid
  # influencing what we're observing:
  todo = []
  for ann in blip.annotations:
    if ann.name == ROBOT_KEY:
      todo.append((ann.start, ann.end, ann.value))
  # now call GoogleSearch for all values and insert a link if we get a matching
  # url:
  for start, end, value in todo:
    payload = text[start:end]
    value_split = value.split('/', 1)
    if value.startswith('search/'):
      if len(value_split) > 1:
        url = GoogleSearch(payload, value_split[1])
      else:
        url = GoogleSearch(payload)
    elif value.startswith('prefix/'):
      if len(value_split) > 1:
        url = 'http://%s%s' % (value_split[1], payload)
    else:
      continue
    blip.range(start, end).clear_annotation(ROBOT_KEY)
    if url:
      blip.range(start, end).annotate('link/manual', url)


def OnSelfAdded(event, wavelet):
  """Invoked when any participants have been added/removed from the wavelet."""
  Execute(event, wavelet)


def OnDocumentChanged(event, wavelet):
  """Invoked when any participants have been added/removed from the wavelet."""
  Execute(event, wavelet)

def OnAnnotationChanged(event, wavelet):
  """Called when the document changes."""
  # We only care about new docs which are indicated by datadocs:
  Execute(event, wavelet)

if __name__ == '__main__':
  linker = robot.Robot('Wave Linker',
      image_url='http://www.seoish.com/wp-content/uploads/2009/04/wrench.png',
      profile_url='')
  linker.register_handler(events.WaveletSelfAdded, OnSelfAdded)
  linker.register_handler(events.DocumentChanged, OnDocumentChanged)
  linker.register_handler(events.AnnotatedTextChanged, OnAnnotationChanged, filter=ROBOT_KEY)
  appengine_robot_runner.run(linker, debug=True)
