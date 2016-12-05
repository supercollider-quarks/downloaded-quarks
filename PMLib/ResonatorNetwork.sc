ResonatorNetwork {
	var <resonators,<connPointMatrix,<massMatrix,<excPointMatrix,<readoutPointMatrix,<modalData;
	classvar pythonScriptPath,<>pythonPath;

	*initClass {
		Class.initClassTree(String);
		pythonScriptPath = this.class.filenameSymbol.asString.dirname ++ "/python";
		pythonPath = "/System/Library/Frameworks/Python.framework/Versions/Current/bin/python"
	}

	*new { arg resonators,connPointMatrix,massMatrix,excPointMatrix,readoutPointMatrix;
		^super.newCopyArgs(resonators,connPointMatrix,massMatrix,excPointMatrix,readoutPointMatrix)
		.init(resonators,connPointMatrix,massMatrix,excPointMatrix,readoutPointMatrix)
	}

	init { arg resonators,connPointMatrix,massMatrix,excPointMatrix,readoutPointMatrix;
		this.resonators = resonators ? [Resonator1D.new,Resonator1D.new(101)];
		this.connPointMatrix = connPointMatrix ? Array2D.fromArray(2,1,[0.5,0.5]);
		this.massMatrix = massMatrix ? Array2D.fromArray(2,1,[1,1]);
		this.excPointMatrix = excPointMatrix ? Array2D.fromArray(2,1,[0.25,0]);
		this.readoutPointMatrix = readoutPointMatrix ? Array2D.fromArray(2,1,[0.5,0])
	}

	resonators_ { arg newResonators;
		(newResonators.size == 0).if { newResonators = [newResonators].asList };
		newResonators.respondsTo('values').if { newResonators = newResonators.values };
		newResonators.isKindOf(Array).if { newResonators = newResonators.asList };
		resonators = newResonators
	}

	connPointMatrix_ { arg newConnPointMatrix;
		newConnPointMatrix.respondsTo('rows').if {
			connPointMatrix = newConnPointMatrix
		} {
			(newConnPointMatrix.shape.size != 2).if {
				Error("arg connPointMatrix=% must be a 2D indexable collection".format(newConnPointMatrix)).throw
			} {
				(newConnPointMatrix any: { |arr| arr.size != newConnPointMatrix[0].size }).if {
					Error("arg connPointMatrix=% must contain equal sized rows".format(newConnPointMatrix)).throw
				} {
					connPointMatrix = newConnPointMatrix
				}
			}
		}
	}

	massMatrix_ { arg newMassMatrix;
		newMassMatrix.respondsTo('rows').if {
			massMatrix = newMassMatrix
		} {
			(newMassMatrix.shape.size != 2).if {
				Error("arg massMatrix=% must be a 2D indexable collection".format(newMassMatrix)).throw
			} {
				(newMassMatrix any: { |arr| arr.size != newMassMatrix[0].size }).if {
					Error("arg massMatrix=% must contain equal sized rows".format(newMassMatrix)).throw
				} {
					massMatrix = newMassMatrix
				}
			}
		}
	}

	excPointMatrix_ { arg newExcPointMatrix;
		newExcPointMatrix.respondsTo('rows').if {
			excPointMatrix = newExcPointMatrix
		} {
			(newExcPointMatrix.shape.size != 2).if {
				Error("arg excPointMatrix=% must be a 2D indexable collection".format(newExcPointMatrix)).throw
			} {
				(newExcPointMatrix any: { |arr| arr.size != newExcPointMatrix[0].size }).if {
					Error("arg excPointMatrix=% must contain equal sized rows".format(newExcPointMatrix)).throw
				} {
					excPointMatrix = newExcPointMatrix
				}
			}
		}
	}

	readoutPointMatrix_ { arg newReadoutPointMatrix;
		newReadoutPointMatrix.respondsTo('rows').if {
			readoutPointMatrix = newReadoutPointMatrix
		} {
			(newReadoutPointMatrix.shape.size != 2).if {
				Error("arg readoutPointMatrix=% must be a 2D indexable collection".format(newReadoutPointMatrix)).throw
			} {
				(newReadoutPointMatrix any: { |arr| arr.size != newReadoutPointMatrix[0].size }).if {
					Error("arg readoutPointMatrix=% must contain equal sized rows".format(newReadoutPointMatrix)).throw
				} {
					readoutPointMatrix = newReadoutPointMatrix
				}
			}
		}
	}

	calcModalData { arg minFreq=25,maxFreq=(Server.default.sampleRate ? 44100).div(2),minT60=0.01,gain=1,pathname=pythonScriptPath ++ "/modalData.json",incl="ynnn",async=false;
		var cmdSeq;
		this.prCheckMatrixDimensions;
		this.prParseArgsAsJSON(minFreq,maxFreq,minT60,gain,pathname,incl);
		cmdSeq = "cd" + pythonScriptPath.shellQuote + "&&" + pythonPath.shellQuote + "systemSetup.py && rm -rf networkArgs.json";
		async.if { cmdSeq.unixCmd } { cmdSeq.systemCmd };
		this.prParseModalData(pathname)
	}

	loadModalData { arg pathname;
		this.prParseModalData(pathname)
	}

	/*
	 *******************
	 * PRIVATE METHODS *
	 *******************
	 */
	prCheckMatrixDimensions {
		var err=Error("matrix must have the same nr. of rows as there are items in resonators");
		[connPointMatrix,massMatrix,excPointMatrix,readoutPointMatrix] do: { |mtr|
			var op = mtr.respondsTo('rows').if { 'rows' } { 'size' };
			(mtr.perform(op) != resonators.size).if { err.throw }
		};
		err=Error("args connPointMatrix and massMatrix must have the same dimensions");
		(connPointMatrix.respondsTo('rows') and: { massMatrix.respondsTo('rows') }).if {
			(connPointMatrix.rows == massMatrix.rows and: { connPointMatrix.cols == massMatrix.cols }).not.if { err.throw }
		} {
			(connPointMatrix.respondsTo('rows') and: { massMatrix.respondsTo('rows').not }).if {
				(connPointMatrix.rows == massMatrix.size and: { connPointMatrix.cols == massMatrix[0].size }).not.if { err.throw }
			} {
				(connPointMatrix.respondsTo('rows').not and: { massMatrix.respondsTo('rows') }).if {
					(connPointMatrix.size == massMatrix.rows and: { connPointMatrix[0].size == massMatrix.cols }).not.if { err.throw }
				} {
					(connPointMatrix.shape != massMatrix.shape).if { err.throw }
				}
			}
		}
	}

	prParseArgsAsJSON { arg minFreq,maxFreq,minT60,gain,pathname,incl;
		var matrixToString = { |mtr|
			var str = "[";
			mtr.isKindOf(Array2D).if {
				mtr rowsDo: { |row,j| str = str ++ row ++ (j == (mtr.rows-1)).if { "]\n" } { "," } }
			} {
				mtr do: { |row,j| str = str ++ row ++ (j == (mtr.size-1)).if { "]\n" } { "," } }
			};
			str
		};

		File.use(pythonScriptPath ++ "/networkArgs.json","w",{ |f|
			var json = "{ \"minFreq\":" ++ minFreq ++ ",\"maxFreq\":" ++ maxFreq ++ ",\"minT60\":" ++ minT60 ++ ",\"gain\":" ++ gain ++ ",\"incl\":" ++ "\"" ++ incl ++ "\"" ++ ",\"path\":" ++ "\"" ++ pathname ++ "\"" ++ ",\"resonators\":" ++ "[";
			resonators do: { |obj,i|
				json = json ++ obj.jsonString ++ (i == resonators.lastIndex).if { "]," } { "," }
			};
			json = json ++ "\"connPointMatrix\":" ++ matrixToString.(connPointMatrix) ++ ",";
			json = json ++ "\"massMatrix\":" ++ matrixToString.(massMatrix) ++ ",";
			json = json ++ "\"excPointMatrix\":" ++ matrixToString.(excPointMatrix) ++ ",";
			json = json ++ "\"readoutPointMatrix\":" ++ matrixToString.(readoutPointMatrix) ++ " }";
			f.write(json)
		})
	}

	prParseModalData { arg pathname;
		File.use(pathname,"r",{ |f|
			File.exists(pathname).not.if {
				Error("The file % does not exist. Hence, it is likely that no modal data has been calculated.".format(pathname)).throw
			};
			modalData = pathname.parseYAMLFile.dataAsStringToFloat;
		})
	}

}

+ SequenceableCollection {
	indicesOfNotEqual { |item|
		var indices=Array.new;
		this.do { arg val, i;
			if (item != val) { indices = indices.add(i) }
		};
		^indices
	}

	indicesOfLessThan { |item|
		var indices=Array.new;
		this.do { arg val, i;
			if (item >= val) { indices = indices.add(i) }
		};
		^indices
	}

	indicesOfLessThanAbs { |item|
		var indices=Array.new;
		this.do { arg val, i;
			if (item.abs >= val.abs) { indices = indices.add(i) }
		};
		^indices
	}

	indicesOfGreaterThan { |item|
		var indices=Array.new;
		this.do { arg val, i;
			if (item <= val) { indices = indices.add(i) }
		};
		^indices
	}

	indicesOfGreaterThanAbs { |item|
		var indices=Array.new;
		this.do { arg val, i;
			if (item.abs <= val.abs) { indices = indices.add(i) }
		};
		^indices
	}
}

// converts all dictionary data in string format to float format
+ Dictionary {
	dataAsStringToFloat {
		var stringToFloat = { |key,val|
			val.isKindOf(Dictionary).if {
				val.keysValuesChange(stringToFloat)
			} {
				val.asFloat
			}
		};
		this.keysValuesChange(stringToFloat)
	}
}