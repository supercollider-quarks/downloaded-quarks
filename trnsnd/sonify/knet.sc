
// Knet {
// 	// http://www.kyoshin.bosai.go.jp/kyoshin/
// 	var strDataArray, <numData, <files;
//
// 	*new { |path|
// 		^super.new.init(path);
// 	}
// 	init { |path|
// 		path = PathName(path?"/Users/yota/Documents/publication/2012/日本音響学会/311M9/20110311144600.knt/");
// 		files = path.files;
// 		numData = files.size;
// 		strDataArray = Array.fill(numData, { |i|
// 			File.use(files[i].fullPath, "r", { |f| f.readAllString });
// 		});
// 	}
// 	getDataAsSignalAt { |index|
// 		var offset, regex, sig;
// 		offset = strDataArray[index].find("Memo.");
// 		regex = strDataArray[index].findRegexp("-?[[:digit:]]+[[:blank:]]", offset: offset);
// 		sig = Signal.newClear(regex.size);
// 		regex.do({|item,i|
// 			sig[i] = item[1].interpret;
// 		});
// 		^sig
// 	}
// 	getLatiAt { |index|
// 		^strDataArray[index].findRegexp(
// 			"[[:digit:]]+.?[[:digit:]]+$",
// 			offset: strDataArray[index].find("Station Lat.")
// 		)[0][1].interpret;
// 	}
// 	getLongAt {|index|
// 		^strDataArray[index].findRegexp(
// 			"[[:digit:]]+.?[[:digit:]]+$",
// 			offset: strDataArray[index].find("Station Lat.")
// 		)[1][1].interpret;
// 	}
// 	getHeightAt {|index|
// 		^strDataArray[index].findRegexp(
// 			"-?[[:digit:]]+$",
// 			offset: strDataArray[index].find("Station Height")
// 		)[0][1].interpret;
// 	}
// 	getTimeAt {|index|
// 		^strDataArray[index].findRegexp(
// 			"[0-9][0-9]:[0-9][0-9]:[0-9][0-9]$",
// 			offset: strDataArray[index].find("Record Time")
// 		)[0][1].interpret;
// 	}
//
// }

Knet {
	classvar <path, march11th, <files;
	classvar strDataArray, <numData, <sampleRate, s;

	*initMarch11th { |pathToData|
		path = PathName(pathToData?"/Users/yota/Documents/publication/2012/日本音響学会/311M9/20110311144600.knt/");
		files = path.files;
		march11th = "1103111446.";
		numData = files.size;
		sampleRate = 100;
		s = Server.default;
	}
	*openDataFolder {
		("open " ++ path.fullPath).unixCmd;
	}
}
KnetPoint : Knet {
	var dataPath, strData, <data, <spec, <buf;

	*new { |stationCode="AIC001" direction="EW"|
		^super.new.init(stationCode, direction);
	}
	init { |stationCode direction|
		var tmp;
		dataPath = path +/+ PathName(stationCode ++ march11th ++ direction);
		tmp = FileReader.readInterpret(dataPath.fullPath, true, true, startRow: 17);
		tmp = tmp.flat;
		data = List[];
		tmp.do({ |item| if(item.notNil, { data.add(item) }) });
		spec = [data.minItem, data.maxItem].asSpec;
		strData = File.use(dataPath.fullPath, "r", { |f| f.readAllString });
	}
	time {
		^(strData.findRegexp(
			"[0-9][0-9]:[0-9][0-9]:[0-9][0-9]$",
			offset: strData.find("Record Time")
		)[0][1] ++ ":00").asSecs;
	}
	signal {
		var offset, regex, sig;
		offset = strData.find("Memo.");
		regex = strData.findRegexp("-?[[:digit:]]+[[:blank:]]", offset: offset);
		sig = Signal.newClear(regex.size);
		regex.do({ |item i|
			sig[i] = item[1].interpret;
		});
		^sig
	}
	lati {
		^strData.findRegexp(
			"[[:digit:]]+.?[[:digit:]]+$",
			offset: strData.find("Station Lat.")
		)[0][1].interpret;
	}
	long {
		^strData.findRegexp(
			"[[:digit:]]+.?[[:digit:]]+$",
			offset: strData.find("Station Lat.")
		)[1][1].interpret;
	}
	height {
		^strData.findRegexp(
			"-?[[:digit:]]+$",
			offset: strData.find("Station Height")
		)[0][1].interpret;
	}
	plot {
		Array.newFrom(data).plot
	}
	bufRate {
		if(s.serverRunning.not, {
			Error("Server not running.").throw
		}, {
			^(sampleRate/s.sampleRate)
		});
	}
	load2buf {
		if(s.serverRunning.not, {
			Error("Server not running.").throw
		}, {
			^Buffer.loadCollection(s, \bipolar.asSpec.map(spec.unmap(data)));
		});
	}
	// dsp
	leakDC {
		var prev = data[0], next, y = 0;
		data.size.do({| i |
			next = data.clipAt(i+1);
			y = next - prev + (0.995 * y);
			prev = next;
			data[i] = y;
		});
	}
}