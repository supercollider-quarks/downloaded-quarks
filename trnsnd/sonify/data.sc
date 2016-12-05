Data {
	var <>data, <>sampleRate, <>storage;
	var <realtime = false;
	// r/w
	readCSV { |path="" skip=0|
		data = CSVFileReader.readInterpret(path, true, true, startRow: skip);
		data = data.asList;
	}
	writeCSV { |path|
		var tmp = "";
		var file = File(path, "w");
		data.do({ |row|
			if(row.isKindOf(SequenceableCollection), {
				row.do({ |val i|
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
	plot { |index|
		if(index.isNil, {
			// all
			Array.fill(data[0].size, { |i| this.col(i) }).plot;
		}, {
			// select
			if(index.isKindOf(SequenceableCollection), {
				Array.fill(index.size, { |i| this.col(index[i]) }).plot;
			}, {
				this.col(index).plot;
			});
		});
	}
	info {
		inform("***********************************************");
		inform("rows: " ++ data.size);
		this.localSpecs.do { |item,i|
			inform("col #" ++ i ++ " range -> "++ " (min: " ++ item.minval ++ ", " ++ "max: " ++ item.maxval ++ ")");
		};
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
	sampleDur {
		// for Pbind use
		if(sampleRate.isNil, {
			Error("sampelRate unknown.").throw;
		}, {
			^sampleRate.reciprocal;
		});
	}
	row { |index=0|
		^data[index]
	}
	col { |index=0 normalized=false|
		var spec;
		normalized.switch(
			\within, { spec = this.localSpecs[index] },
			\across, { spec = this.globalSpec }
		);
		if(spec.isNil, {
			^Array.fill(data.size, { |i| data[i][index] })
		}, {
			^Array.fill(data.size, { |i| spec.unmap(data[i][index]) })
		});
	}
	at { |index=0|
		^this.col(index)
	}
	// archive
	archive { |path|
		data.writeBinaryArchive(path);
	}
	unarchive { |path|
		data = Object.readBinaryArchive(path);
	}
	// audify
	writeSoundFile { |path header="WAV" format="int16"|
		var sf;
		sf = SoundFile.new
		.headerFormat_(header)
		.sampleFormat_(format)
		.sampleRate_(sampleRate)
		.numChannels_(data[0].size)
		;
		if(sf.openWrite(path), {
			// must be bipolar normalized FloatArray
			sf.writeData(FloatArray.newFrom(data.flat));
			sf.close;
		}, {
			Error("Could not open.").throw;
		});
	}
	// ___________________________________ map
	localSpecs {
		^Array.fill(data[0].size, { |i|
			var col = this.col(i);
			ControlSpec(col.minItem, col.maxItem);
		});
	}
	globalSpec {
		var minItems, maxItems;
		minItems = this.localSpecs.collect({ |item| item.minval });
		maxItems = this.localSpecs.collect({ |item| item.maxval });
		^ControlSpec(minItems.minItem, maxItems.maxItem);
	}
	map { |index outSpec inSpec|
		if(inSpec.isNil, {
			if(realtime, {
				^Pfunc { outSpec.asSpec.map(data[index]) };
			}, {
				^Pseq(outSpec.asSpec.map(this.col(index, \within)));
			});
		}, {
			if(realtime, {
				^Pfunc { outSpec.asSpec.map(inSpec.asSpec.unmap(data[index])) };
			}, {
				^Pseq(outSpec.asSpec.map(inSpec.asSpec.unmap(this.col(index))));
			});
		});
	}
	// ___________________________________ array
	rangePut { |index=0 from to item|
		from.for(to, { |i| data[i][index] = item });
	}
	// ___________________________________ dsp
	clip { |lo hi| data = data.clip(lo, hi) }
	// unipolar
	normalize1 { |how='within'|
		how.switch(
			'across', {
				var spec = this.globalSpec;
				data.size.do({ |i|
					data[0].size.do({ |j|
						data[i][j] = spec.unmap(data[i][j]);
					});
				});
			},
			'within', {
				var spec = this.localSpecs;
				data.size.do({ |i|
					data[0].size.do({ |j|
						data[i][j] = spec[j].unmap(data[i][j]);
					});
				});
			}
		);
	}
	// bipolar
	normalize2 { |how='within'|
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
	leakDC { |index=0|
		var prev = data[0][index], next, y=0;
		data.do({ |row i|
			next = row.clipAt(i+1);
			y = next - prev + (0.995 * y);
			prev = next;
			data[i][index] = y;
		});
	}
	lpf { |index=0 freq|
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
		data.do({ |row i|
			y0 = row[index] + (b1 * y1) + (b2 * y2);
			data[i][index] = a0 * (y0 + (2 * y1) + y2);
			y2 = y1;
			y1 = y0;
		});
	}
	hpf { |index=0 freq|
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
		data.do({ |row i|
			y0 = row[index] + (b1 * y1) + (b2 * y2);
			data[i][index] = a0 * (y0 - (2 * y1) + y2);
			y2 = y1;
			y1 = y0;
		});
	}
}