#!/usr/bin/python2.4
#
# Copyright 2009 Google Inc. All Rights Reserved.
"""Implementation of Graphy robot."""

import logging
import re

from api import document
from api import events
from api import model
from api import robot

ROBOT_NAME = 'graphy'


class WaveEnv(object):
  def __init__(self, context):
    self.result = None
    self.api = context

def FindImageElement(blip):
  elements = blip.GetDocument()._blip_data.elements
  if elements:
    for key in elements:
      element = elements[key]
      if element['type'] == 'IMAGE' and u'properties' in element and \
          u'url' in element['properties']:
        url = element['properties']['url']
        
        if "http://chart.apis.google.com/chart?" in url:
          return (element, key)
  return (None, None)

def GenerateChartUrlFromText(text):
  if "graph:" not in text.lower():
    return None
  after = text[(text.lower().find("graph:") + 6):]
  datarows = []
  
  datalinere = re.compile(
      "([^:]+[:])?([ \t]*[0-9]+)([ \t]*[,][ \t]*[0-9]+)*[ \t]*")
  mdata = 0
  for line in after.split("\n"):
    linematch = datalinere.match(line.strip())
    if linematch:
      line = linematch.group(0)
      row = {}
      if ":" in line:
        sl = line.split(":")
        row['name'] = sl[0]
        line = sl[1]
      dataelems = [i.strip() for i in line.split(",")]
      m = max([float(i) for i in dataelems])
      if m > mdata:
        mdata = m
      row['data'] = dataelems
      datarows.append(row)
  dim = 0
  for row in datarows:
    d = len(row['data'])
    if d > dim:
      dim = d
  
  mdata = mdata / 100.0
  if dim == 1:
    # pie chart
    nums = []
    names = []
    for row in datarows:
      nums.append(row['data'][0])
      if 'name' in row:
        names.append(row['name'])
    url = "http://chart.apis.google.com/chart?cht=p3&chs=375x150&chd=t:"
    url += ",".join([str(float(i)/mdata) for i in nums])
    if len(nums) == len(names):
      url += "&chl=" + "%7C".join(names)
    url = url.replace(" ", "%20")
    logging.info("Pie chart url: " + url)
    return url
  else:
    # line graph
    url = "http://chart.apis.google.com/chart?cht=lc&chs=375x150"
    # line colors
    url += "&chco=ff0000,0000ff,00ff00,ffff00,00ffff,ff00ff"
    # background gradiant
    url += "&chf=c,lg,90,76A4FB,0,FFFFFF,0.75|bg,s,FFFFFF"
    # data
    url += "&chd=t:"
    nums = []
    names = []
    for row in datarows:
      nums.append(row['data'])
      if 'name' in row:
        names.append(row['name'])
    url += "|".join([",".join([str(float(j)/mdata) for j in i]) for i in nums])
    if len(nums) == len(names):
      url += "&chdl=" + "|".join(names)
    url = url.replace(" ", "%20")
    logging.info("Line graph url: " + url)
    return url

def OnDocumentChanged(properties, context):
  """Invoked when the document changes."""
  blip_id = properties['blipId']
  blip = context.GetBlipById(blip_id)
  text = blip.GetDocument().GetText()
  url = GenerateChartUrlFromText(text)
  if not url:
    return
  imgElement, elemnum = FindImageElement(blip)
  if imgElement:
    if imgElement['properties']['url'] == url:
      logging.info("Url already exists.")
      return

  element = document.Image(url)
  if imgElement:
    blip.GetDocument().ReplaceElement(elemnum, element)
  else:
    blip.GetDocument().AppendElement(element)

if __name__ == '__main__':
  graphy = robot.Robot(ROBOT_NAME.capitalize())
  graphy.RegisterHandler(events.DOCUMENT_CHANGED,
                       OnDocumentChanged)
  graphy.Run(debug=True)
