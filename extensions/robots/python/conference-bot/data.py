class Conference():
  # each conference should have a list of sessions
  # each session should have title, and other optional info
  def __init__(self, sessions=None):
    if sessions is None:
      self.sessions = []
    else:
      self.sessions = sessions

class Session():
  def __init__(self, title, link=None, speakers=None, time=None, day=None,
               location=None, description=None, speakers_image=None,
               speakers_description=None):
    self.title = title
    self.link = link
    self.speakers = speakers
    self.speakers_image = speakers_image
    self.speakers_description = speakers_description
    self.time = time
    self.day = day
    self.location = location
    self.description = description
