import logging

from waveapi import appengine_robot_runner
from waveapi import element
from waveapi import events
from waveapi import ops
from waveapi import robot

# the robot
sinky = None

def OnBlipSubmitted(event, wavelet):
  """Invoked when any blip we are interested in is submitted."""
  logging.info('OnBlipSubmitted')
  blip = event.blip

  # blip.first(element.Gadget,...) actually returns a BlipRefs object
  # i.e. an iterator to blip matches. However BlipRefs implement a
  # truth function to see whether anything matches. Furthermore they
  # forward __getattr__ to their target. This makes .get() below
  # work.
  gadget = blip.first(element.Gadget,
      url='http://kitchensinky.appspot.com/public/embed.xml')
  if gadget:
    logging.info('Gadget. loaded:%s seen:%s' % (getattr(gadget, 'loaded', 'no'),
      gadget.get('seen', 'no')))
  if (gadget
      and gadget.get('loaded', 'no') == 'yes'
      and gadget.get('seen', 'no') == 'no'):
    # elements should always be updated through a BlipRefs to correspond
    # the matching operations for the wire.
    gadget.update_element({'seen': 'yes'})
    blip.append('\nSeems all to have worked out.')
    image = blip.first(element.Image)
    image.update_element({'url': 'http://www.google.com/logos/poppy09.gif'})


def OnSelfAdded(event, wavelet):
  """Invoked when any participants have been added/removed from the wavelet."""
  logging.info('OnSelfAdded')
  blip = event.blip
  wavelet.title = 'A wavelet title'
  blip.append(element.Image(url='http://www.google.com/logos/clickortreat1.gif',
                            width=320, height=118))

  # add a reply to the blip authored by a proxy. Effectively
  # the address on this will be kitchensinky+proxy@appspot.com.
  # Note that as a side effect this will also add this
  # participant to the wave.
  wavelet.proxy_for('proxy').reply().append('hi from douwe')
  inlineBlip = blip.insert_inline_blip(5)
  inlineBlip.append('hello again!')

  # Create a new wave. The new wave will have its own operation queue.
  # new_wave also takes an optional 'message' parameter which can be
  # set to an arbitrary string. By setting it to the serialized version
  # of the current wave, we can reconstruct the current wave when the
  # other wave is constructed and update the current wave.
  new_wave = sinky.new_wave(wavelet.domain,
                            wavelet.participants,
                            message=wavelet.serialize())
  new_wave.root_blip.append('A new day and a new wave')
  new_wave.root_blip.append_markup(
      '<p>Some stuff!</p><p>Not the <b>beautiful</b></p>')

  # since the new wave has its own operation queue, we need to submit
  # it explicitly through the active gateway, or, as in this case,
  # submit it together with wavelet, which will handle the submit
  # automatically.
  new_wave.submit_with(wavelet)


def OnWaveletCreated(event, wavelet):
  """Called when the robot creates a new wave."""
  logging.info('OnWaveletCreated')

  # Recreate the wavelet in which context this new wavelet was
  # created (i.e. the one we were originally added to). This is a
  # "blind" wavelet since any operations applied to this wavelet
  # are done without us really knowing what the state of the wavelet
  # is (it might have changed on the server). This means we have
  # to be careful.
  org_wavelet = wavelet.robot.blind_wavelet(event.message)

  # add a gadget that embeds the newly created wave to the original
  # wavelet.
  gadget = element.Gadget(
      'http://kitchensinky.appspot.com/public/embed.xml')
  gadget.waveid = wavelet.wave_id
  org_wavelet.root_blip.append(gadget)
  reply = org_wavelet.reply()
  reply.append('Some test text')
  org_wavelet.tags.append('activetag')

  # insert some non standard ascii characters:
  org_wavelet.root_blip.append('\nInserted a gadget:')

  # again we have to explicitly submit the operations to the other
  # wavelet
  org_wavelet.submit_with(wavelet)


def OnBlipDeleted(event, wavelet):
  logging.info('blip deleted')

if __name__ == '__main__':
  sinky = robot.Robot('Kitchensinky',
      image_url='http://kitchensinky.appspot.com/public/avatar.png')
  sinky.register_handler(events.WaveletSelfAdded,
                        OnSelfAdded)
  sinky.register_handler(events.WaveletCreated,
                        OnWaveletCreated)
  sinky.register_handler(events.BlipSubmitted, OnBlipSubmitted)
  sinky.register_handler(events.WaveletBlipRemoved, OnBlipDeleted)

  appengine_robot_runner.run(sinky, debug=True)
