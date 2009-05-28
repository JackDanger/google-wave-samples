#!/usr/bin/python2.4
#
# Copyright 2009 Google Inc. All Rights Reserved.

"""Model module for bloggy."""

__author__ = 'douwe@google.com (Douwe Osinga)'


from google.appengine.ext import db


class BlogPost(db.Model):

  author = db.StringProperty()
  title = db.StringProperty()
  waveid = db.StringProperty()
  date = db.DateTimeProperty(auto_now_add=True)
