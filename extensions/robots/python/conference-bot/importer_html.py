import re

from BeautifulSoup import BeautifulSoup
import util
import data

def processEventDiv(div):
  id = div['id'].split('_')[2]
  img_alt = div.img['alt']
  if img_alt != 'Interactive_track':
    return None
  time = getDivText(div, 'event_clicker')
  venue = getDivText(div, 'event_venue_name')
  name = getDivText(div, 'event_name')
  return {'id': id, 'time': time, 'venue': venue, 'name': name}

def getDivText(div, class_name):
  child_div = div.find('div', {'class': class_name})
  return stripHTML(child_div)

def stripHTML(elem):
  return u' '.join([s.string.replace('\n', ' ').strip() for s in elem.findAll(text=True)])

def createConferenceFromHTML(url):
  html = util.fetchHTML(url)
  if html is None:
    return None
  conf = data.Conference()
  soup = BeautifulSoup(html)
  event_divs = soup.findAll('div', {'class': re.compile('event_(odd|even)')})
  for event_div in event_divs:
    event_info = processEventDiv(event_div)
    if event_info:
      link = 'http://my.sxsw.com/events/event/%s' % event_info['id']
      session = data.Session(event_info['name'], id=event_info['id'], link=link,
                           location=event_info['venue'])
      conf.sessions.append(session)
  return conf
