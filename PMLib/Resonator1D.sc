Resonator1D : ResonatorBase {
	classvar <validBoundaryConds;

	*initClass {
		Class.initClassTree(Array);
		validBoundaryConds = #[\bothClamped,\leftClampedRightSimplySupported,\leftSimplySupportedRightClamped,\bothSimplySupported,\leftClampedRightFree,\leftFreeRightClamped,\leftSimplySupportedRightFree,\leftFreeRightSimplySupported,\bothFree]
	}

	boundaryCond_ { arg newBoundaryCond;
		validBoundaryConds.includes(newBoundaryCond).if {
			boundaryCond = newBoundaryCond
		} {
			Error("arg boundaryCond=% is not a valid 1D boundary condition".format(newBoundaryCond)).throw
		}
	}

	jsonString {
		var bcStr = boundaryCond.asString;
		bcStr[0] = bcStr[0].toUpper;
		^"{ \"dim\":" ++ 1 ++ ",\"gamma\":" ++ gamma ++ ",\"kappa\":" ++ kappa ++ ",\"b1\":" ++ b1 ++ ",\"b2\":" ++ b2 ++ ",\"bc\":" ++ "\"" ++ bcStr ++ "\" }"
	}

}