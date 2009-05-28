#!/usr/bin/python2.4
#
# Copyright 2009 Google Inc. All Rights Reserved.

"""Views module for bloggy.

Currently just brings up a template that looks like blogger with a wave
in it.
"""

__author__ = 'douwe@google.com (Douwe Osinga)'


import logging
import os

from django.template import Context, loader, TemplateDoesNotExist
from django.shortcuts import render_to_response
from django.http import HttpResponse
from django.http import HttpResponseRedirect
from django.utils import html

import model

from xml.sax.saxutils import quoteattr

def Index(request, userid, waveid=None):
  clause = ''
  posts = model.BlogPost.gql('WHERE author = :1 ORDER BY date DESC', userid)
  if not posts or posts.count(5) == 0:
    return render_to_response('noposts.html', {'name': userid})
  if not waveid:
    waveid = posts[0].waveid
  posts = [{'url': '/%s/%s' % (userid, post.waveid),
            'title': post.title} for post in posts]
  if userid == 'lars':
    img = '/inc/yala.jpg'
    name = 'Yarima and Lars'
    title = 'The Adventures of Lars'
  else:
    img = 'http://www.corp.google.com/Photos/big/%s.jpg' % userid
    name = userid.capitalize()
    title = 'The Wave Blog of ' + name

  server = 'http://wave-latest.corp.google.com/a/google.com/'
  host = os.environ['HTTP_HOST']
  # If hosting from prod, use the public wave sandbox.
  if host.endswith('.appspot.com'):
    server = 'http://wave.google.com/a/wavesandbox.com/'

  bgcolor = '#446666'
  color = '#cceedd'
  font = 'verdana'
  d = {
      'server': server,
      'posts': posts,
      'waveid': waveid,
      'img': img,
      'name': name,
      'title': title,
      'bgcolor': bgcolor,
      'color': color,
      'font': font
  }
  return render_to_response('blog.html', d)
