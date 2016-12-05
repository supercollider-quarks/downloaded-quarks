ResonatorBase {
	var <gamma,<kappa,<b1,<b2,<boundaryCond;
	classvar <sampleRate,<timeStep;

	*initClass {
		Class.initClassTree(Server);
		sampleRate = Server.default.sampleRate ? 44100;
		timeStep = sampleRate.reciprocal;
	}

	*new { arg gamma=200,kappa=1,b1=0,b2=0,boundaryCond=\bothSimplySupported;
		(super == ResonatorBase).if {
			Error("an instance of % is not supposed to be created directly by the user".format(this)).throw
		} {
			^super.newCopyArgs(gamma,kappa,b1,b2,boundaryCond).init(gamma,kappa,b1,b2,boundaryCond)
		}
	}

	init { arg gamma,kappa,b1,b2,boundaryCond;
		this.gamma = gamma;
		this.kappa = kappa;
		this.b1 = b1;
		this.b2 = b2;
		this.boundaryCond = boundaryCond
	}

	gamma_ { arg newGamma;
		(newGamma >= 0 and: { newGamma <= (Server.default.sampleRate ? 44100).div(2) }).if {
			gamma = newGamma
		} {
			Error("arg gamma=% must be a real number between 0 and %".format(newGamma,Server.default.sampleRate ? 44100)).throw
		}
	}

	kappa_ { arg newKappa;
		(newKappa >= 0).if {
			kappa = newKappa
		} {
			Error("arg kappa=% must be a real number greater than or equal to 0".format(newKappa)).throw
		}
	}

	b1_ { arg newB1;
		(newB1 >= 0).if {
			b1 = newB1
		} {
			Error("arg b1=% must be a real number greater than or equal to 0".format(newB1)).throw
		}
	}

	b2_ { arg newB2;
		(newB2 >= 0).if {
			b2 = newB2
		} {
			Error("arg b2=% must be a real number greater than or equal to 0".format(newB2)).throw
		}
	}

	boundaryCond_ { arg newBoundaryCond;
		boundaryCond = nil
	}
}