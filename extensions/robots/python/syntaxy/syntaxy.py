"""
Copyright 2009 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
"""

from waveapi import events
from waveapi import robot
from waveapi.document import Range

from pygments import lexers
from pygments import highlight
from pygments.formatter import Formatter
from pygments.token import Token

COLORS = {
  Token.Keyword:("#335588", "bold"),
  Token.Comment:("#7777FF", None),
  Token.Literal.String:("#007000", None),
  Token.Operator:("#663388", "bold"),
  Token.Punctuation:("#663388", None),
  Token.Name.Function:("#550000", None),
  Token.Name.Namespace:("#550000", None),
  Token:(None, None) }

WELCOME_TEXT = "\nSyntaxy is being used in this wave, put #!<language> (e.g. #!python) at the start of a blip to enable syntax highlighting\n\n"

def OnBlipSubmit(properties, context):
  blip = context.GetBlipById(properties['blipId']).GetDocument()
  HighlightBlip(blip)

def HighlightBlip(blip):
  """Given an blip document, if the blip contains a #! syntax highlight that blip"""
  text = blip.GetText()
  if text.lstrip()[:2] == "#!":
    lang = text.lstrip()[2:20].split()
    if len(lang) > 0:
      lang = lang[0]
      range = Range(text.find(lang) + len(lang), len(blip.GetText()))
      # only highlight after the #!language 
      HighlightRange(blip, range, lang)

def GetLexer(lang):
  """ Attempts to find a lexer according to the contents of the #! first by language name then by file extension """
  try:
    lex = lexers.get_lexer_by_name(lang)
    return lex
  except:
    pass
  filename = "Filename." + lang
  try:
    lex = lexers.guess_lexer_for_filename(filename, "")
    return lex
  except:
    pass
  return None

def HighlightRange(blip, range, lang):
  """ Highlight within a specific range within the text of a blip """
  # Set the text to an unknown language so Spelly will (hopefully) leave it alone
  blip.SetAnnotation(range, "lang", "xx")
  otext = blip.GetTextRange(range)
  text = otext.lstrip()
  # adjust range to cut leading whitespace
  range.start = range.start + (len(otext) - len(text))

  # Change to a monospace font
  blip.SetAnnotation(range, "style/fontFamily", "Courier New,monospace")

  # find the right lexer for this language
  lex = GetLexer(lang)
  if lex:
    formatter = WaveFormatter()
    formatter.setBlipChunk(blip, range, text)
    highlight(text, lex, formatter)

class WaveFormatter (Formatter):
  """ Formatter for Pygments library to apply syntax highlighting to a blip """
  def setBlipChunk(self, blip, range, text):
    self.blip = blip
    self.end = range.start 
    self.text = text

  def annotateToken(self, tokenstring, color, bold=None):
    start = self.end 
    end = len(tokenstring) + self.end
    range = Range(start, end)
    self.blip.SetAnnotation(range, "style/color", color) 
    self.blip.SetAnnotation(range, "style/fontWeight", bold)

  def format(self, tokensource, outfile):
    for (tokentype, tokenstring) in tokensource:
      parents = tokentype.split()
      # token types are hierarchical
      # the list is in this order with the most specific last
      for tok in parents[::-1]:
        if tok in COLORS:
          (color, bold) = COLORS[tok]
          self.annotateToken(tokenstring, color, bold)
          break;
      self.end += len(tokenstring)
    return ""

def OnLoad(properties, context):
  """ When the robot first is added to a wave, add a welcome message to the title blip
  and recursively check all blips for highlighting.
  This does not work because only the root blip is sent here, this is a defect
  of the wave robot api."""
  root_blip_id = context.GetRootWavelet().GetRootBlipId()
  blip = context.GetBlipById(root_blip_id)
  blip.GetDocument().InsertText(1, WELCOME_TEXT)
  RecAnnotateBlips(blip, context)

def RecAnnotateBlips(blip, context):
  """ Recursively traverse the wavelet tree"""
  HighlightBlip(blip.GetDocument())
  for c in blip.GetChildBlipIds():
    child_blip = context.GetBlipById(c)
    if child_blip:
      RecAnnotateBlips(child_blip, context)

if __name__ == '__main__':
  lexers.get_all_lexers()
  myRobot = robot.Robot('kasyntaxy', 
    image_url='http://kasyntaxy.appspot.com/assets/avatar.gif',
    version='1.7',
    profile_url='http://kasyntaxy.appspot.com/')
  myRobot.RegisterHandler(events.DOCUMENT_CHANGED, OnBlipSubmit)
  myRobot.RegisterHandler(events.WAVELET_SELF_ADDED, OnLoad)
  myRobot.Run()
