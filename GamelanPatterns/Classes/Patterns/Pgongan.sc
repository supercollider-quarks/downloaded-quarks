
// embeds gongan name and information about where one is in the gongan

Pgongan : FilterPattern {
	var <>name, <>duration, <>iphase, <>stretch;

	*new { arg pattern, name, duration, iphase=1.0, stretch = 1.0; // duration = Formzahl
		^super.new(pattern).name_(name).duration_(duration).iphase_(iphase).stretch_(stretch)

	}

	embedInStream { arg inval;
		var phase = iphase, outval, stream, dur;
		stream = pattern.asStream;
		loop {
			outval = stream.next(inval);
			if(outval.isNil) { nil.alwaysYield; ^inval };
			outval.use {

				dur = ~dur;
				~gonganPhase = phase;
				~sabet = phase + 1; // "beat"
 				~gongan = name;
				~gonganDur = duration;

			};

			phase = phase + (dur * stretch) % duration;
			inval = outval.yield;
		}
	}


}

// embeds information about how many gongans have passed

PgonganCount : FilterPattern {
	embedInStream { arg inval;
		var count = 0, outval, stream;
		stream = pattern.asStream;
		loop {
			outval = stream.next(inval);
			if(outval.isNil) { nil.alwaysYield; ^inval };
			outval[\gonganCount] = count;
			if(outval[\gonganPhase] == 0.0) { count = count + 1 };
			inval = outval.yield;
		}
	}

}


PgonganPlayer : FilterPattern {
	classvar <>dict;
	var <>name, <>event;

	*new { arg pattern, name, event;
		^super.new(pattern).name_(name).event_(event)
	}

	*initClass {
		dict = (
			lancaran: (
				gong: [16],
				kenong: (4, 8 .. 16),
				kempul: [6, 12, 14],
				kethuk: (2, 6 .. 14),
				kempyang: []
			),
			ketawang: (
				gong: [16],
				kenong: [8, 16],
				kempul: [12],
				kethuk: (2, 6 .. 14),
				kempyang: (1, 3 .. 15)
			),
			ketawang_salahan: (
				gong: [16],
				kenong: [8, 16],
				kempul: [12],
				kethuk: (2, 6 .. 14).insert(3, 11.5).insert(4, 12.5),
				kempyang: (1, 3 .. 15)
			),
			ladrang: (
				gong: [32],
				kenong: (8, 16 .. 32),
				kempul: [12, 20, 28],
				kethuk: (2, 6 .. 30),
				kempyang: (1, 3 .. 31)
			),
			ladrang_salahan: (
				gong: [32],
				kenong: (8, 16 .. 32),
				kempul: [12, 20, 28],
//				kethuk: [2, 6, 10, 14, 18, 22, 26, 27.5, 28.5, 30],
				kethuk: (2, 6 .. 30).insert(7, 27.5).insert(8, 28.5),
				kempyang: (1, 3 .. 31)
			),
			ladrang_kebar: (
				gong: [32],
				kenong: (8, 16 .. 32),
				kempul: [12, 13,  20, 21, 28, 29, 30.5],
				kethuk: (2, 6 .. 30),
				kempyang: (1, 3 .. 31)
			),
			ladrang_64: (
				gong: [64],
				kenong: (16, 32 .. 64),
				kempul: [24, 40, 56],
				kethuk: (4, 12 .. 60),
				kempyang: (2, 6 .. 62)
			),

			merong_k2k: (
				gong: [64],
				kenong: (16, 32 .. 64),
				kempul: [],
				kethuk: (4, 12 .. 60),
				kempyang: []
			),
			merong_k4k: (
				gong: [128],
				kenong: (32, 64 .. 128),
				kempul: [],
				kethuk: (4, 12 .. 124),
				kempyang: []
			),
			merong_k8k: (
				gong: [256],
				kenong: (64, 128 .. 256),
				kempul: [],
				kethuk: (4, 12 .. 252),
				kempyang: []
			),
			merong_k2a: (
				gong: [128],
				kenong: (32, 64 .. 128),
				kempul: [],
				kethuk: (8, 24 .. 120),
				kempyang: []
			),
			merong_k4a: (
				gong: [256],
				kenong: (64, 128 .. 256),
				kempul: [],
				kethuk: (8, 24 .. 248),
				kempyang: []
			),
			merongKtw_k2k: (
				gong: [32],
				kenong: [16, 32],
				kempul: [],
				kethuk: (4, 12 .. 30),
				kempyang: []
			),
			merongKtw_k4k: (
				gong: [64],
				kenong: [32, 64],
				kempul: [],
				kethuk: (4, 12 .. 60),
				kempyang: []
			),
			merongKtw_k8k: (
				gong: [128],
				kenong: [64, 128],
				kempul: [],
				kethuk: (4, 12 .. 124),
				kempyang: []
			)
			inggah_k4: (
				gong: [64],
				kenong: (16, 32 .. 64),
				kempul: [],
				kethuk: (2, 6 .. 62),
				kempyang: (1, 3 .. 63)
			),
			inggah_k8: (
				gong: [128],
				kenong: (32, 64 .. 128),
				kempul: [],
				kethuk: (2, 6 .. 126),
				kempyang: (1, 3 .. 127)
			),
			inggah_k16: (
				gong: [256],
				kenong: (64, 128 .. 256),
				kempul: [],
				kethuk: (2, 6 .. 254),
				kempyang: (1, 3 .. 255)
			),
			inggahKtw_k8: (
				gong: [64],
				kenong: [32, 64],
				kempul: [],
				kethuk: (2, 6 .. 62),
				kempyang: (1, 3 .. 63)
			),
			inggahKtw_k16: (
				gong: [128],
				kenong: [64, 128],
				kempul: [],
				kethuk: (2, 6 .. 126),
				kempyang: (1, 3 .. 127)
			)
			/*more to be added here ... this should contain all regular periodical forms already
			though.
			There are (rarely used) salahans for all merong forms. Then there
			is the additional instrumentpair of Engkuk and Kemong.
			In rare ceremonial Styles there are a few additional interpunctuating
			Instruments*/
		);
	}

	embedInStream { arg inval;
		var outval, stream;
		stream = pattern.asStream;
		loop {
			outval = stream.next(inval);
			if(outval.isNil) { nil.alwaysYield; ^inval };
			outval = outval.copy;
			if(this.gongMoment(outval)) {
				outval.put(\degree, 0);
			} {
				outval.put(\degree, \rest);
			};
			outval.putAll(event);
			inval = outval.yield;
		}
	}

	gongMoment { arg event;
		var val = event.at(\sabet);
		var form = event.at(\gongan);
		var gongans  = dict.at(form);
		if(gongans.isNil) { Error("gongan form (%) not found!".format(event.at(\gongan))).throw };
		gongans.at(name).do { |x|
			if(val < x) { ^false }; // array is sorted
			if(val == x) { ^true };
		};
		^false
	}

}

