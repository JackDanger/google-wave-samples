from google.appengine.ext import db

class WaveExport(db.Model):
  title = db.StringProperty()
  body = db.TextProperty()
  id = db.StringProperty()
  participants = db.StringListProperty()
  created = db.DateTimeProperty(auto_now_add=True)
