import datetime
from google.appengine.ext import db

class SearchWave(db.Model):
  wave_json = db.TextProperty()
  last_id = db.StringProperty()
  search_term = db.StringProperty()
