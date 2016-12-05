m.helpers[\amplitudeTracker] = Ndef(\amplitudeTracker);

Ndef(\amplitudeTracker, {
	Amplitude.ar(SoundIn.ar(0));
});

Ndef(\amplitudeTracker); // return value