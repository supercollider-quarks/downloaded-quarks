PushPullOSC {
	var <netAddr;

	var capResp, encoderResp, batteryResp, pingResp, imuResp;
	var <>capAction, <>encoderAction, <>batteryWarnAction, <>pingAction, <>imuAction;
	var <>capUri = "/inputs/serial/1";
	var <>encoderUri = "/inputs/serial/2";
	var <>batteryUri = "/battery";
	var <>pingUri = "/ping";
	var <>imuUri = "/imu";

	var <trace;
	var capTareMode = false;

	var <>capMinMax, numCap = 6;
	var <capRawVals, <capNormVals, <capTrigs;
	var <>capMinThresh = 0.01, capTrigThresh = 0.1;

	var <encoderDict;

	var <imuDict;
	var <direction;

	var voltage = 0;
	var <>batteryWarnThresh = 3.0; // voltage at which to warn user of low battery

	*new{|netAddr|
		^super.new.init(netAddr);
	}

	init {|myNetAddr|

		netAddr = myNetAddr;
		capMinMax = [0, 16383]!numCap;
		encoderDict = IdentityDictionary.newFrom([\a, 0, \b, 0, \c, 0, \d, 0, \e, 0, \f, 0]);
		imuDict = IdentityDictionary.newFrom([\g, [0,0,0], \a, [0, 0, 0], \m, [0, 0, 0], \t, 0]);
		direction = [0, 0, 0]; // ground oriantation

		trace = ();
		#[\cap, \encoder, \imu, \battery, \ping].do{|key|
			trace[key] = false;
		};

		this.pr_makeResponders;
	}
	tareMag {
		direction = this.imuDict[\mag];
	}
	tareCap {|isOn|
		isOn.if({
			capMinMax = {[inf, -inf]}!numCap;
			capTareMode = true;
			"CapSense tare mode. Actions disabled.".inform;
		}, {
			capTareMode = false;
			"Set capMinMax to\n\t%;\n".postf(capMinMax.asCompileString);
		});
	}
	pr_normalizeCap {|arr|
		^arr.collect { |val, i|
			val.linlin(capMinMax[i][0], capMinMax[i][1], 0, 1).linlin(capMinThresh, 1, 0, 1);
		};
	}
	pr_tareCap {|arr|
		var min, max;

		arr.do{|val, i|
			#min, max = capMinMax[i];

			(val < min).if{
				capMinMax[i][0] = val;
				"newMin(%) = %".format(i, val).inform;
			};
			(val > max).if{
				capMinMax[i][1] = val;
				"newMax(%) = %".format(i, val).inform;
			};
		};
	}

	pr_makeResponders {
		capResp = OSCFunc(
			{|msg|
				var arr = msg[1].collect(_.asAscii).join;

				try({
					capRawVals = arr.split($ ).asInteger;
					trace[\cap].if{
						"caps:\t%".format(capRawVals).inform;
					};
					capTareMode.not.if({
						capNormVals = this.pr_normalizeCap(capRawVals);
						capTrigs = (capNormVals > capTrigThresh).asInteger;
						// cap action
						capAction.value(capNormVals,capTrigs, capRawVals);
					}, {
						this.pr_tareCap(capRawVals)
					});
				}, {
					"PushPullOSC(cap): failed to acquire data ( % )".format(arr.asCompileString).warn;
				});

			},
			capUri,
			netAddr
		);

		encoderResp = OSCFunc(
			{|msg|
				var raw = msg[1].collect(_.asAscii).join;
				var key, val;
				try({
					#key, val = raw.split($ );
					key = key.asSymbol;
					val = val.asInteger;

					trace[\encoder].if{
						"encoder(%):\t%".format(key, val).inform;
					};
					switch (key)
						{\a} {encoderDict[key] = val}
						{\b} {encoderDict[key] = val}
						{\c} {encoderDict[key] = val-1}
						{\d} {encoderDict[key] = val-1}
						{\e} {encoderDict[key] = val-1}
						{\f} {encoderDict[key] = val-1};

					encoderAction.value(key, encoderDict[key], encoderDict);
				}, {
					"PushPullOSC(encoder): failed to acquire data ( % )".format(raw.asCompileString).warn;
				})
			},
			encoderUri,
			netAddr
		);
		imuResp = OSCFunc(
			{|msg|
				trace[\imu].if{
					"imu:\t%".format(msg[1..]).inform;
				};

				imuDict[\gyro] = msg[1..3];
				imuDict[\acc] = msg[4..6];
				imuDict[\mag] = msg[7..9];
				imuDict[\normedMag] = ((msg[7..9] - direction) % 2pi) - pi;
				imuDict[\temp] = msg[10];

				imuAction.value(imuDict);
			},
			imuUri,
			netAddr
		);
		batteryResp = OSCFunc(
			{|msg|
				voltage = msg[1];
				trace[\battery].if{
					"battery:\t% V".format(voltage.asStringPrec(3)).inform;
				};

				(voltage <= batteryWarnThresh).if{
					"PushPull: low voltage (% V)".format().warn;
					batteryWarnAction.value(voltage);
				}
			},
			batteryUri,
			netAddr
		);
		pingResp = OSCFunc(
			{|msg|
				pingAction.value(this);

				trace[\ping].if{
					"ping:\n\tIP: %\n\tMAC: %".format(*msg[1..]).inform;
				}

			},
			pingUri,
			netAddr
		);
	}

	setLight {|color|
		color = color.bubble.flat;
		color = color.wrapExtend(8).collect{|c| c.asArray.keep(3) * 255}.flat.as(Int8Array);
		try {netAddr.sendMsg("/outputs/rgb/1", color);}
	}
}

PushPullOSC_SE : PushPullOSC {
	var <>isLeft = false;

	pr_makeResponders {
		capResp = OSCFunc(
			{|msg|
				var arr = msg[1].collect(_.asAscii).join;

				try({
					capRawVals = arr.split($ ).asInteger;

					// INDEXING
					isLeft.if({
						capRawVals = capRawVals[#[1, 2, 3, 0, 4, 5]];
					}, {
						capRawVals = capRawVals[#[0, 3, 2, 1, 4, 5]];
					});



					trace[\cap].if{
						"caps:\t%".format(capRawVals).inform;
					};
					capTareMode.not.if({
						capNormVals = this.pr_normalizeCap(capRawVals);
						capTrigs = (capNormVals > capTrigThresh).asInteger;
						// cap action
						capAction.value(capNormVals,capTrigs, capRawVals);
					}, {
						this.pr_tareCap(capRawVals)
					});
				}, {
					"PushPullOSC(cap): failed to acquire data ( % )".format(arr.asCompileString).warn;
				});

			},
			capUri,
			netAddr
		);

		encoderResp = OSCFunc(
			{|msg|
				var raw = msg[1].collect(_.asAscii).join;
				var key, val;
				try({
					#key, val = raw.split($ );
					key = key.asSymbol;
					val = val.asInteger;

					trace[\encoder].if{
						"encoder(%):\t%".format(key, val).inform;
					};
					switch (key)
						{\a} {encoderDict[key] = val}
						{\b} {encoderDict[key] = val}
						{\c} {encoderDict[key] = val-1}
						{\d} {encoderDict[key] = val-1}
						{\e} {encoderDict[key] = val-1}
						{\f} {encoderDict[key] = val-1};

					encoderAction.value(key, encoderDict[key], encoderDict);
				}, {
					"PushPullOSC(encoder): failed to acquire data ( % )".format(raw.asCompileString).warn;
				})
			},
			encoderUri,
			netAddr
		);
		imuResp = OSCFunc(
			{|msg|
				trace[\imu].if{
					"imu:\t%".format(msg[1..]).inform;
				};

				imuDict[\gyro] = msg[1..3];
				imuDict[\acc] = msg[4..6];
				imuDict[\mag] = msg[7..9];
				imuDict[\normedMag] = ((msg[7..9] - direction) % 2pi) - pi;
				imuDict[\temp] = msg[10];

				imuAction.value(imuDict);
			},
			imuUri,
			netAddr
		);
		batteryResp = OSCFunc(
			{|msg|
				voltage = msg[1];
				trace[\battery].if{
					"battery:\t% V".format(voltage.asStringPrec(3)).inform;
				};

				(voltage <= batteryWarnThresh).if{
					"PushPull: low voltage (% V)".format().warn;
					batteryWarnAction.value(voltage);
				}
			},
			batteryUri,
			netAddr
		);
		pingResp = OSCFunc(
			{|msg|
				trace[\ping].if{
					"ping:\n\tIP: %\n\tMAC: %".format(*msg[1..]).inform;
				}

			},
			pingUri,
			netAddr
		);
	}
}