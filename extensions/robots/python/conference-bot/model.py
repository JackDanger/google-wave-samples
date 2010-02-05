import datetime
from google.appengine.ext import db
from google.appengine.api import users

class ConferenceStore(db.Model):
  owner = db.StringProperty()
  toc_wave = db.StringProperty()
  session_waves = db.StringListProperty()
  name = db.StringProperty()
