// relevant helpfiles:
// relevant helpfiles:
"Overviews/Modality".openHelpFile;
MKtl.openHelpFile;

// finding devices is as simple as:
MKtl.find;

// or for just one protocol:
MKtl.find( 'hid' ); // needs 3.7dev
MKtl.find( 'midi' );

// this will print out a list of devices, pick the one you want:

/*
// Available MIDIMKtls:
// MKtl('myNickName', 'lookupName');
		// [ midi device, portname, uid]

MKtl('midi_0_iac_driver', 'midi_0_iac_driver');
		// [ "IAC Driver", "IAC Bus 1", -476939297 ]
		// no matching desc files found!

// on linux, the system creates ports for various things already,
// such as the SuperCollider app, other  apps using midi,
// midi through ports, and system ports.

-----------------------------------------------------

// Available HIDMKtlDevices:
// MKtl('myNickName', 'lookupName');
		// [ product, vendor, (serial#) ]

MKtl('hid_0_run_n_', 'hid_0_run_n_drive');
		// [ "Run'N' Drive", "Thrustmaster" ]
		// create from desc file:
MKtl('hid_0_run_n_', "thrustmaster-run-n-drive");

*/


// e.g. we want to open the iCON iControls:
// change name to shortname, use description file name as posted:
MKtl( 'icon', "icon-icontrols" );

// if that controller is not connected, MKtl will create a virtual MKtl,
// and one can create a gui to replace it:
MKtl( 'icon' ).gui;


// what can it do? see the device description for details
MKtl( 'icon' ).desc.openFile

// or look at the names of elements in a hierarchical list:
MKtl( 'icon' ).postElements

// tell it to post a trace when it does something:
MKtl( 'icon' ).trace;
// now do something ... and see the trace posts

// turn trace off again:
MKtl( 'icon' ).trace( false );

// this printing only works if your device is known already by the toolkit. If not, you can create your own description file:
"Tutorials/How_to_create_a_description_file".openHelpFile;

"MIDIExplorer".openHelpFile;

// One can explore the device by posting information on the data coming from it:
MKtl( 'icon' ).explore( true );
// wiggle every control element and then:

// when done, stop exploring, the descr
MKtl( 'icon' ).explore( false );
// open the desc file,
MIDIExplorer.openDoc;
// edit for clarity and hierarchy, then save it here:
MKtlDesc.defaultFolder.openOS;

// --- does anyone have a device that is not yet known by the toolkit?
// - pick one to give an example how it works for everyone, preferably one of each type (MIDI / HID)
// - then help those who have other devices that do not work out of the box

