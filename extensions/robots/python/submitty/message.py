prelim = """
Congratulations on completing your extension!

Welcome to the extension review process. We have put together a series of questions that will help us learn more about your extension and how users will use it inside Wave. When you are done responding, you can let us know by clicking the "Share with Reviewers" button above. If you haven't yet, we suggest reading through the <a href="http://code.google.com/apis/wave/extensions/designprinciples.html">design principles</a> and <a href="http://code.google.com/apis/gadgets/docs/publish.html#Hi_Volume">gadgets performance</a> docs.

<b>First, some basic info about you:</b>

<b>Name</b>:
<b>Location</b>:

<b>Now, some info about your extension:</b>

<b>Name</b>:
<b>Sample Wave</b>:
<i>Please host on Google Wave Preview. If it is not a public wave, please add google-wave-extensions-review@googlegroups.com to the wave so that we can access it.</i>
<b>Installer</b>:


"""

prelim_robotq = "Does your extension include a robot?"

prelim_gadgetq = "Does your extension include a gadget?"

robot = """
Great! Please answer the following questions about the robot:

- For each event that the robot reacts to, how does it respond?
- What does the robot do when first added to a Wave (if anything)?
- Can/does this bot work in conjunction with any other robots?
- Does the user have to know some special syntax for using your robot? (We recommend avoiding this when possible)
"""

gadget = """
Great! Please answer the following questions about the gadget:

- What data is shared in the state object? Please describe the key/value mapping.
- How frequenty is the state updated?
- Does the gadget use setModeCallback to provide different behavior in edit
  versus view mode? If so, please describe.
- What does the user see in playback mode?
- How does one user know what/where another user is editing?
- Is the gadget constant sized? If not, please describe the gadget resizing.
- Does the gadget provide instructions to users?
- Does the gadget provide a way for users to give feedback?
"""


questions = """
<b>Performance/Stability:</b>
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
