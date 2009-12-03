#!/usr/bin/python2.4
#
# Copyright 2009 Google Inc. All Rights Reserved.

"""One-line documentation for htmler module.

A detailed description of htmler.
"""

__author__ = 'pamelafox@google.com (Pamela Fox)'

import logging
import re

def log(message, string):
  logging.info(message + ": " + string)

def convert_to_css_property(annotation_name):
  log("convert_to_css", "")
  split_annotation = annotation_name.split('/')
  style_type = split_annotation[1]
  css_rule = re.sub(r'([A-Z])','-\\1', style_type, 1)
  css_rule = css_rule.lower()
  return css_rule

def convert_to_html(blip):
  # Take in a blip, convert to HTML
  indices = []
  text = blip.GetDocument().GetText()
  i = 0
  while i < len(text):
    indices.insert(i, {"index": i, "character": text[i], "linkStarts": [], "linkEnds": [], "annotationStarts": [], "annotationEnds": []})
    i += 1
    log('char: ', str(i))
  annotations = blip.GetAnnotations()
  for annotation in annotations:
    range = annotation.range
    if "style" in annotation.name:
      #only process these for now
      css_rule = convert_to_css_property(annotation.name)
      indices[range.start]["annotationStarts"].append(annotation)
      indices[range.end]["annotationEnds"].append(annotation)
    if "link" in annotation.name:
      log("found link", annotation.value)
      indices[range.start]["linkStarts"].append(annotation)
      indices[range.end]["linkEnds"].append(annotation)

  html = ""
  active_annotations = []
  open_span = False
  for data in indices:
    closed_span = False
    saw_new = False
    if len(data["annotationEnds"]) > 0:
      html = html + "</span>"
      closed_span = True
      open_span = False

    if len(data["linkEnds"]) > 0:
      html += "</a>"
    if len(data["linkStarts"]) > 0:
      link_starts = data["linkStarts"]
      html += "<a href='" + link_starts[0].value + "'>"

    annotation_starts = data["annotationStarts"]
    if len(annotation_starts) > 0:
      active_annotations.extend(annotation_starts)
      saw_new = True
      if not closed_span:
        html += "</span>"

    if saw_new or closed_span:
      html += "<span style='"
      current_annotations = []
      for annotation in active_annotations:
        if annotation.range.end > data["index"]:
          css_property = convert_to_css_property(annotation.name)
          html += css_property + ":" + annotation.value + ";"
      html += "'>"
      open_span = True
    html = html + data["character"]
    span_now = False
  if open_span:
    html += '</span>'

  log("html", html)
  return html
