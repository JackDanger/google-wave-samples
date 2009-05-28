#!/usr/bin/python2.4
#
# Copyright 2009 Google Inc. All Rights Reserved.

"""Urls module for bloggy."""

__author__ = 'douwe@google.com (Douwe Osinga)'


from django.conf.urls.defaults import *


urlpatterns = patterns('',
    (r'^$', 'views.Index'),
    (r'^(?P<userid>[^\/]+)$', 'views.Index'),
    (r'^(?P<userid>[^\/]+)/(?P<waveid>.*)$', 'views.Index'),
)
