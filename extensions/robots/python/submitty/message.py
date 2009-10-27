questions = """
For any robot in the extension:

- What events does the robot react to? How does it react?

- What does the robot do when first added to a Wave (if anything)?

- Have you tested your robot in conjunction with other robots?

- For any gadget in the extension:

- What data is shared in the state object? Describe the key/value mapping.

- At what points are state deltas submitted? How fine-grained are the deltas?

- When a new state is received, how does the gadget process it to produce the new view?

- In edit mode, what can the user do? Is there an indication that it is in edit mode?

- In view mode, what can the user do? Is there an indication that it is in view mode?

- When the gadget goes from view mode to edit mode, what is the transition like? What about when it goes from edit to view?

- What does the user see in playback mode?

- What do users see when they first add the gadget? (Are there any special instructions?)

- How does one user know what another user is editing? (What indicators are used?)

Ease of Use;

- For robots: Does the user have to know some special syntax for using your robot? (We recommend avoiding this when possible).

- For gadgets: Does the gadget provide some instructions to users? When are these instructions displayed to users, and do they describe what other users will see?

Ease of Installation:

- Do you provide an extension installer?

Looking Good:

- Describe how you have designed your toolbar icon to be recognizable to users.

- If robot involved, does your robot avatar somehow relate to the toolbar icon?

- If gadget involved:

- Describe the gadget sizing. Is it constant sized? If not, when does it resize?

- Does the gadget have a clear border around it?

Performance/Stability:

- If it is a gadget:

What other resources does your gadget bring in? For each resource, please note the server that the resource is on, and if you are using gadget caching or some other caching technique.

- If it is a robot:

What additional web services does it contact? For each web service, please note the rate limits and expected load of that service.

Have you enabled billing on your app engine account for your robot's app?

Have you profiled your robot to make sure that your code is optimized in terms of CPU usage?

If you are using a datastore on App Engine, please describe the reads and writes, and whether memcache is being used to cache hits.

If storing static files on App Engine, please note the cache headers that you've used.

If rendering dynamic pages on App Engine, please comment on the use of memcache for temporarily caching these renders.
"""
