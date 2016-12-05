// **********  Server Config:  **********

	// tweak for fast response and rare complaints:
	// default is 0.2 secs, which feels slow;
	// less than 0.03 may introduce many late messages from server.
	// latency nil means do things immediately, but will compromise timing:
	// it quantizes to the hardwareBufferSize border.
// m.server.latency = 0.05;
m.server.latency = nil;	// make the server respond to controls immediately;

// // hardware setup first
m.server.options
// .device_("Fireface 400 (297)")	// device name
 .numInputBusChannels_(12)		//
 .numOutputBusChannels_(8)		//
// .blockSize_(64)					//  .kr bus rate, default is 64 samples
// .hardwareBufferSize_(128)		// take fastest power of two hardware allows
 .numAudioBusChannels_(1024)		// for proxy usage: lots of ar bus space
 .memSize_(8192 * 64)			// 512 MB memory for buffers, soundfiles!
 .maxNodes_(1024 * 16)			// lots of synth nodes
 .numWireBufs_(64*8)				// fat synthdefs need lots of interconnects
;

// recording options:
m.server
 .recChannels_(m.config.numChans ? 2)			// record numChans
 .recSampleFormat_("float") // in float to recover from clipping
;


if (m.server.serverRunning) {
	m.warnWin(
		"Setting Server.options while server running!\n"
		"Settings will not take effect until server is rebooted:\n"
		"m.server.reboot;",
		"Server options!"
	);
};


m.postServerOptions;

nil; // return value
