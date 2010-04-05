import datetime
from google.appengine.ext import db

class SearchWave(db.Model):
  wave_json = db.TextProperty()
  creator = db.StringProperty()
  modified = db.DateTimeProperty(auto_now=True)
