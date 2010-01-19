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
        'body': 'Please check the ID',
        'id': waveId
      }
    else:
      participants = result.participants
      canDisplayWave = False
      if 'public@a.gwave.com' in participants:
        canDisplayWave = True
      else:
        user = users.get_current_user()
        if not user:
          self.redirect(users.create_login_url(self.request.uri))
          return
        sandboxEmail = user.email().replace('@gmail.com', '@wavesandbox.com')
        if sandboxEmail in participants:
          canDisplayWave = True
      id_domain = waveId.split('!')[0]
      if id_domain == 'googlewave.com':
        dir = 'wave/'
      else:
        dir = 'a/' + id_domain + '/'

      if canDisplayWave:
        template_values = {
          'title': result.title,
          'html': result.html,
          'body': result.body,
          'id': waveId,
          'dir': dir,
          'url': 'https://wave.google.com/' + dir + '#minimized:nav,minimized:contact,minimized:search,restored:wave:' + waveId.replace('+', '%252B')
        }
      else:
        template_values = {
          'title': 'Wave Access Denied',
          'body': 'This Wave is not public, and ' + sandboxEmail + ' is not a participant.',
          'id': waveId
        }

    format = self.request.get('template')
    if format and format == 'xml':
      filename = 'export.xml'
    else:
      filename = 'export.html'

    path = os.path.join(os.path.dirname(__file__), filename)
    self.response.out.write(template.render(path, template_values))

  
application = webapp.WSGIApplication(
                                     [('/export', MainPage)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()
