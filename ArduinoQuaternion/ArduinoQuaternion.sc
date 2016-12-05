/*
Michael McCrea : University of Washington : DXARTS : Jan 2015
mtm5@uw.edu

Methods in ArduinoQuaternionParser and Quaternion extension
ported from Fabio Varesano's FreeIMU_cube Processing example.
http://www.varesano.net/

Dependencies: MathLib Quark (Quaternion), Arduino Quark
*/

ArduinoQuaternion : Arduino
{
	// TODO: write euler coords to bus
	// var <posBus, <writePos = false, <posPlotter;

	*parserClass { ^ArduinoQuaternionParser }

	send { | aString |
		var str;
		str = aString ++ "\n";
		port.putAll(str);
	}

	free { this.close }

	setHome { parser.setHome }
	clearHome { parser.clearHome }
}




ArduinoQuaternionParser : ArduinoParser
{
	var asciiInputLine, <charState, <settingHome = false, homeQ = nil, <>postYPR=false, <>postRaw=false;

	init { }

	/* called from a Routine (inputThread) in Arduino's init  */
	parse {
		asciiInputLine = List(); // hold each line
		charState = nil;

		// start the loop that reads the SerialPort
		loop { this.parseByte(port.read) };
	}

	parseByte { | byte |
		if (byte === 13) {
			// wait for LF
			charState = 13;
		} {
			if (byte === 10) {
				if (charState === 13) {
					// CR/LF encountered, wrap up this line

					if (asciiInputLine.notEmpty) {
						// postf("asciiInputLine: %\n", asciiInputLine); // debug
						this.dispatch( asciiInputLine );
					};

					// clear the line stream
					asciiInputLine.clear;
					charState = nil;
				}
			} {
				asciiInputLine.add( byte );
			}
		}
	}

	dispatch { |asciiLine|
		var localCopy, split;

		// must copy line so it isn't lost in the fork below once
		// asciiInputLine.clear in parseByte()
		localCopy = List.copyInstance(asciiLine);

		// split = asciiLine.asAscii.split($,);
		split = asciiLine.collect({|x| {x.asInteger.asAscii}.try ? "" }).join.split($,);
		postRaw.if{ split.postln };

		if( split.size >= 5 ){
			block{ |break|
				var hexQuat, q, eulerRad;
				hexQuat = split.copyRange(0, 3);

				q = Quaternion( *hexQuat.collect{ |hex|
					if( hex.size == 8 ) {
						this.strToFloat( hex );
					} { break.("hexQuat is the wrong size".error) }
					}
				);

				if( settingHome ) { homeQ = q.conjugate; settingHome = false; };

				eulerRad =  if( homeQ.notNil, { homeQ * q }, { q } ).asEuler;

				arduino.prDispatchMessage( eulerRad );
				postYPR.if{ this.prPostYPR( eulerRad ) };
			}
		};
	}

	strToFloat { |floatString = "130C633F"|
		var strToBits;
		strToBits = floatString.clump(2).collect({ |hex| ("0x" ++ hex.asString).asFloat.asInteger });
		^Float.from32Bits(
			(strToBits[3] << 24) | ((strToBits[2] & 0xff) << 16) | ((strToBits[1] & 0xff) << 8) | (strToBits[0] & 0xff)
		);
	}

	setHome { settingHome = true }
	clearHome { homeQ = nil }

	prPostYPR { |euler| postf("YAW: %\tPITCH: %\tROLL %\n", *euler.raddeg) }

}

+ Quaternion {

	// From Varesano: See Sebastian O.H. Madwick report
	// "An efficient orientation filter for inertial and intertial/magnetic
	// sensor arrays" Chapter 2 Quaternion representation

	asEuler {
		var q, euler = Array.newClear(3);
		q = this.coordinates;

		euler[0] = atan2( (2 * q[1] * q[2]) - (2 * q[0] * q[3]), (2 * q[0]*q[0]) + (2 * q[1] * q[1]) - 1); // psi
		euler[1] = asin( (2 * q[1] * q[3]) + (2 * q[0] * q[2])).neg; // theta
		euler[2] = atan2( (2 * q[2] * q[3]) - (2 * q[0] * q[1]), (2 * q[0] * q[0]) + (2 * q[3] * q[3]) - 1); // phi

		^euler
	}
}



/* -- usage --
SerialPort.devices

// a = ArduinoQuaternion( "/dev/tty.AdafruitEZ-Link3e2f-SPP", 38400)
a = ArduinoQuaternion( "/dev/tty.AdafruitEZ-Link416c-SPP", 38400)

// set the action to be performed with every new
// reading of the sensor. yaw, pitch, and roll are
// passed into your action function
a.action = { |y,p,r| [ y,p,r ].raddeg.postln};

a.setHome
a.clearHome

// post yaw pitch and roll from within parser for debugging
a.parser.postYPR = true
a.parser.postYPR = false
a.parser.postRaw = true
a.parser.postRaw = false

a.close

SerialPort.closeAll
*/

/* -- headtracker demo --
SerialPort.listDevices

// a = ArduinoQuaternion( "/dev/tty.AdafruitEZ-Link3e2f-SPP", 38400)
a = ArduinoQuaternion( "/dev/tty.AdafruitEZ-Link416c-SPP", 38400)

// set the action to be performed with every new
// reading of the sensor. yaw, pitch, and roll are
// passed into your action function
a.action = { |y,p,r|
	[ y,p,r ].raddeg.postln
};

a.setHome
a.clearHome

// post yaw pitch and roll from within parser for debugging
a.parser.postYPR = true
a.parser.postYPR = false

a.close

SerialPort.closeAll

~bfBus = CtkAudio.play(4)
~bfBus.bus
~group = CtkGroup.play(addAction: \tail);
(
d = CtkSynthDef(\xform, {arg outbus = 0, inbus, rotate=0, tilt=0, tumble=0, amp = 1;
	var bf, rtt;
	bf = In.ar(inbus, 4);
	// rtt = [rotate, tilt, tumble] * pi/180;
	// bf = FoaRTT.ar(in, *rtt);
	bf = FoaRTT.ar(bf, rotate, tilt, tumble);
	Out.ar(outbus, bf * amp);
})
)

~transformer = d.note(addAction: \head, target: ~group).inbus_(60).outbus_(~bfBus).play
~transformer.free

(
o = OSCdef(\rtt, { |msg, time, addr, recvPort|
	var ypr;
	// msg.postln;
	ypr = msg[1..3]; //.postln;
	~transformer.rotate_(ypr[0]).tilt_(ypr[2]).tumble_(ypr[1].neg)
	// ~transformer.rotate_(ypr[0]).tilt_(0).tumble_(0) // rotate!
	// ~transformer.rotate_(0).tilt_(0).tumble_(ypr[1].neg) // tumble!
	// ~transformer.rotate_(0).tilt_(ypr[2]).tumble_(0) // tilt!
}, '/ypr');
)
o.free

SerialPort.closeAll


~decoder = FoaDecoderKernel.newCIPIC;
~decoder = FoaDecoderKernel.newUHJ;
~decoder = FoaDecoderKernel.newSpherical;
~decoder = FoaDecoderMatrix.newStereo;
(
~decDef = CtkSynthDef(\decoder, {arg outbus = 0, inbus, rotate=0, tilt=0, tumble=0, amp = 1;
	var bf, ster;
	bf = In.ar(inbus, 4);
	ster = FoaDecode.ar(bf, ~decoder);
	Out.ar(outbus, Limiter.ar(ster * amp));
})
)

~dec = ~decDef.note(addAction: \tail, target: ~group).inbus_(~bfBus).outbus_(0).play
~dec.free
~group.freeAll
FoaAudition()

s.scope(4, ~bfBus.bus)

// note the first virtual bus:
s.options.numInputBusChannels+s.options.numOutputBusChannels

~transformer.free
*/


/* -- scratch --
~quaternionToEuler = { |q|
		var euler = Array.newClear(3);

		euler[0] = atan2( (2 * q[1] * q[2]) - (2 * q[0] * q[3]), (2 * q[0]*q[0]) + (2 * q[1] * q[1]) - 1); // psi
		euler[1] = asin( (2 * q[1] * q[3]) + (2 * q[0] * q[2])).neg; // theta
		euler[2] = atan2( (2 * q[2] * q[3]) - (2 * q[0] * q[1]), (2 * q[0] * q[0]) + (2 * q[3] * q[3]) - 1); // phi

		euler.raddeg
	}

~quaternionToEuler.([ 0.433523863554, 0.06194182485342, 0.0072955079376698, 0.89710456132889 ])
~quaternionToEuler.([ -0.2, 0.3, 0.7, 0.8 ])


	(
	~decodeF = { |floatString = "130C633F"|
	var strToBits;
	strToBits = floatString.clump(2).collect({ |hex| ("0x" ++ hex.asString).asFloat.asInteger });
	// inData.postln;
	^Float.from32Bits(
	(inData[3] << 24) | ((inData[2] & 0xff) << 16) | ((inData[1] & 0xff) << 8) | (inData[0] & 0xff)
	);
	};
	)

	a = ~decodeF.()
*/