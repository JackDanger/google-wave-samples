#!/usr/bin/python2.4
#
# Copyright 2010 Google Inc. All Rights Reserved.

"""Tests for robot."""

__author__ = 'pamelafox@google.com (Pamela Fox)'

import unittest

import robot

class RobotTest(unittest.TestCase):

  def testGetItems(self):
    robot.GetItems()
    pass

if __name__ == '__main__':
  unittest.main()
