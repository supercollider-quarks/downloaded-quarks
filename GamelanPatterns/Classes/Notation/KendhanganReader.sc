KendhanganReader : BalunganReader {
	classvar <strokeMap;

	*initClass {
		strokeMap = (
				// Kdh. Gendhing: capitals
				// right hand:
		b: \g_bem, // sc 3.6 somehow cause problems with capitals as key - for now quick fix for bem and thung
		u: \g_thung, // see above
		e: \g_ket,
		R: \g_kret,
		J: \g_tapR,
				// left hand - not yet used thus still capitals:
		T: \g_tak,
		O: \g_thong,
		Q: \g_thok,
		I: \g_tapL,

				// Kdh. Ketipung: minuscles
				//right hand
		p: \k_thung,
		k: \k_ket,
		r: \k_kret,
		j: \k_tapR,
				// left hand:
		t: \k_tak,
		l: \k_lang, // faked - copy of \k_tak. We need lang for the combination dhang: [Bl]
		o: \k_thong,
		q: \k_thok,
		i: \k_tapL,
		y: \k_kretL // faked - copy of \k_kret (which is right or neutral)

/*			//	Kdh. Ciblon -
				// notation? no more good letters available!
				// use letter-prefix, enclosure??
				// right hand

					// available samples
					\c_dhen,
					\c_dhet,
					\c_hen,
					\c_ket,
					\c_kret,
					\c_tapR,
					\c_thung.
				// left hand
					// available samples
					\c_lang,
					\c_lung,
					\c_thong,
					\c_tak,
					\c_tapL,
					\c_thok,
					\c_kretLeft
*/


	)

	}
	*getEvent { arg str;
		var res = strokeMap[str.asSymbol];
		res.postln;
		if (res.notNil, { ^(instKey: \Kendhang, noteKey: res, dur: 1.0) });

		events.pairsDo {|key, val|
			if(str == key) {
				^val.value
			};
		};
		^if(str.every(_.isDecDigit)) {
				(degree: str.asInteger, dur: 1.0)
		}
	}
}


// Old version using capitals for  kendhang bem -
// for unknown reason this doesn't work with SC 3.6
/*
KendhanganReader : BalunganReader {
	classvar <strokeMap;

	*initClass {
		strokeMap = (
				// Kdh. Gendhing: capitals
				// right hand:
		B: \g_bem,
		P: \g_thung,
		K: \g_ket,
		R: \g_kret,
		J: \g_tapR,
				// left hand:
		T: \g_tak,
		O: \g_thong,
		Q: \g_thok,
		I: \g_tapL,

				// Kdh. Ketipung: minuscles
				//right hand
		p: \k_thung,
		k: \k_ket,
		r: \k_kret,
		j: \k_tapR,
				// left hand:
		t: \k_tak,
		l: \k_lang, // faked - copy of \k_tak. We need lang for the combination dhang: [Bl]
		o: \k_thong,
		q: \k_thok,
		i: \k_tapL,
		y: \k_kretL // faked - copy of \k_kret (which is right or neutral)

/*			//	Kdh. Ciblon -
				// notation? no more good letters available!
				// use letter-prefix, enclosure??
				// right hand

					// available samples
					\c_dhen,
					\c_dhet,
					\c_hen,
					\c_ket,
					\c_kret,
					\c_tapR,
					\c_thung.
				// left hand
					// available samples
					\c_lang,
					\c_lung,
					\c_thong,
					\c_tak,
					\c_tapL,
					\c_thok,
					\c_kretLeft
*/


	)

	}
	*getEvent { arg str;
		var res = strokeMap[str.asSymbol];

		if (res.notNil, { ^(instKey: \Kendhang, noteKey: res, dur: 1.0) });

		events.pairsDo {|key, val|
			if(str == key) {
				^val.value
			};
		};
		^if(str.every(_.isDecDigit)) {
				(degree: str.asInteger, dur: 1.0)
		}
	}
}
*/