# Apache licensed
import logging
import random

from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from waveapi import appengine_robot_runner
from waveapi import element
from waveapi import events
from waveapi import ops
from waveapi import robot

import credentials

# the robot
myrobot = None
domain = 'wavesandbox.com'

class CreateHandler(webapp.RequestHandler):
  _robot  = None

  # override the constructor
  def __init__(self, robot):
    self._robot  = robot
    webapp.RequestHandler.__init__(self)

  def get(self):
    address = self.request.get('address') or 'public@a.gwave.com'
    note = self.request.get('note') or ''
    url = self.request.get('url')
    callback = self.request.get('callback')
    # create a new wave, submit immediately
    wavelet = self._robot.new_wave(domain     = domain,
                                participants = [address],
                                submit       = True)
    wavelet.title = ('Note')
    wavelet.root_blip.append(note)
    wavelet.root_blip.append('\n\nNoted from: %s' % url)

    self._robot.submit(wavelet)
    
    if wavelet.wave_id:
      json = '{"status": "success", "wave_id": "%s"}' % wavelet.wave_id
    else:
      json = '{"status": "error"}'
    if callback:
      self.response.out.write(callback + '(' + json + ')')
    else:
      self.response.out.write(json)


if __name__ == '__main__':
  myrobot = robot.Robot('Wave Noter',
                        image_url='http://www.traillink.com/images/mapIcons/national/stateMarker50.png')
  myrobot.set_verification_token_info(credentials.VERIFICATION_TOKEN, credentials.ST)
  myrobot.setup_oauth(credentials.CONSUMER_KEY, credentials.CONSUMER_SECRET,
                      credentials.RPC_BASE[domain])
  appengine_robot_runner.run(myrobot, debug=True, extra_handlers=[
      ('/web/create', lambda: CreateHandler(myrobot))
  ]
)
