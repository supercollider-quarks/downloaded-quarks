CapSerial {
	var <>capAction, <capResp;

	var <>trace;
	var capTareMode = false;

	var <>capMinMax, numCap = 6;
	var <capRawVals, <capNormVals;
	var serialPort;

	*new{|serialPort|
		^super.new.init(serialPort);
	}

	init {|mySerialPort|
		
		serialPort = mySerialPort;
		capMinMax = [0, 16383]!numCap;

		trace = false;

		this.pr_makeResponders;
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
			val.linlin(capMinMax[i][0], capMinMax[i][1], 0, 1);
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

		capResp = Task({
			var char;
			var raw = "";
			var tmpArr;

			inf.do{
				while({
					char= serialPort.next; char.isNil
				}, { 0.01.wait });

				char = char.asAscii;
				(char != 10.asAscii).if({
					raw = raw ++ char;
				},{
					try({
						capRawVals = raw.split($ ).asInteger;
						trace.if{
							"caps:\t%".format(capRawVals).inform;
						};
						capTareMode.not.if({
							capNormVals = this.pr_normalizeCap(capRawVals);
							capAction.value(capNormVals, capRawVals);
						}, {
							this.pr_tareCap(capRawVals)
						});
					}, {"failed to acquire data, raw array is:%".format(raw).warn});

					raw = "";
				});
			}
		}).play;

	}
}