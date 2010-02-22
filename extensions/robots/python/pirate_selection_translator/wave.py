#!/usr/bin/python2.4

from waveapi import robot
from waveapi import events
from waveapi import appengine_robot_runner

ROBOT_KEY = 'text-to-translate'

def Translate(english):
  translations = {'yes': 'Yarr!', 'lol': 'Yo-ho-ho!'}
  return translations.get(english)

def OnAnnotationChanged(event, wavelet):
  """Actual linking."""
  blip = event.blip
  text = blip.text
  # construct the todo outside of the loop to avoid
  # influencing what we're observing:
  todo = []
  for ann in blip.annotations:
    if ann.name == ROBOT_KEY:
      todo.append((ann.start, ann.end, ann.value))
  for start, end, value in todo:
    payload = text[start:end]
    blip.range(start, end).clear_annotation(ROBOT_KEY)
    translation = Translate(payload)
    if translation:
      blip.range(start, end).replace(Translate(payload))

if __name__ == '__main__':
  robotty = robot.Robot('Pirate Speaky',
      image_url='',
      profile_url='')
  robotty.register_handler(events.AnnotatedTextChanged, OnAnnotationChanged, filter=ROBOT_KEY)
  robotty.register_handler(events.WaveletSelfAdded, OnAnnotationChanged)
  appengine_robot_runner.run(robotty, debug=True)
