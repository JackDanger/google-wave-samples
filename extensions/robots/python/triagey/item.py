#!/usr/bin/python2.4
#
# Copyright 2010 Google Inc. All Rights Reserved.

"""One-line documentation for item module.

A detailed description of item.
"""

__author__ = 'pamelafox@google.com (Pamela Fox)'


class TriageItem:
  def __init__(self, id, title, link, snippet=None):
    self.id = id
    self.title = title
    self.link = link
    self.snippet = snippet
