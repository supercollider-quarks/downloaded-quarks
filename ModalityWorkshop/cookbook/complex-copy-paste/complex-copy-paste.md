#  Trigger sounds and copy/paste from one button to another
formerly known as *Jeff's "simple" MPD18 use case (JNCv2)*

The MPD18 has 16 Buttons and a slider.

+ [Sound Buttons] Buttons 1-3 are mapped to adsr enveloped sound sources.  
  + By pushing them down sound turns on; releasing: sound off.
+ the Slider sets amplitude (or pitch) for the (sound)source of the currently depressed button.

+ [Memory Slots] Buttons 5-16 represent 'memory' positions (initially not mapped)
  + if sound is assigned (see below), sound is played when button depressed.

+ [Shift Button] Button 4 is a 'shift key'. When depressed
  1. Sound Buttons don't trigger any sound but select the active slot. This can be followed by
  2. depressing a Memory Slot button, which assigns the selected sound to that pad.
  3. if you release the shift key before assignment, nothing happens.
  4. assigning a copy to an already assigned memory slot replaces existing
  5. mute copy
+[Sound Button then Shift button]
  1. Sound Button triggers sound
  2. depress Memory Slot button, assigning the sound to the pad, with sound 

## Variant

+ Several (up to three) sound buttons can be assigned to a memory slot
+ slider informs all sounds assigned to a memory slot


### Further Variation
+ include velocity and aftertouch from pads
+ press shift button then memory slot (w/o pressing sound button) clears memory

+ play sound using 2 to 4 dim fan out w/ aftertouch and slider
 + slider no longer applies to amplitude
 + velocity to amplitude

+ button then shift.
 + shift starts to record control data from aftertouch
 + release shift to stop recording.
 + aftertouch recording (looped) is applied to that source for some param
 + double click shift key then sound source to remove recorded source param