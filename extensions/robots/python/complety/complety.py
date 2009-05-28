#!/usr/bin/python2.4
#
# Copyright 2009 Google Inc. All Rights Reserved.
"""Implementation of the Everything and the Kitchen Sink bot

Yet another smiley robot
"""

__author__ = 'douwe@google.com (Douwe Osinga)'

import logging
import urllib
import re

from api import events
from api import robot

from django.utils import simplejson
from google.appengine.api import urlfetch


def OnBlipSubmitted(properties, context):
  blip = context.GetBlipById(properties['blipId'])
  contents = blip.GetDocument().GetText()
  if '???' in contents:
    q = '"%s"' % contents.replace('???', '*').replace('"', ' ')
    start = 0
    res = {}
    for i in range(6):
      url = 'http://ajax.googleapis.com/ajax/services/search/web?v=1.0&start=%d&q=%s' % (start, urllib.quote(q))
      js = urlfetch.fetch(url=url).content
      for fragment in simplejson.loads(js)['responseData']['results']:
        for m in re.findall('\<b\>([^\<]*)', fragment['content']):
          m = m.lower()
          if m == '...':
            continue
          res[m] = res.get(m, 0) + 1
      start += 5
    if res:
      res = res.items()
      res.sort(lambda a,b: -cmp(a[1], b[1]))
      blip.GetDocument().SetText(res[0][0])


if __name__ == '__main__':
  complety = robot.Robot('Complety',
                         image_url='http://complete-wave.appspot.com/inc/complety.png',
                         profile_url='http://www.google.com')

  complety.RegisterHandler(events.BLIP_SUBMITTED, OnBlipSubmitted)
  complety.Run(debug=True)

