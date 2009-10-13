#!/usr/bin/python2.4
#
# Copyright 2009 Google Inc. All Rights Reserved.
"""Implementation of a game playing robot.

This robot mostly demonstrates how gadgets and robots can communicate.
"""

import logging

from api import document
from api import events
from api import robot

PLAYERID = 'rowoffour@appspot.com'

SCORE = [0, 1, 3, 10, 100]
ROW = [10, 8, 7, 6, 5, 4]

def FreeSpot(board, x):
  y = 5
  while y >= 0:
    if board[(x,y)] != 'empty':
      if y == 5:
        return -1
      else:
        return y + 1
    y -= 1
  return 0


def EvalBoard(board, color):
  score = 0
  for y in range(6):
    for x in range(7):
      for i in range(16):
        if i % 4 == 0:
          us = 0
          them = 0
        if i <= 3:
          if x >= 4: continue
          x1 = x + i
          y1 = y
        elif i <= 7:
          if y >= 3: continue
          x1 = x
          y1 = y + i - 4
        elif i <= 11:
          if y >= 3 or x >= 4:
            continue
          x1 = x + i - 8
          y1 = y + i - 8
        else:
          if y >= 3 or x < 3:
            continue
          x1 = x - i + 12
          y1 = y + i - 12
        if board[(x1,y1)] == color:
          us += 1
        elif board[(x1,y1)] != 'empty':
          them += 1
        if (i + 1) % 4 == 0:
          if them == 0 or us == 0:
            delta = (SCORE[us] - SCORE[them]) * ROW[y]
            score += (SCORE[us] - 1.2 * SCORE[them]) * ROW[y]
  return score


def NextMove(board, color):
  bestscore = None
  bestx = None
  for x in range(7):
    y = FreeSpot(board, x)
    if y != -1:
      board[(x,y)] = color
      score = EvalBoard(board, color)
      board[(x,y)] = 'empty'
      if bestscore is None or score > bestscore:
        bestscore = score
        bestx = x
  return bestx


def OnBlipSubmitted(properties, context):
  logging.info('OnBlipSubmitted')
  blip_id = properties['blipId']
  blip = context.GetBlipById(blip_id)
  doc = blip.GetDocument()
  gadget = blip.GetGadgetByUrl('http://rowoffour.appspot.com/gadget.xml')
  if not gadget:
    logging.info('No gadget found. odd')
  else:
    color = gadget.get('color', 'red');
    if color == 'red':
      nextcolor = 'blue'
    else:
      nextcolor = 'red'
    if gadget.get(nextcolor, '') == PLAYERID:
      logging.info('We are the other guy')
      # we're the other guy
      return
    if gadget.get(color, PLAYERID) != PLAYERID:
      logging.info('Not our turn')
      # not our turnGadgetSubmitDelta
      return

    board = {}
    for x in range(7):
      for y in range(6):
        key = 'c%d%d' % (y, x)
        if hasattr(gadget, key):
          val = getattr(gadget, key)
        else:
          val = 'empty'
        board[(x, y)] = val
    x = NextMove(board, color)
    y = FreeSpot(board, x);
    if y == -1:
      return
    delta = {}
    delta['c%d%d' % (y, x)] = color;
    delta[color] = PLAYERID;
    delta['color'] = nextcolor;
    doc.GadgetSubmitDelta(gadget, delta)


def OnSelfAdded(properties, context):
  blip = context.GetBlipById(context.GetRootWavelet().GetRootBlipId())
  if blip:
    blip.GetDocument().SetText('A row of four\nLets play that game!')
    blip.GetDocument().AppendElement(
        document.Gadget('http://rowoffour.appspot.com/gadget.xml'))


if __name__ == '__main__':
  bot = robot.Robot('RowOfFour',
                     '1',
                     image_url='http://rowoffour.appspot.com/red.png',
                     profile_url='http://code.google.com')

  bot.RegisterHandler(events.BLIP_SUBMITTED, OnBlipSubmitted)
  bot.RegisterHandler(events.WAVELET_SELF_ADDED, OnSelfAdded)
  bot.Run(debug=True)
