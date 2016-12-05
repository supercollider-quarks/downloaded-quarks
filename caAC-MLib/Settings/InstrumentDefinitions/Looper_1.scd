// SynthDef Definition


(
SynthDef(\Looper1,

    { | start = 0, end = 1, wait = 0, sndbuf = 0, startJitterAmount = 0, pitch = 0, startJitterFreq = 0, amp = 0.1, distort = 0, t_trig = 1, out = 0,
        gate = 1|

        var iBufFrames, iChunkFrames, dur, iBufPos, env, iTrigger;

        iBufFrames   = BufFrames.kr(sndbuf);

        #start, end  = [start, end] * SinOsc.ar(startJitterFreq).range(1-startJitterAmount,1+startJitterAmount);

        start        = start * iBufFrames;
        end          = end   * iBufFrames;

        iChunkFrames = abs(end - start) * midiratio(pitch);
        dur          = iChunkFrames / SampleRate.ir() * midiratio(pitch.neg);
        end          = iChunkFrames + start;

        iBufPos      = DemandEnvGen.ar(Dseq([start, Dstutter(2, Dseq([end]))], inf), Dseq([dur, wait, 0], inf));

        iTrigger     = HPZ1.ar(iBufPos).abs > 1;

        env = EnvGen.ar(Env([0, 1, 1, 0], [0.02, Latch.ar(dur, iTrigger) - 0.04, 0.02], -8), iTrigger);

        Out.ar(out,
            Pan2.ar(
                HPF.ar((BufRd.ar(1, sndbuf, iBufPos % iBufFrames, loop: 1) * (2**distort)).softclip * env,50), TRand.ar(-1,1,iTrigger),
                EnvGen.kr(Env.asr(0.03, 1, 0.03, 4), gate, levelScale: amp, doneAction: 2)
            )
        );

    }

).store
)

Buffer.loadCollection(s, { 1.0.rand2 } ! 44100);


// Bewerk SynthDef metadata
(
m = SynthDesc.readDef(\Looper1); if (m.metadata.isNil) { m.metadata = (); };

m.metadata[\specs] = (
    start: ControlSpec(0,1.0),
    end: ControlSpec(0,1.0),
    wait: ControlSpec(0, 2, 5),
    pitch: ControlSpec(-24,24.0),
    startJitterFreq: ControlSpec(0.1, 10, 'exp'),
    startJitterAmount: ControlSpec(0,1),
    amp: ControlSpec(0, 1.0),
    distort: ControlSpec(0, 4.0)
);
// Ignore key list
m.metadata[\noGui] = [];

// Store processed SynthDef metadata
SynthDesc.storeMetaData(\Looper1, m.metadata);
);

// Test SynthDef
EmbedGui(\Looper1).makeGui;


w = Window.new;

a = SBufControl(\sndbuf, ControlSpec(0,10))
a.gui(w, Rect(0, 50, 400, 30));

w.front





