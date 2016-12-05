CSV {
	var <>data, <>sampleRate;
	//r/w
	read {| path, skip=0 |
		data = CSVFileReader.readInterpret(path, true, true, startRow: skip);
	}
	write {| path |
		var tmp = "";
		var file = File(path, "w");
		data.do({|row|
			if(row.isKindOf(SequenceableCollection), {
				row.do({|val,i|
					tmp = tmp ++ val.asString;
					if(i<(row.size-1), { tmp = tmp ++ "," });
				});
				}, {
					tmp = tmp ++ row.asString;
			});
			tmp = tmp ++ "\n";
		});
		file.write(tmp);
		file.close;
	}
	plot {
		Array.fill(data[0].size, {|i| this.col(i) }).plot;
	}
	info {
		inform("***********************************************");
		inform("data size: " ++ data.size ++ "x" ++ data[0].size);
		inform("***********************************************");
		^""
	}
	duration {
		if(sampleRate.isNil, {
			Error("sampelRate unknown.").throw;
		}, {
			^(data.size / sampleRate);
		});
	}
	row {| index=0 |
		^data[index]
	}
	col {| index=0 |
		^Array.fill(data.size, {|i| data[i][index] })
	}
	//archive
	archive {| path |
		data.writeBinaryArchive(path);
	}
	unarchive {| path |
		data = Object.readBinaryArchive(path);
	}
	//audify
	writeSoundFile {| path, header="WAV", format="int16" |
		var sf;
		sf = SoundFile.new
		.headerFormat_(header)
		.sampleFormat_(format)
		.sampleRate_(sampleRate)
		.numChannels_(data[0].size)
		;
		if(sf.openWrite(path), {
			//must be bipolar normalized FloatArray
			sf.writeData(FloatArray.newFrom(data.flat));
			sf.close;
			}, {
			Error("Could not open.\n").throw;
		});
	}
	//spec
	localSpecs {
		^Array.fill(data[0].size, {|i|
			var col = this.col(i);
			ControlSpec(col.minItem, col.maxItem);
		});
	}
	globalSpec {
		var minItems, maxItems;
		minItems = this.localSpecs.collect({|item| item.minval });
		maxItems = this.localSpecs.collect({|item| item.maxval });
		^ControlSpec(minItems.minItem, maxItems.maxItem);
	}
	//dsp
	normalize1 {| how='across' |
		//unipolar
		how.switch(
			'across', {
				var spec = this.globalSpec;
				data.size.do({|i|
					data[0].size.do({|j|
						data[i][j] = spec.unmap(data[i][j]);
					});
				});
			},
			'within', {
				var spec = this.localSpecs;
				data.size.do({|i|
					data[0].size.do({|j|
						data[i][j] = spec[j].unmap(data[i][j]);
					});
				});
			}
		);
	}
	normalize2 {| how='across' |
		var bipolar = \bipolar.asSpec;
		how.switch(
			'across', {
				var spec = this.globalSpec;
				data.size.do({|i|
					data[0].size.do({|j|
						data[i][j] = bipolar.map(spec.unmap(data[i][j]));
					});
				});
			},
			'within', {
				var spec = this.localSpecs;
				data.size.do({|i|
					data[0].size.do({|j|
						data[i][j] = bipolar.map(spec[j].unmap(data[i][j]));
					});
				});
			}
		);
	}
	leakDC {| index=0 |
		var prev = data[0][index], next, y = 0;
		data.do({| row, i |
			next = row.clipAt(i+1);
			y = next - prev + (0.995 * y);
			prev = next;
			data[i][index] = y;
		});
	}
	hpz1 {| index=0 |
	}
	lpz1 {| index=0 |
	}
	lpf {| index=0, freq |
		var a0, b1, b2;
		var y0, y1, y2;
		var pfreq = freq * (2pi / sampleRate) * 0.5;
		var c = 1 / tan(pfreq);
		var c2 = c.squared;
		var sqrt2c = c * 2.sqrt;
		a0 = b1 = b2 = y0 = y1 = y2 = 0;
		a0 = 1 / (1 + sqrt2c + c2);
		b1 = -2 * (1 - c2) * a0;
		b2 = neg(1 - sqrt2c + c2) * a0;
		data.do({| row |
			y0 = row[index] + (b1 * y1) + (b2 * y2);
			row[index] = a0 * (y0 + (2 * y1) + y2);
			y2 = y1;
			y1 = y0;
		});
	}
	hpf {| index=0, freq |
		var a0, b1, b2;
		var y0, y1, y2;
		var pfreq = freq * (2pi / sampleRate) * 0.5;
		var c = 1 / tan(pfreq);
		var c2 = c.squared;
		var sqrt2c = c * 2.sqrt;
		a0 = b1 = b2 = y0 = y1 = y2 = 0;
		a0 = 1 / (1 + sqrt2c + c2);
		b1 = 2 * (1 - c2) * a0;
		b2 = neg(1 - sqrt2c + c2) * a0;
		data.do({| row |
			y0 = row[index] + (b1 * y1) + (b2 * y2);
			row[index] = a0 * (y0 - (2 * y1) + y2);
			y2 = y1;
			y1 = y0;
		});
	}
}

CSV0 {
	var <header, <data, <pathName;
	var <localSpecs, <globalSpec, <>sampleRate;

	*new {| path, sampleRate=44100, hasHeader=false |
		^super.new.init(path, sampleRate, hasHeader)
	}
	init {| path, sampleRate, hasHeader |
		var file, rows, cols;
		pathName = PathName(path);
		file = CSVFileReader.read(pathName.fullPath, true, true);
		if(hasHeader, {
			header = file[0];
			rows = file.size - 1;//skip header
			cols = header.size;
			data = Array2D(rows, cols);
			cols.do({|i|
				rows.do({|j|
					data[j,i] = file[j+1][i].interpret;
				})
			});
			// data = Array.fill2D(rows, cols, { file[j+1][i].interpret });
			}, {
				header = nil;
				rows = file.size;
				cols = file[0].size;
				data = Array2D(rows, cols);
				cols.do({|i|
					rows.do({|j|
						data[j,i] = file[j][i].interpret;
					})
				});
				// data = Array.fill2D(rows, cols, { file[j][i].interpret });
		});
		this.makeLocalSpecs;
		this.makeGlobalSpec;
		this.sampleRate = sampleRate;
	}

	//specs
	makeLocalSpecs {
		localSpecs = Array.newClear(this.cols);
		this.cols.do({|i|
			//crude nil checker
			if(this.colAt(i).isNil, {
				localSpecs[i] = nil;
				}, {
				localSpecs[i] = [this.colAt(i).minItem, this.colAt(i).maxItem].asSpec;
			});
		});
	}
	makeGlobalSpec {
		var minItems, maxItems;
		minItems = localSpecs.collect({|item| item.minval });
		maxItems = localSpecs.collect({|item| item.maxval });
		globalSpec = [minItems.minItem, maxItems.maxItem].asSpec;
	}

	// *loadDialog {| hasHeader=false |
	// 	Dialog.openPanel({|path|
	// 		^super.new.init(path, hasHeader)
	// 	})
	// }
	info {
		if(header.isNil.not, {
			inform("***********************************************");
			inform("data size: " ++ data.cols ++ "x" ++ data.rows);
			header.do({| item, i |
				Post <<< i << ": " <<< item << Char.nl;
				Post << "range: " <<< [localSpecs[i].minval, localSpecs[i].maxval] << Char.nl << Char.nl;
			});
			inform("***********************************************");
			}, {
			inform("***********************************************");
			inform("data size: " ++ data.cols ++ "x" ++ data.rows);
			inform("duration: " ++ this.duration ++ " seconds");
			inform("***********************************************");
		});
		^""
	}
	plot {
		var arrayOfArrays = Array.newClear(this.cols);
		this.colsDo({|item,i| arrayOfArrays[i] = item });
		arrayOfArrays.plot;
	}
	duration { ^(data.rows / sampleRate) }

	//interface
	at {| row=0, col=0 |
		^data.at(row, col);
	}
	colAt {| index=0 |
		^data.colAt(index);
	}
	rowAt {| index=0 |
		^data.rowAt(index);
	}
	cols { ^data.cols }
	rows { ^data.rows }
	colsDo {|func| ^data.colsDo(func) }
	rowsDo {|func| ^data.rowsDo(func) }

	//data modification
	put {| row, col, val |
		data.put(row, col, val);
	}
	leakDC {| index=0 |
		var tmp = this.colAt(index);
		var prev = tmp[0], next, y = 0;
		tmp.size.do({|i|
			next = tmp.clipAt(i+1);
			y = next - prev + (0.995 * y);
			prev = next;
			data.put(i, index, y);
		});
		this.rebuildSpecs;
	}
	rebuildSpecs {
		this.makeLocalSpecs;
		this.makeGlobalSpec;
	}
	normalizeAcross {
		data.array.do({|item,i| data.array[i] = globalSpec.unmap(item) });
	}
	normalizeWithin {
		data.array.do({|item,i| data.array[i] = localSpecs[(i%(localSpecs.size)).round].unmap(item) });
	}
	normalizeAcrossBipolar {
		var divBy, dataAsRawArray;
		divBy = this.globalSpec.minval.abs.max(this.globalSpec.maxval);
		dataAsRawArray = FloatArray.newClear(data.array.size);
		data.array.do({|item,i| dataAsRawArray[i] = item / divBy });
		^dataAsRawArray;
	}
	normalizeWithinBipolar {
		var divBy, dataAsRawArray;
		divBy = FloatArray.newClear(this.localSpecs.size);
		this.localSpecs.do({|item,i| divBy[i] = item.minval.abs.max(this.globalSpec.maxval) });
		dataAsRawArray = FloatArray.newClear(data.array.size);
		data.array.do({|item,i| dataAsRawArray[i] = item / divBy[(i%(divBy.size)).round] });
		^dataAsRawArray;
	}

	//archive
	archive {
		super.writeBinaryArchive(pathName.fullPath ++ ".scbin");
	}
	*unarchive {| path |
		^super.readBinaryArchive(path);
	}
	writeSoundFile {| path, header="WAV", format="int16", normalize=\across |
		var sfName, soundFile;
		var extension = "." ++ header.toLower;
		if(path.isNil, { sfName = pathName.fileName });
		soundFile = SoundFile.new
		.headerFormat_(header)
		.sampleFormat_(format)
		.sampleRate_(this.sampleRate)
		.numChannels_(data.cols)
		;
		if(
			soundFile.openWrite(
				pathName.pathOnly +/+
				sfName ++
				"@" ++
				data.cols ++
				"ch" ++
				sampleRate ++
				extension
			), {
				normalize.switch(
					\across, { soundFile.writeData(this.normalizeAcrossBipolar) },
					\within, { soundFile.writeData(this.normalizeWithinBipolar) },
					{ Error("Normalize failed.\n").throw }
				);
				soundFile.close;
		}, {
				Error("Could not open.\n").throw
		});
	}
}