+ VGTuning {
	
		// temp only while MulTrees have no separate 
		// branches for scaleSteps :
	*retuneLaras { |laras, val = 1.0| 
		VGTuning.mulTrees[laras].mulBus.set(\freq, val)
	}
	
	*retuneInst { |laras, inst, val = 1.0| 
		VGTuning.mulTrees[laras].nodeAt(inst).set(\freq, val)
	}

	*retuneNote { |laras, inst, note, val = 1.0| 
		VGTuning.mulTrees[laras].nodeAt(inst, note).mulBus.set(\freq, val)
	}

		// test later
	*retunePartial { |laras, inst, note, partial, val = 1.0| 
		VGTuning.mulTrees[laras].nodeAt(inst, note, partial).mulBus.set(\freq, val)
	}
	
		// weird write into multree-notes, 
		// should be separately i the multree.
	*retuneDegree { |laras, degree, val = 1.0| 

		VGTuning.mulTrees[laras].branches.keysValuesDo { |inst, insttree| 
			inst.postln; 
			insttree.branches.keysValuesDo { |note, notetree| 
				if (note.asString.asInteger % 10 == degree) { 
					['detune:', inst, note].postln;
					notetree.mulBus.set(\freq, val);
				};
					
			}
		}
	}
	
}

+ VGScale {
	
	*getRefDegreeInterval { |laras, degree| 
		var myscale = VGScale.refScales[laras]; 
		var refFreq = myscale.at(VGScale.refNoteKey);
		var degFreq = myscale.at((VGScale.refNoteKey.asString.first ++ degree).asSymbol);
		var degInterval = (degFreq / refFreq).ratiomidi;
		
		^degInterval
	}
		// (copying is evil ;-)
	*getRefDegreeFreq { |laras, degree| 
		var myscale = VGScale.refScales[laras]; 
		var refFreq = myscale.at(VGScale.refNoteKey);
		var degFreq = myscale.at((VGScale.refNoteKey.asString.first ++ degree).asSymbol);
		//var degInterval = (degFreq / refFreq).ratiomidi;
		
		^degFreq
	}
	
	*setDegreeCent { arg laras, degree, cents; 
		
		var degInterval = this.getRefDegreeInterval (laras, degree);
		var newInterval = (cents * 0.01);
		var newRatio = (newInterval - degInterval).midiratio;
		
		VGTuning.retuneDegree(laras, degree, newRatio);
	}
	
	
}