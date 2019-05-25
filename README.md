#Helix Audio Clip Looper

Simple Java application to push an audio clip into the Line 6 looper and set accurate loop points.

To use, follow these steps:

 * Select the correct MIDI device (usually "HELIX" if you are plugged in via USB)
 * Select an audio device that is routed to the looper
 * Drag and drop a file from your file explorer onto the gray panel
 * Hit 'Play'

This program has not been tested on Windows or Linux- let me know if you use it and/or have issues!

The HELIX looper does seem to be a bit sketchy- you cane easily crash the whole device by sending it too many commands, and sometimes it randomly ignores messages (which obviously screws this app up!)

Audio Device Routing
====================

I create an additional routing on the helix from USB input 3/4 mixing directly into the main signal path, before the looper.

![Alt text](https://github.com/guyburton/HelixAudioClipLooper/blob/master/screenshot.png?raw=true "screenshot")

An alternative to this would be to use a different audio output device plugged into the audio return inputs of the helix and use a "Return" block before your looper.

The clip audio will be sent to channels 1/2 of the selected audio interface (limitation of java).
Since the Helix designates channels 1/2 to go directly to the main outputs this is a problem.
On OS X I solve this by creating an aggregate audio device including the Helix, and then configure the speaker settings to use channels 3/4 as the stereo output.
