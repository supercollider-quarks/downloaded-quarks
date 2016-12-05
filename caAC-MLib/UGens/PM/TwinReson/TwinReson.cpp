/*
TwinReson.cpp
Ugen port from twinreson~ Marinus Klaassen december 2012
 
twinreson~.c, created by Katja Vetter Dec. 2012.

The twinreson~ class for PureData is a DSP analogy of a double coupled rc-network, a resonator.
Each resonator is a second order IIR section like used in biquad filters. Resonators are coupled via a
nonlinear function.
 
Frequency is a user parameter and it is used in calculation of the c1 filter coefficient.
 
Decaytime is a user-parameter and expresses decay time to amplitude 36.8 %, in milliseconds. 
It is used in calculating c1 and c2 filtercoefficients.
 
Omega is the angle velocity over one sample interval, expressed in radians, and it is calculated as 
frequency*2*pi/samplerate.
 
Radius is calculated as exp(-2000/samplerate/decaytime).
 
Attenuation factor (c0) is calculated as sin(omega). 
 
Feedback filter coefficient 1 (c1) is calculated as 2*cos(omega)*radius.
 
Feedback filter coefficient 2 (c2) is calculated as (radius*radius).
*/ 


#include "SC_PlugIn.h"

#define TWOPI 6.283185307179586
#define DECAYTIMEMIN 10.
#define FREQMIN 2.
#define NONLINEARMIN 0.
#define NONLINEARMAX 300
#define NRESONATORS 2

#define BIGORSMALL(f) ((((*(unsigned int*)&(f))&0x60000000)==0) || \
(((*(unsigned int*)&(f))&0x60000000)==0x60000000))

// InterfaceTable contains pointers to functions in the host (server).
static InterfaceTable *ft;

// typedef struct makes a type of reson with data abstraction possibilities of struct.  
typedef struct  {
    // delayed output values (filter states)
    float  ynmin1;    
    float  ynmin2;
    
    // user parameters
    float frequency;	
	float decaytime;
    
    // coefficients
    float c0;         // attenuator
    float c1;         // first feedback coefficient
    float c2;         // second feedback coefficient
} reson;

// [twinreson~] class definition
struct TwinReson : public Unit { 
	float f;
    float samplerate; 
	float nonlinear;
    reson reson[NRESONATORS];
}; 

extern "C" {
void TwinReson_Ctor(TwinReson *unit); 
void TwinReson_next(TwinReson *unit, int inNumSamples);
}
void TwinReson_setcoeffs(TwinReson *unit, int i);
double mclip (double input, double min, double max); 
double mclipLo (double input, double min); 


double mclip (double input, double min, double max) {
	input = input < min ? min : input;
	input = input > max ? max : input;
	return input; 
}

double mclipLo (double input, double min) { 
	input = input < min ? min : input;
	return input; 
}

// set coefficients for one resonator
void TwinReson_setcoeffs(TwinReson *unit, int i)
{
    float radius = exp(-2000. / unit->samplerate / unit->reson[i].decaytime);	
	float omega = (unit->reson[i].frequency * TWOPI) / unit->samplerate;
	
    unit->reson[i].c0 = sin(omega);                                        // attenuator
    unit->reson[i].c1 = 2 * cos(omega) * radius;                           // first feedback coefficient
	unit->reson[i].c2 = (radius * radius);                                 // second feedback coefficient
}

void TwinReson_Ctor(TwinReson *unit) { 
	unit->samplerate = SAMPLERATE; 
	unit->nonlinear = IN0(unit->mNumInputs-1) * 0.001; // last input is non-linear
    
    // get input::: continue with this after the break:
	for(int i=0; i<NRESONATORS; i++) {
		unit->reson[i].frequency = mclipLo(IN0(i*2+1), FREQMIN); 
		unit->reson[i].decaytime = mclipLo(IN0(i*2+2), DECAYTIMEMIN); 
    }; 
	
	TwinReson_setcoeffs(unit, 0); 
	
	SETCALC(TwinReson_next); 
	TwinReson_next(unit,1); // calculate one sample output  
}

void TwinReson_next(TwinReson *unit, int inNumSamples) {
	
	float x_spring, y_spring, yy_spring, yn, yyn, xn;

	float *in = IN(0); // signal input 
	
	float *resout1 = OUT(0); 
	//float *resout2 = OUT(1); 
	
	// copy filter state variables from struct fields to local
    float ynmin1 = unit->reson[0].ynmin1;					// delayed output values of resonator 1
    float ynmin2 = unit->reson[0].ynmin2;
    float yynmin1 = unit->reson[1].ynmin1;					// delayed output values of resonator 2
    float yynmin2 = unit->reson[1].ynmin2;
	
    // general user parameter
	unit->nonlinear = IN0(unit->mNumInputs-1) * 0.001; // last input is non-linear
	float nonlinear = unit->nonlinear; 
	// get input::: continue with this after the break:
	for(int i=0; i<NRESONATORS; i++) {
		unit->reson[i].frequency = mclipLo(IN0(i*2+1), FREQMIN); 
		unit->reson[i].decaytime = mclipLo(IN0(i*2+2), DECAYTIMEMIN); 
    }; 
		
	TwinReson_setcoeffs(unit, 0); 
		
    // resonator 1 coefficients
    float c0 = unit->reson[0].c0;
	float c1 = unit->reson[0].c1;
	float c2 = unit->reson[0].c2;
    
    // resonator 2 coefficients
    float cc0 = unit->reson[1].c0;
	float cc1 = unit->reson[1].c1;
	float cc2 = unit->reson[1].c2;
	    
	for (int i=0; i<inNumSamples; i++) { 
 		xn = in[i]; 
		x_spring = nonlinear * pow(ynmin1 - yynmin1, 3);	
		yn = ((xn - x_spring) * c0) + (c1 * ynmin1) - (c2 * ynmin2); // resonator 1 output 
		yyn = ((xn + x_spring) * cc0) + (cc1 * yynmin1) - (cc2 * yynmin2); // resonator 2 output
		resout1[i]  = yn; 
		//resout2[i]  = yyn; 
		//this code in case of separate nonlinear antiphase feedback
		//y_spring = nonlinear * pow(ynmin1, 3);
		//yy_spring = nonlinear * pow(yynmin1, 3);
		//*y_out++ = yn = ((xn - y_spring) * attenuator1) + (c1 * ynmin1) - (c2 * ynmin2);
		//*yy_out++ = yyn = ((xn  - yy_spring) * attenuator2) + (cc1 * yynmin1) - (cc2 * yynmin2);
		if(BIGORSMALL(yn)) yn = 0.;      // avoid recycling subnormals
        if(BIGORSMALL(yyn)) yyn = 0.;
        ynmin2 = ynmin1;
		ynmin1 = yn;
		yynmin2 = yynmin1;
		yynmin1 = yyn;
    }
	
    unit->reson[0].ynmin1 = ynmin1; // store delayed output values of resonator 1 in struct
    unit->reson[0].ynmin2 = ynmin2;
	
    unit->reson[1].ynmin1 = yynmin1; // store delayed output values of resonator 2 in struct
    unit->reson[1].ynmin2 = yynmin2;

}

PluginLoad(TwinReson) {
	ft = inTable; 
	DefineSimpleUnit(TwinReson);
}
