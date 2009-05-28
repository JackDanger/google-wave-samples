#!/usr/bin/python2.4
#
# Copyright 2009 Google Inc. All Rights Reserved.

"""Settings module for bloggy."""

__author__ = 'douwe@google.com (Douwe Osinga)'


import os


ROOT_URLCONF = 'urls'  # Replace 'project.urls' with just 'urls'

DEBUG = True
TEMPLATE_DEBUG = DEBUG


MIDDLEWARE_CLASSES = (
    'django.middleware.common.CommonMiddleware',
    'django.middleware.doc.XViewMiddleware',
)

INSTALLED_APPS = (
    'django.contrib.contenttypes',
    'django.contrib.sites',
)

APPEND_SLASH=False

ROOT_PATH = os.path.dirname(__file__)

TEMPLATE_DIRS = (
    # Put strings here, like "/home/html/django_templates" or
    # "C:/www/django/templates".  Always use forward slashes, even on Windows.
    # Don't forget to use absolute paths, not relative paths.
    ROOT_PATH + '/templates',
)
