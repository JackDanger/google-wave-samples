#!/usr/bin/python2.4
#
# Copyright 2010 Google Inc. All Rights Reserved.
import logging
import os

from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app

from django.utils import simplejson

import models
import util

class StartHandler(webapp.RequestHandler):
  def get(self):
    self.Render(self.GetTemplateFilename(), self.GetTemplateValues(), self.GetContentType())

  def GetContentType(self):
    return 'text/html'

  def GetTemplateFilename(self):
    return ''

  def GetTemplateValues(self):
    # render dropdown
    q = models.TriagePreset.all()
    presets = q.fetch(100)
    template_values = {'presets': presets, 'server': util.GetServer()}
    return template_values

  def Render(self, filename, template_values, content_type):
    path = os.path.join(os.path.dirname(__file__), 'templates/' + filename)
    self.response.headers['Content-Type'] = content_type
    self.response.out.write(template.render(path, template_values))

class StartGadgetHandler(StartHandler):
  def GetTemplateFilename(self):
    return 'start_gadget.xml'

  def GetContentType(self):
    return 'text/xml'

class StartPageHandler(StartHandler):
  def GetTemplateFilename(self):
    return 'start_page.html'

class GetPresetsHandler(webapp.RequestHandler):
  def get(self):
    q = models.TriagePreset.all()
    presets = q.fetch(100)
    presets_list = []
    for preset in presets:
      presets_list.append(preset.GetDict())
    json = simplejson.dumps(presets_list)
    self.response.out.write(json)


class GetPresetHandler(webapp.RequestHandler):
  def post(self):
    preset_key = self.request.get('preset_key')
    logging.info('preset_kkey %s' % preset_key)
    preset = models.TriagePreset.get(preset_key)
    json = simplejson.dumps(preset.GetDict())
    self.response.out.write(json)


class EditPresetHandler(webapp.RequestHandler):
  def get(self):
    # fill form with preset info
    preset_key = self.request.get('preset_key')
    preset = models.TriagePreset.get(preset_key)
    if preset is None:
      # error somehow
      pass
    sources = preset.GetSourcesList()
    template_values = {'preset': preset, 'sources': sources}
    path = os.path.join(os.path.dirname(__file__), 'templates/form.html')
    self.response.out.write(template.render(path, template_values))

class SavePresetHandler(webapp.RequestHandler):
  def post(self):
    name = self.request.get('name')
    sources = []
    source_num = 0
    source = self.request.get('source' + str(source_num))
    while source != '':
      source_project = self.request.get('source_project' + str(source_num))
      source_label = self.request.get('source_label' + str(source_num))
      logging.info('project:' + source_project)
      logging.info('label:' + source_label)
      sources.append({'type': 'code', 'project': source_project, 'label': source_label})
      source_num += 1
      source = self.request.get('source' + str(source_num))

    key = self.request.get('key')
    if key != '':
      preset = models.TriagePreset.get(key)
    else:
      preset = models.TriagePreset()
    preset.name = name
    preset.SetSourcesFromList(sources)
    preset.put()
    self.response.out.write('{}')


application = webapp.WSGIApplication(
                                     [
                                     ('/web/start/gadget', StartGadgetHandler),
                                     ('/web/start', StartPageHandler),
                                     ('/web/edit', EditPresetHandler),
                                     ('/web/presets', GetPresetsHandler),
                                     ('/web/preset', GetPresetHandler),
                                     ('/web/save', SavePresetHandler)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()
