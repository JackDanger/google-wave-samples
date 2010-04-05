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
import content

# the robot
myrobot = None
domain = 'googlewave.com'

class CreateHandler(webapp.RequestHandler):
  _robot  = None

  # override the constructor
  def __init__(self, robot):
    self._robot  = robot
    webapp.RequestHandler.__init__(self)

  def get(self):
    address = 'public@a.gwave.com'
    wavelet = self._robot.new_wave(domain     = domain,
                                participants = [address,
                                                'doctorwave.gallery@googlewave.com'],
                                submit       = True)
    wavelet.title = ('Ways to use Wave')
    num = random.randint(0, len(content.messages)-1) 
    wavelet.root_blip.append(content.messages[num])
    self._robot.submit(wavelet)



if __name__ == '__main__':
  myrobot = robot.Robot('Pirate Treasure Hunt',
                        image_url='http://www.traillink.com/images/mapIcons/national/stateMarker50.png')
  myrobot.set_verification_token_info(credentials.VERIFICATION_TOKEN, credentials.ST)
  myrobot.setup_oauth(credentials.CONSUMER_KEY, credentials.CONSUMER_SECRET,
                      credentials.RPC_BASE[domain])
  appengine_robot_runner.run(myrobot, debug=True, extra_handlers=[
      ('/web/cron', lambda: CreateHandler(myrobot))
  ]
)
