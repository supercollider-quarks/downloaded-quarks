VGFreqEditor : VGRatioEditor {
	var <localDetCent, <totalDetCent, <extDetCent, <relCent;

	var width = 350, height = 44, gap;

	*defaultNames {
		^[ [\refFreq, \baseFreq, \totalDetune, \localDetune, \extDetune],
		  [\relCent, \currFreq, \totalDetCent, \localDetCent, \extDetCent]
		]
	}

	*defaultVals { ^[440, 660, 1, 1] }

	makeBoxes { |w|

		slSpec = [0.9438, 1.0594, \exp].asSpec; //about +- 100cent
		//slSpec = [0.8, 1.25, \exp].asSpec; //original setting was this

		boxes = [

			{|box| this.refVal_(box.value) },
			{|box| this.baseVal_(box.value) },
			{|box| this.totalDetune_(box.value) },
			{|box| this.localDetune_(box.value) },
			{|box| this.extDetune_(box.value) },

			{|box| this.relCent_(box.value) },
			{|box| this.currVal_(box.value) },
			{|box| this.totalDetCent_(box.value) },
			{|box| this.localDetCent_(box.value) },
			{|box| this.extDetCent_(box.value) },
		].collect { |func, i|
			NumberBox(w, Rect(0,0, width / 5 - 3, 20))
			     .decimals_(3)
			     .action_(func);
		};
		boxes[[0, 1, 4, 9]].do { |box| box.enabled_(false).scroll_(false) };
	}
	allVals {
		^[	refVal, baseVal, totalDetune, localDetune, extDetune,
			relCent, currVal, totalDetCent, localDetCent, extDetCent]
	}

	altCalc {
		sendFunc.(localDetune);
			// secondary representations
		relCent = (currVal / refVal).ratiomidi * 100;
		totalDetCent = totalDetune.ratiomidi * 100;
		localDetCent = localDetune.ratiomidi * 100;
		extDetCent = extDetune.ratiomidi * 100;
	}

	localDetCent_ { |inval|
		this.localDetune_( (inval * 0.01).midiratio);
	}
	totalDetCent_ { |inval|
		this.totalDetune_( (inval * 0.01).midiratio);
	}
	extDetCent_ { |inval|
		this.extDetune_( (inval * 0.01).midiratio);
	}
	relCent_ { |inval|
		this.currVal_(refVal * (inval * 0.01).midiratio);
	}

}

VGAmpEditor : VGRatioEditor {
	var <localDetDB, <totalDetDB, <extDetDB, <relDB;

	var width = 350, height = 44, gap, <boxes;

	*defaultNames {
		^[ [\refAmp, \baseAmp, \totalDetune, \localDetune, \extDetune],
		  [\relDB, \currAmp, \totalDetDB, \localDetDB, \extDetDB]
		]
	}
	*defaultVals { ^[0.1, 0.2, 1, 1] }

	makeBoxes { |w|
		boxes = [

			{|box| this.refVal_(box.value) },
			{|box| this.baseVal_(box.value) },
			{|box| this.totalDetune_(box.value) },
			{|box| this.localDetune_(box.value) },
			{|box| this.extDetune_(box.value) },

			{|box| this.relDB_(box.value) },
			{|box| this.currVal_(box.value) },
			{|box| this.totalDetDB_(box.value) },
			{|box| this.localDetDB_(box.value) },
			{|box| this.extDetDB_(box.value) },
		].collect { |func, i|
			NumberBox(w, Rect(0,0, width / 5 - 3, 20))
			    .decimals_(3)
			    .action_(func);
		};
		boxes[[0, 1, 4, 9]].do { |box| box.enabled_(false).scroll_(false) };
	}
	allVals {
		^[	refVal, baseVal, totalDetune, localDetune, extDetune,
			relDB, currVal, totalDetDB, localDetDB, extDetDB]
	}

	altCalc {
		sendFunc.(localDetune);
			// secondary representations
		relDB = (currVal / refVal).ampdb;
		totalDetDB = totalDetune.ampdb;
		localDetDB = localDetune.ampdb;
		extDetDB = extDetune.ampdb;
	}

	localDetDB_ { |inval|
		this.localDetune_(inval.dbamp);
	}
	totalDetDB_ { |inval|
		this.totalDetune_(inval.dbamp);
	}
	extDetDB_ { |inval|
		this.extDetune_(inval.dbamp);
	}
	relDB_ { |inval|
		this.currVal_(refVal *inval.dbamp);
	}

}