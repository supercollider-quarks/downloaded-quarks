Resonator2D : ResonatorBase {
	var <epsilon;
	classvar <validBoundaryConds;

	*initClass {
		Class.initClassTree(Array); Class.initClassTree(Symbol);
		validBoundaryConds = #[\allSidesClamped,\leftClampedRightClampedTopClampedBottom,\leftClampedRightClampedTopClampedBottom,\leftClampedRightClampedTopSimplySupportedBottom,\leftClampedRightClampedTopSimplySupportedBottom,\leftClampedRightClampedTopSimplySupportedBottom,\leftClampedRightClampedTopFreeBottom,\leftClampedRightClampedTopFreeBottom,\leftClampedRightClampedTopFreeBottom,\leftClampedRightSimplySupportedTopClampedBottom,\leftClampedRightSimplySupportedTopClampedBottom,\leftClampedRightSimplySupportedTopClampedBottom,\leftClampedRightSimplySupportedTopSimplySupportedBottom,\leftClampedRightSimplySupportedTopSimplySupportedBottom,\leftClampedRightSimplySupportedTopSimplySupportedBottom,\leftClampedRightSimplySupportedTopFreeBottom,\leftClampedRightSimplySupportedTopFreeBottom,\leftClampedRightSimplySupportedTopFreeBottom,\leftClampedRightFreeTopClampedBottom,\leftClampedRightFreeTopClampedBottom,\leftClampedRightFreeTopClampedBottom,\leftClampedRightFreeTopSimplySupportedBottom,\leftClampedRightFreeTopSimplySupportedBottom,\leftClampedRightFreeTopSimplySupportedBottom,\leftClampedRightFreeTopFreeBottom,\leftClampedRightFreeTopFreeBottom,\leftClampedRightFreeTopFreeBottom,\leftSimplySupportedRightClampedTopClampedBottom,\leftSimplySupportedRightClampedTopClampedBottom,\leftSimplySupportedRightClampedTopClampedBottom,\leftSimplySupportedRightClampedTopSimplySupportedBottom,\leftSimplySupportedRightClampedTopSimplySupportedBottom,\leftSimplySupportedRightClampedTopSimplySupportedBottom,\leftSimplySupportedRightClampedTopFreeBottom,\leftSimplySupportedRightClampedTopFreeBottom,\leftSimplySupportedRightClampedTopFreeBottom,\leftSimplySupportedRightSimplySupportedTopClampedBottom,\leftSimplySupportedRightSimplySupportedTopClampedBottom,\leftSimplySupportedRightSimplySupportedTopClampedBottom,\leftSimplySupportedRightSimplySupportedTopSimplySupportedBottom,\allSidesSimplySupported,\leftSimplySupportedRightSimplySupportedTopSimplySupportedBottom,\leftSimplySupportedRightSimplySupportedTopFreeBottom,\leftSimplySupportedRightSimplySupportedTopFreeBottom,\leftSimplySupportedRightSimplySupportedTopFreeBottom,\leftSimplySupportedRightFreeTopClampedBottom,\leftSimplySupportedRightFreeTopClampedBottom,\leftSimplySupportedRightFreeTopClampedBottom,\leftSimplySupportedRightFreeTopSimplySupportedBottom,\leftSimplySupportedRightFreeTopSimplySupportedBottom,\leftSimplySupportedRightFreeTopSimplySupportedBottom,\leftSimplySupportedRightFreeTopFreeBottom,\leftSimplySupportedRightFreeTopFreeBottom,\leftSimplySupportedRightFreeTopFreeBottom,\leftFreeRightClampedTopClampedBottom,\leftFreeRightClampedTopClampedBottom,\leftFreeRightClampedTopClampedBottom,\leftFreeRightClampedTopSimplySupportedBottom,\leftFreeRightClampedTopSimplySupportedBottom,\leftFreeRightClampedTopSimplySupportedBottom,\leftFreeRightClampedTopFreeBottom,\leftFreeRightClampedTopFreeBottom,\leftFreeRightClampedTopFreeBottom,\leftFreeRightSimplySupportedTopClampedBottom,\leftFreeRightSimplySupportedTopClampedBottom,\leftFreeRightSimplySupportedTopClampedBottom,\leftFreeRightSimplySupportedTopSimplySupportedBottom,\leftFreeRightSimplySupportedTopSimplySupportedBottom,\leftFreeRightSimplySupportedTopSimplySupportedBottom,\leftFreeRightSimplySupportedTopFreeBottom,\leftFreeRightSimplySupportedTopFreeBottom,\leftFreeRightSimplySupportedTopFreeBottom,\leftFreeRightFreeTopClampedBottom,\leftFreeRightFreeTopClampedBottom,\leftFreeRightFreeTopClampedBottom,\leftFreeRightFreeTopSimplySupportedBottom,\leftFreeRightFreeTopSimplySupportedBottom,\leftFreeRightFreeTopSimplySupportedBottom,\leftFreeRightFreeTopFreeBottom,\leftFreeRightFreeTopFreeBottom,\allSidesFree]
	}

	*new { arg gamma=200,kappa=1,b1=0,b2=0,boundaryCond=\allSidesSimplySupported,epsilon=1;
		^super.newCopyArgs(gamma,kappa,b1,b2,boundaryCond,epsilon).init(epsilon)
	}

	init { arg epsilon;
		this.epsilon = epsilon
	}

	epsilon_ { arg newEpsilon;
		(epsilon > 0).if {
			epsilon = newEpsilon
		} {
			Error("arg epsilon=% must be a real number greater than 0".format(newEpsilon)).throw
		}
	}

	boundaryCond_ { arg newBoundaryCond;
		validBoundaryConds.includes(newBoundaryCond).if {
			boundaryCond = newBoundaryCond
		} {
			Error("arg boundaryCond=% is not a valid 2D boundary condition".format(newBoundaryCond)).throw
		}
	}

	jsonString {
		var bcStr = boundaryCond.asString;
		bcStr[0] = bcStr[0].toUpper;
		^"{ \"dim\":" ++ 2 ++ ",\"gamma\":" ++ gamma ++ ",\"kappa\":" ++ kappa ++ ",\"b1\":" ++ b1 ++ ",\"b2\":" ++ b2 ++ ",\"bc\":" ++ "\"" ++ bcStr ++ "\",\"epsilon\":" ++ epsilon ++ " }"
	}
}