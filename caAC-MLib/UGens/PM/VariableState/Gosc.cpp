/*
Marinus Klaassen. Dec 2012

Towards an 1-dimensional mass coupled oscillator.  
 
Next NCoupling gravity oscillator will deal with passing Arrays into Ugens. 
The models in text: 
Wall - Spring - Mass - Spring - Mass - etc.. etc.. - Spring - Wall
StiffnessArray should aways be 1 element bigger the mass so when this is not 
The sclang array can be accessed by specifying the number of the input we want to acces. 
unit->mNumInputs tells how many input in total are being provided. 

Check mathlab for gravity oscillators and coupled systems. 
When a makelist is made cmake will build the apropiate files to compile this extenions file with the make compiler. 
cmake -DSC_PATH=/Users/MarinusKlaassen/Desktop/SuperCollider-Source -DCMAKE_OSC_ARCHITECTURES='i386;x86_64'

Add the stiffness contant for mass. 
 
SampleRate = 44100; 
Freq = 440; 
Theta = 2*pi * Freq / SampleRate
y = sin(theta); 
calculate the frequency of a mass
 
F = (1 / 2π) * √ (k / m)

F^2 = (1/2pi) * k
 1          1 
 
1/k = 0.5pi / f^2; 
 
k = f^(1/2)/0.5pi
how to calculate corresponding stiffness when a frequency is given? 

freq^2 = (1/2pi)^2 * b/m
  1             1
b/m = (1/2pi)^2 / freq^2 = freq^2 / 2pi^2
b/1 = etc. 
*/
 
// constant used in CoupledSpring functions
#define ONEOVERMASSMIN 0.0001f
#define QMIN 0.999f
#define QMAX 0.999999f
#define CLIPMIN 1.0f
#define CLIPMAX 2.0f
#define SPRINGMIN 0.0f
#define SPRINGMAX 1.0f

#include "SC_PlugIn.h"

// The static interface table pointer is a reference to a table of SuperColliderfunction such as 
// the ones to register a new ugen. 
static InterfaceTable *ft;

// structs will hold data wich are passed from one vector to another or from constructor to vector func.
struct Gosc1 : public Unit {
	float vx;
	float ax;
	float sx;
};

struct CoupledSpring : public Unit {
	float a1;				/* velocity intermediate variable*/
	float v1;				/* acceleration*/
	float s1;				/* displacement or position variable*/
	float a2;				/* velocity intermediate variable*/
	float v2;				/* acceleration*/
	float s2;				/* displacement or position variable*/
};

struct CoupledSpring2 : public Unit {
	float a1;				/* velocity intermediate variable*/
	float v1;				/* acceleration*/
	float s1;				/* displacement or position variable*/
	float a2;				/* velocity intermediate variable*/
	float v2;				/* acceleration*/
	float s2;				/* displacement or position variable*/
};

struct CoupledSpring3 : public Unit {
	float a1;				/* velocity intermediate variable*/
	float v1;				/* acceleration*/
	float s1;				/* displacement or position variable*/
	float a2;				/* velocity intermediate variable*/
	float v2;				/* acceleration*/
	float s2;				/* displacement or position variable*/
	float a3;				/* velocity intermediate variable*/
	float v3;				/* acceleration*/
	float s3;				/* displacement or position variable*/
};

struct NCoupling : public Unit {
	float *acc_Array;	 
	float *vel_Array;  
	float *disp_Array;
	int n; 
};




// declarartion of local scope functions
double mclip (double input, double min, double max); 
double mclipLo (double input, double min); 

// declaration of global unit generator functions. 
extern "C" {
	void Gosc1_next(Gosc1 *unit, int inNumSamples);
	void Gosc1_Ctor(Gosc1 *unit);
	void CoupledSpring_Ctor(CoupledSpring *unit); 
	void CoupledSpring_next(CoupledSpring *unit, int inNumSamples); 
	void CoupledSpring2_Ctor(CoupledSpring2 *unit); 
	void CoupledSpring2_next(CoupledSpring2 *unit, int inNumSamples); 
	void CoupledSpring3_Ctor(CoupledSpring3 *unit); 
	void CoupledSpring3_next(CoupledSpring3 *unit, int inNumSamples); 
	void NCoupling_Ctor(NCoupling *unit); 
	void NCoupling_next(NCoupling *unit, int inNumSamples);
}


// declaration of function wich are local to this file. 
double mclip (double input, double min, double max) {
	input = input < min ? min : input;
	input = input > max ? max : input;
	return input; 
}

double mclipLo (double input, double min) { 
	input = input < min ? min : input;
	return input; 
}

void Gosc1_Ctor(Gosc1 *unit) {
	unit->vx = IN0(1);
	unit->ax = IN0(2);
	unit->sx = IN0(3);
	
	SETCALC(Gosc1_next);
	Gosc1_next(unit, 1); 
}

void Gosc1_next(Gosc1 *unit, int inNumSamples) {

	float *out = OUT(0);
	float *mass = IN(0); 
		
	float vx = unit->vx; 
	float ax = unit->ax;
	float sx = unit->sx;
		
	// fill output buffer
	for (int i = 0; i < inNumSamples; ++i) {
		
		ax = (float) tanh(sx * 40) * *mass; 
		vx = SAMPLEDUR * ax + vx; // SAMPLEDUR gives the duration of the sample rate.
		sx = SAMPLEDUR * vx + sx; 
		out[i] = sx; 
	}; 
	
	unit->vx = vx; 
	unit->ax = ax; 
	unit->sx = sx; 
}

void CoupledSpring_Ctor(CoupledSpring *unit) {
	// initialize feedback parameters
	unit->a1 = 0.0f;
	unit->v1 = 0.0f; 
	unit->s1 = 0.0f;
    unit->a2 = 0.0f;
	unit->v2 = 0.0f; 
	unit->s2 = 0.0f;
	// this tells scstnth the name of the calculation function for this Ugen. 	
	SETCALC(CoupledSpring_next); 
	// calculate one sample output.
	CoupledSpring_next(unit, 1); 
}

void CoupledSpring_next(CoupledSpring *unit, int inNumSamples) {

	// copy variables from struct fields into local variables
	// this can improve the effience of the unit, since the c++ 
	// optimizer will typically cause the values to be loaded 
	// into registers
	float a1 = unit->a1;
	float v1 = unit->v1;
	float s1 = unit->s1;
	float a2 = unit->a2;
	float v2 = unit->v2;
	float s2 = unit->s2;

	// pointers to audio input and output 
	float *in = IN(0); 		
	float *out = OUT(0); 

	/*control rate user parameters hence the use of IN0(n)
	conditional statements delimiting user parameter range 
	minimum and maximum values are defined as constants at 
	the beginning of the file 
	The marcros IN() and OUT will return appropriate pointers for the 
	desired input/output. The grab a single control rate value IN0
	wich is a shortcut for IN(1)[0].
	*/
	double oneovermass1 = mclipLo(IN0(1),ONEOVERMASSMIN);  
	double oneovermass2 = mclipLo(IN0(2),ONEOVERMASSMIN);  
	double clip = IN0(3);  
	double qfact1 = mclip(IN0(4),QMIN,QMAX); 
	double qfact2 = mclip(IN0(5),QMIN,QMAX); 
	double nonlinear = mclip(IN0(6),SPRINGMIN,SPRINGMAX); 

	//y coordinates 
	double oneminnonlinear = 1.0-nonlinear;
	double oneminq1 = (1.0-qfact1);
	double oneminq2 = (1.0-qfact2);
	
	// variable spring1 wordt niet gebruikt in de vector berekening in het oude bestand
		
	float deltaS, spring1, spring2; 
	
	//The actual vector calculation of the CoupledSpring system
	for (int i = 0; i < inNumSamples; ++i) {
			
		deltaS = s1 - s2; // calculate delta displacement
		spring2 = (oneminnonlinear * deltaS) + nonlinear * pow(deltaS, 3);
		
		// processing input. 
		a1 = oneovermass1 * (in[i] - s1 - spring2); 
		v1 = a1 + v1 * qfact1; 
		s1 = oneminq1 * v1 + s1 * qfact1;
		
		a2 = oneovermass2 * spring2;
		v2 = a2 + v2 * qfact2;
		s2 = oneminq2 * v2 + s2 * qfact2;
		
		out[i] = s2; 
	};
	
	//copy local variables back into struct field
	unit->a1 = a1;		
	unit->v1 = v1;
	unit->s1 = s1;
	
	unit->a2 = a2;
	unit->v2 = v2;
	unit->s2 = s2;

}


void CoupledSpring2_Ctor(CoupledSpring2 *unit) {
	// initialize feedback parameters
	unit->a1 = 0.0f;
	unit->v1 = 0.0f; 
	unit->s1 = 1.0f;
    unit->a2 = 0.0f;
	unit->v2 = 0.0f; 
	unit->s2 = 0.0f;
	// this tells scstnth the name of the calculation function for this Ugen. 	
	SETCALC(CoupledSpring2_next); 
	// calculate one sample output.
	CoupledSpring2_next(unit, 1); 
}

void CoupledSpring2_next(CoupledSpring2 *unit, int inNumSamples) {
	
	// copy variables from struct fields into local variables
	// this can improve the effience of the unit, since the c++ 
	// optimizer will typically cause the values to be loaded 
	// into registers
	float a1 = unit->a1;
	float v1 = unit->v1;
	float s1 = unit->s1;
	float a2 = unit->a2;
	float v2 = unit->v2;
	float s2 = unit->s2;
	
	// pointers to audio input and output 		
	float *out = OUT(0); 
	
	/*The marcros IN() and OUT will return appropriate pointers for the 
	desired input/output. The grab a single control rate value IN0
	wich is a shortcut for IN(1)[0].
	*/

	float mass1 = IN0(0); 
	float mass2 = IN0(1); 
	
	float forcew1; // initialize force variable for spring wallL to m1
	float force12; // initialize force variable for spring m1 to m2
	float force2w; // initialize force variable for spring m2 to wallR
	
	float Stiffnessw1 = IN0(2);  // spring stiffness wallL to mass1 
	float Stiffness12 = IN0(3);  // spring stiffness mass1 to mass2
	float Stiffness2w = IN0(4);  // spring stiffness mass2 to wallR
	
	//The actual vector calculation of the CoupledSpring system
	for (int i = 0; i < inNumSamples; ++i) {
		
		forcew1 = -s1 * Stiffnessw1; 
		force2w = -s2 * Stiffness2w; 
		force12 = (s2-s1) * Stiffness12; 
		
		a1 = (forcew1-force12) / mass1;  
		a2 = (force2w-force12) / mass2; 
		
		v1 = v1 + SAMPLEDUR * a1; 
		v2 = v2 + SAMPLEDUR * a2; 
		
		s1 = s1 + SAMPLEDUR * v1; 
		s2 = s2 + SAMPLEDUR * v2; 

		out[i] = s1; 
	};
	
	//copy local variables back into struct field
	unit->a1 = a1;		
	unit->v1 = v1;
	unit->s1 = s1;
	
	unit->a2 = a2;
	unit->v2 = v2;
	unit->s2 = s2;
	
}


// with an extra added mass. 
void CoupledSpring3_Ctor(CoupledSpring3 *unit) {
	// initialize feedback parameters
	unit->a1 = 0.0f;
	unit->v1 = 0.0f; 
	unit->s1 = 1.0f;
    unit->a2 = 0.0f;
	unit->v2 = 0.0f; 
	unit->s2 = 0.0f;
	unit->a3 = 0.0f;
	unit->v3 = 0.0f; 
	unit->s3 = 0.0f;
	// this tells scstnth the name of the calculation function for this Ugen. 	
	SETCALC(CoupledSpring3_next); 
	// calculate one sample output.
	CoupledSpring3_next(unit, 1); 
}

void CoupledSpring3_next(CoupledSpring3 *unit, int inNumSamples) {
	
	// copy variables from struct fields into local variables
	// this can improve the effience of the unit, since the c++ 
	// optimizer will typically cause the values to be loaded 
	// into registers
	float a1 = unit->a1;
	float v1 = unit->v1;
	float s1 = unit->s1;

	float a2 = unit->a2;
	float v2 = unit->v2;
	float s2 = unit->s2;
	
	float a3 = unit->a3;
	float v3 = unit->v3;
	float s3 = unit->s3;
	
	// pointers to audio input and output 		
	float *out = OUT(0); 
	
	/*The marcros IN() and OUT will return appropriate pointers for the 
	desired input/output. The grab a single control rate value IN0
	wich is a shortcut for IN(1)[0].
	*/
	
	float mass1 = IN0(0); 
	float mass2 = IN0(1);
	float mass3 = IN0(2);
	
	float forcew1; // initialize force variable for spring wallL to m1
	float force12; // initialize force variable for spring m1 to m2
	float force23; // initialize force variable for spring m1 to m3
	float force3w; // initialize force variable for spring m3 to wallR
	
	float Stiffnessw1 = IN0(3);  // spring stiffness wallL to mass1 
	float Stiffness12 = IN0(4);  // spring stiffness mass1 to mass2
	float Stiffness23 = IN0(5);  // spring stiffness mass2 to mass3
	float Stiffness3w = IN0(6);  // spring stiffness mass3 to wallR
	
	//The actual vector calculation of the CoupledSpring system
	for (int i = 0; i < inNumSamples; ++i) {
		
		forcew1 = -s1 * Stiffnessw1; 
		force3w = -s3 * Stiffness3w; 
		force12 = (s2-s1) * Stiffness12; 
		force23 = (s3-s2) * Stiffness23; 
		
		a1 = (forcew1-force12) / mass1;  
		a2 = (force23-force12) / mass2; 
		a3 = (force3w-force23) / mass3; 
		
		v1 = v1 + SAMPLEDUR * a1; 
		v2 = v2 + SAMPLEDUR * a2; 
		v3 = v3 + SAMPLEDUR * a3; 
		
		s1 = s1 + SAMPLEDUR * v1; 
		s2 = s2 + SAMPLEDUR * v2; 
		s3 = s3 + SAMPLEDUR * v3;
		
		out[i] = s1; 
	};
	
	//copy local variables back into struct field
	unit->a1 = a1;		
	unit->v1 = v1;
	unit->s1 = s1;
	
	unit->a2 = a2;
	unit->v2 = v2;
	unit->s2 = s2;
	
	unit->a3 = a3;
	unit->v3 = v3;
	unit->s3 = s3;
}





// NCoupling with n amount of masses and n+1 amount of springs

void NCoupling_Ctor(NCoupling *unit) {
	
	// initialize feedback parameters
	int n = (int)IN0(0); // nMasses fixed is also a fixed parameter of initialization of the ugen instance. 
	
	// alloc memory for each 
	unit->acc_Array = (float*)RTAlloc(unit->mWorld, n * sizeof(float));
	unit->vel_Array = (float*)RTAlloc(unit->mWorld, n * sizeof(float));
	unit->disp_Array = (float*)RTAlloc(unit->mWorld, n * sizeof(float));
	
	*unit->disp_Array = 1; // initialize first discplacement of the mass. 
	
	unit->n = n; 
	
	// this tells scstnth the name of the calculation function for this Ugen. 	
	SETCALC(NCoupling_next); 
	// calculate one sample output.
	NCoupling_next(unit, 1); 
	
}

void NCoupling_next(NCoupling *unit, int inNumSamples) {
	
	// assign pointers to the memory locations
	float *acc_Array  = unit->acc_Array;	 
	float *vel_Array  = unit->vel_Array;  
	float *disp_Array = unit->disp_Array;					
	
	// pointers to audio input and output 		
	float *out = OUT(0); 
	
	/*The marcros IN() and OUT will return appropriate pointers for the 
	desired input/output. The grab a single control rate value IN0
	wich is a shortcut for IN(1)[0].
	n: IN0(0); // first input of the ugen
	mass: IN0(i + 1); // goofy way the read from the input array parameters. 
	stiffness: IN0(i + 1 + unit->n); 
	there is an array of forces with the same size as stiffness. 
	*/ 
		
	float forceL, forceR, stiffnessL, stiffnessR, mass, accel, vel, disp, dispL,dispR;  
	
	//The actual vector calculation of the CoupledSpring system
	for (int i = 0; i < inNumSamples; i++) {
		
		// printf ("start vector\n");
		// printf ("nMasses: %d\n", (int)IN0(0)); 
		// printf ("nMasses: %d\n", unit->n);
		
		for (int massIndex = 0; massIndex < unit->n; massIndex++) {
			
			accel = *(acc_Array + massIndex);  	 
			vel = *(vel_Array + massIndex);  
			disp = *(disp_Array + massIndex);
			
			// get neighbour displacements
			if (massIndex < 0 ) {
				dispL = 0; 
				dispR = *(disp_Array + (massIndex + 1)); 
			} else if (massIndex > 0 && massIndex < (unit->n - 1)) {	
				dispL = *(disp_Array + (massIndex - 1)); 
				dispR = *(disp_Array + (massIndex + 1)); 
			} else if (massIndex > (unit->n - 1)) {
				dispL = *(disp_Array + (massIndex - 1)); 
				dispR = 0; 
			}; 
			
			// printf ("massIndex: %d\n", massIndex+1);
			// printf ("stiffnessIndex: %d\n", massIndex+1+unit->n);
		
			mass = IN0(massIndex + 1); 
			stiffnessL = IN0(massIndex + 1 + unit->n); 
			stiffnessR = IN0(massIndex + 2 + unit->n);	
								   
			forceL = (dispL-disp)*stiffnessL;		   
			forceR = (dispR-disp)*stiffnessR; 		  
							
			// solving the equation:    
			accel = (forceL - forceR) / mass;  
			vel = vel + SAMPLEDUR * accel; 
			disp = disp + SAMPLEDUR * vel; 
		
			*(acc_Array + massIndex) = accel; 
			*(vel_Array + massIndex) = vel;  
			*(disp_Array + massIndex) = disp; 					
		}; // iteration through mass array. 
		
		out[i] = *unit->disp_Array; // output displacement of the first mass. 
	};

}

// dealing with points and arrays: http://www.functionx.com/cpp/Lesson14.htm

void NCoupling_Dtor(NCoupling* unit) {	
	// free buffers. 
	RTFree(unit->mWorld, unit->acc_Array);
	RTFree(unit->mWorld, unit->vel_Array);
	RTFree(unit->mWorld, unit->disp_Array);
}

PluginLoad(Gosc) {
	ft = inTable;  
	DefineSimpleUnit(Gosc1); 
	DefineSimpleUnit(CoupledSpring); 
	DefineSimpleUnit(CoupledSpring2); 
	DefineSimpleUnit(CoupledSpring3);
	DefineDtorUnit(NCoupling); 
}
