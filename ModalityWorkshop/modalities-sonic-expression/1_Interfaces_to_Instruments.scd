/* **** INTERFACE ****
In the field of computer science, an interface is a tool and concept that refers to a point of interaction between components, and is applicable at the level of both hardware and software. This allows a component, whether a piece of hardware such as a graphics card or a piece of software such as an Internet browser, to function independently while using interfaces to communicate with other components via an input/output system and an associated protocol.

In addition to hardware and software interfaces, a computing interface may refer to the means of communication between the computer and the user by means of peripheral devices such as a monitor or a keyboard, an interface with the Internet via Internet Protocol, and any other point of communication involving a computer.

Most computer interfaces are bi-directional, but some, such as to a simple mouse or microphone, are uni-directional.
*/

/* **** INSTRUMENT ****
A musical instrument is a device created or adapted for the purpose of making musical sounds. In principle, any object that produces sound can serve as a musical instrument, it is through purpose that the object becomes a musical instrument.
*/


// Different protocols:

// - HID, or Human Input Device
//       examples are joysticks, gamepads, mice, keyboards, ...
// ( - WiiMote... uses a special interface using BlueTooth )

// - MIDI, or Musical Instrument Digital Interface
//       examples are midi keyboards, faderboxes, synthesizers, ...
//       also to other programs (e.g. Ardour, Logic), e.g. via IAC-bus on OSX, or ALSA MIDI patchbay on Linux

// - OSC, or Open Sound Control
//       -- used between programs, e.g. sclang and scsynth,
//          but also Processing - SuperCollider, PureData - SuperCollider, etc
//       -- some hardware interfaces use it (e.g. GluiOn), and various Apps these days use it (TouchOSC, LemurApp, etc).

// - SerialPort (not covered during the workshop, but at request, I can do so)
//       examples are Arduino, DMX, etc...


// Modality currently supports HID, MIDI and OSC!
