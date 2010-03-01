import cgi
import os
import models

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db
from google.appengine.ext.webapp import template

class MainPage(webapp.RequestHandler):
  def get(self):
    waveId = self.request.get('waveId').replace('%2B', '+')
    query = db.Query(models.WaveExport)
    query.filter('id =', waveId)
    result = query.get()

    if result is None:
      template_values = {
        'title': 'Wave Not Found',
        'body': '<error>Please check the ID</error>',
        'id': waveId
      }
    else: 
      template_values = {
        'title': result.title,
        'body': result.body,
        'id': waveId
      }

    filename = 'export.xml'

    path = os.path.join(os.path.dirname(__file__), 'export.xml')
    self.response.headers['Content-Type'] = 'text/xml'
    self.response.out.write(template.render(path, template_values))

application = webapp.WSGIApplication(
                                     [('/export', MainPage)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()
