
+ Server {

	// shortcut server.record
	rek { |chans=1 filename format="int16" header="WAV"|
		var path;
		if (filename.isNil, { filename = Date.getDate.format("%y-%m-%d_%Hh%mm%Ss") });
		header.switch(
			"AIFF", { path = "~/Desktop/".standardizePath ++ filename ++ ".aif" },
			"WAV", { path = "~/Desktop/".standardizePath ++ filename ++ ".wav" }
		);
		this.recChannels = chans;
		this.recSampleFormat = format;
		this.recHeaderFormat = header;
		this.record(path);
	}
	stoprek { this.stopRecording }

	waitForReboot { arg func;
		if (isLocal.not) { "can't reboot a remote server".inform; ^this };
		if (serverRunning) {
			Routine.run {
				this.quit;
				this.wait(\done);
				0.1.wait;
				this.waitForBoot(func);
			}
		} {
			this.waitForBoot(func);
		}
	}
}
