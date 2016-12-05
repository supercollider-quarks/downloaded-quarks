/*
Example made by Tijs Ham
In this patch one knob is used to control sound
The speed of the movement determines the mapping of the control data
Use the most left knob on the nanoKontrol2 to play around with the sounds
*/
s.waitForBoot{
	{
		// find devices
		MKtl.find;
		// open the nanoKontrol2
		k = MKtl('nk2', "*trol2");
		// reset all the actions
		k.resetActions;
		// syncing the server
		s.sync;
		// using an Ndef to prepare a sound
		Ndef(\tik, {|freq=8, peak=4000, rel=0.2, att=0.01, cut=2000, rq=0.9, dt=0.1, fb=1|
			CombL.ar(BLowPass.ar(Saw.ar(peak.lag(0.01),
				EnvGen.ar(Env.perc(att, rel, 0.5, -8), Impulse.kr(freq.lag(0.1)))
			),
			cut.lag(0.01), rq.lag(0.01)),0.2, [dt,dt-0.001].lag(0.01), fb).tanh
		});
		// play the Ndef
		Ndef(\tik).play;
		// syncing the server
		s.sync;
		// setting global variables
		~lastTime = Process.elapsedTime;
		~deltaTime = 100;
		// assigning actions for the knob
		k.elementAt( \kn, 0 ).action = {|el|
			// determining the speed of movement of the knob
			~deltaTime = Process.elapsedTime - ~lastTime;
			// different speeds result in different mappings
			case(
				{~deltaTime > 1},{},
				{~deltaTime > 0.01 },{
					Ndef(\tik).set(\freq, el.value.linlin(0, 1.0, 0.2, 12));
				},
				{~deltaTime > 0.007 },{
					Ndef(\tik).set(\cut, el.value.linlin(0, 1.0, 200, 12000));
				},
				{~deltaTime > 0.005 },{
					Ndef(\tik).set(\rq, el.value.linlin(0, 1.0, 0.2, 0.9));
				},
				{~deltaTime > 0.004 },{
					Ndef(\tik).set(\dt, el.value.linlin(0, 1.0, 0.002, 0.2));
				},
				{~deltaTime > 0.003 },{
					Ndef(\tik).set(\fb, el.value.linlin(0, 1.0, 0.2, 9));
				},
				{~deltaTime > 0.002 },{
					Ndef(\tik).set(\att, el.value.linlin(0, 1.0, 0.2, 0.002));
				},
				{~deltaTime > 0.001 },{
					Ndef(\tik).set(\rel, el.value.linlin(0, 1.0, 0.002, 0.2));
				},
				{~deltaTime > 0.00001 },{
					Ndef(\tik).set(\peak, el.value.linlin(0, 1.0, 20, 800));
				},

			);
			// update the time of the process
			~lastTime = Process.elapsedTime;
		};
	}.fork;
}
