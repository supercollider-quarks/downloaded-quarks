
+ CSVFileReader {
	*readCanRT {| path |
		var raw, data, rows, cols;
		raw = this.read(path, true);
		rows = raw.size - 1;//skip spec
		data = Array.newClear(rows);
		data.size.do({|i|
			data[i] = raw[i+1][1].interpret;//skip spec, name
		});
		^data
	}
}
+ Data {
	readMouse { |alsoWrite=false path|
		realtime = true;
		alsoWrite.if({ storage = List[] });
		{ SendReply.kr(Impulse.kr(this.sampleRate), '/mouse', [MouseX.kr, MouseY.kr(1,0)]) }.play;
		OSCFunc({ |m t|
			m = [t, m[3], m[4]];
			this.data = m;
			alsoWrite.if { storage.add(m) };
		}, '/mouse').add;
	}
	readCAN { |alsoWrite=false path|
		var wait = this.sampleDur;
		realtime = true;
		alsoWrite.if({ storage = List[] });
		Routine({
			loop({
				var tmp = CSVFileReader.readCanRT(path);
				alsoWrite.if { storage.add(tmp) };
				this.data = Pfunc({ tmp });
				wait.wait;
			})
		}).play;
	}
	readKTone { |alsoWrite=false path cmd='/signal'|
		realtime = true;
		alsoWrite.if { storage = List[] };
		OSCFunc({ |m,t,a,r|
			m = m.drop(1);
			this.data = m;
			alsoWrite.if { storage.add(m) };
		}, cmd).add;
	}
	readAudio { |alsoWrite=false path |
		// make templates that use bus
	}
	readArduino { |alsoWrite=false, path port=""|
		var wait = this.sampleDur;
		var arduino = SerialPort(port, crtscts: true);
		realtime = true;
		alsoWrite.if({ storage = List[] });
		Routine({
			var byte, str, val;
			inf.do({ |i|
				if(arduino.read == 10, {
					str = "";
					while({ byte = arduino.read; byte != 13 }, { str = str ++ byte.asAscii });
					val = str.asInt;
					alsoWrite.if({ storage.add(val) });
					this.data = Pfunc({ val });
					wait.wait;
				});
			})
		}).play;
	}
}