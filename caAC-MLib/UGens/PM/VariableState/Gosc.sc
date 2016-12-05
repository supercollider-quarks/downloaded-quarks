
Gosc1 : UGen {
	*ar { arg mass = 4000 , vx = 1, ax = 0, sx = 0;
		^this.multiNew('audio', mass,vx, ax, sx)

	}
}

CoupledSpring : UGen {
	*ar { arg in,oneovermass1=100,oneovermass2=300,clip=1,qfact1=0.99999,qfact2=0.9998,nonlinear=0.3;
		^this.multiNew('audio',in,oneovermass1,oneovermass2,clip,qfact1,qfact2,nonlinear)

	}
}

CoupledSpring2 : UGen {
	*ar { arg mass1=0.00001, mass2=0.000007, stiffnessw1 = 1, stiffness12 = 0.2, stiffness2w = 2.0;
		^this.multiNew('audio',mass1, mass2, stiffnessw1, stiffness12, stiffness2w)

	}
}

CoupledSpring3 : UGen {
	*ar { arg mass1=0.00001, mass2=0.000007, mass3=0.000004, stiffnessw1 = 1, stiffness12 = 0.2, stiffness23 = 0.1, stiffness3w = 2.0;
		^this.multiNew('audio',mass1, mass2,mass3, stiffnessw1, stiffness12, stiffness23, stiffness3w)

	}
}

// Instead of calling the UGen method multiNew we call multiNewList. All arguments are now a single array.
// now the ugen can have a variable amount of inputs.
// In C++ we will acces the numerical position of the list.
// So it is not possible to determine the size of a loop and wrap around
// by just looking at the size of the massArray.
// So I have to add an argument nMass wich stands for the amount of masses.

NCoupling : UGen {
	*ar { arg nMass,massArray, stiffnessArray, mul = 1, add = 0;
		^this.multiNewList(['audio',nMass] ++ massArray.asArray ++ stiffnessArray).madd(mul,add);
	}
}


