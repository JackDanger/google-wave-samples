from waveapi import events
from waveapi import robot
from waveapi import element
from waveapi import appengine_robot_runner
import logging

ROBOT_KEY = 'best-annotation-ever'

def OnWaveletSelfAdded(event, wavelet):
  ProcessText(event, wavelet)
  ProcessAnnotations(event, wavelet)

def OnBlipSubmitted(event, wavelet):
  ProcessText(event, wavelet)

def OnAnnotationChanged(event, wavelet):
  ProcessAnnotations(event, wavelet)

def ProcessText(event, wavelet):
  event.blip.all('lolcat').replace(element.LolCat(mini=True))

def ProcessAnnotations(event, wavelet):
  blip = event.blip
  todo = []
  for ann in blip.annotations:
    if ann.name == ROBOT_KEY:
      todo.append((ann.start, ann.end, ann.value))

  for start, end, value in todo:
    payload = blip.text[start:end]
    blip.range(start, end).clear_annotation(ROBOT_KEY)
    blip.range(start, end).delete()
    pos = min(start, len(blip.text)-1)
    if value == 'blink':
      blip.at(pos).insert(element.Blink(payload))
    elif value == 'marquee':
      blip.at(pos).insert(element.Marquee(payload))

if __name__ == '__main__':
  myRobot = robot.Robot('Best Robot Ever', 
                        image_url='http://best-website-ever.appspot.com/images/hamsterdance2.gif')
  myRobot.register_handler(events.WaveletSelfAdded, OnWaveletSelfAdded)
  myRobot.register_handler(events.BlipSubmitted, OnBlipSubmitted)
  myRobot.register_handler(events.AnnotatedTextChanged, OnAnnotationChanged, filter=ROBOT_KEY)
  appengine_robot_runner.run(myRobot)
