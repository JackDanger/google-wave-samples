#!/usr/bin/python2.4
#
# Copyright 2009 Google Inc. All Rights Reserved.
"""Implementation of Monty robot.

This robot can do two things:
  1. Runs Python code that is submitted into the main blip.
  2. Computes expressions when CALC(X) is submitted in a blip.
"""

__author__ = 'davidbyttow@google.com (David Byttow)'


from api import events
from api import model
from api import robot

# Globals
ROBOT_NAME = 'monty'


class WaveEnv(object):
  def __init__(self, context):
    self.result = None
    self.api = context


def ExecProgram(program, context):
  """Runs a Python program."""
  env = WaveEnv(context)
  result = ''
  try:
    exec(program, {'wave': env}, {})
    output = str(env.result)
    result = 'Output:\n' + output
  except Exception or StandardError, ex:
    exception_info = str(ex).split('\n', 1)[0]
    result = 'Sorry, I did not understand that...\n'
    result += 'Error: ' + exception_info
  return result


def HandleRootBlip(root_blip, context):
  """Runs the contents of the root blip as a Python program."""
  result = ExecProgram(root_blip.GetDocument().GetText(), context)
  comm_blip = None
  for blip_id in root_blip.GetChildBlipIds():
    blip = context.GetBlipById(blip_id)
    if blip.GetDocument().HasAnnotation('blip-comm'):
      comm_blip = blip
      break
  if comm_blip:
    comm_blip.GetDocument().SetText(result)


def HandleChildBlip(blip):
  """Handles CALC(X) macros."""
  content = blip.GetDocument().GetText()
  lines = content.split('\n')
  loc = 0
  for line in lines:
    start = line.find('CALC(')
    if start > -1:
      expr = line[start + 5:]
      start += loc
      # Now attempt to find the last parantheses.
      count = 1
      end = 0
      while count > 0 and end < len(expr):
        if expr[end] == ')':
          count -= 1
        end += 1
      if count == 0:
        expr = expr[:end - 1]
        result = '???'
        try:
          result = str(eval(expr))
        except ZeroDivisionError:
          result = u'\u221E'
        except:
          pass
        r = model.Range(start, start + end + 5)
        blip.GetDocument().SetTextInRange(r, result)
    loc += len(line) + 1


def AddBlip(context):
  """Adds a blip to the root wavelet."""
  wavelet = context.GetRootWavelet()
  return wavelet.CreateBlip()


def OnBlipSubmitted(properties, context):
  """Invoked when any blip we are interested in is submitted."""
  blip_id = properties['blipId']
  blip = context.GetBlipById(blip_id)
  if blip.IsRoot():
    HandleRootBlip(blip, context)
  else:
    HandleChildBlip(blip)


def IsRobotInList(l):
  """Determines if this robot is in the participant list."""
  for participant in l:
    if participant.startswith(ROBOT_NAME):
      return True
  return False


def OnParticipantsChanged(properties, context):
  """Invoked when any participants have been added/removed from the wavelet."""
  added = properties['participantsAdded']
  if IsRobotInList(added):
    Setup(context)


def Setup(context):
  """Called when this robot is first added to the wave."""
  wavelet = context.GetRootWavelet()
  blip = context.GetBlipById(wavelet.GetRootBlipId())
  if blip:
    inline_blip = blip.GetDocument().AppendInlineBlip()
    doc = inline_blip.GetDocument()
    doc.SetText('Hello! I\'m Monty. Please input your program below '
                'and I will take care of the rest.')
    doc.AnnotateDocument('blip-comm', '1')


if __name__ == '__main__':
  monty = robot.Robot(ROBOT_NAME.capitalize(),
                      image_url='/public/%s.png' % ROBOT_NAME,
                      profile_url='_wave/profile.xml')
  monty.RegisterHandler(events.WAVELET_PARTICIPANTS_CHANGED,
                        OnParticipantsChanged)
  monty.RegisterHandler(events.BLIP_SUBMITTED, OnBlipSubmitted)
  monty.Run(debug=True)

