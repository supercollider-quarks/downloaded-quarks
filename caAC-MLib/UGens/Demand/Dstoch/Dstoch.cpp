/* 
Marinus Klaassen 2012
Collection of various random distribution generators. 
 
Add function wich will put lo and hi values in the right order. 
  
Include Laplace transform. 
*/


#include "SC_PlugIn.h"
#include <cstdio>
#include <cmath>


static InterfaceTable *ft;


struct Dcoin : public Unit
{
	double m_repeats;
	int32 m_repeatCount;
	float weight;
	};

struct Dcoin2 : public Unit
{
	double m_repeats;
	int32 m_repeatCount;
	float weight;
};

struct Dexpon : public Unit
{
	double m_repeats;
	int32 m_repeatCount;
	float lo, range; 
};

struct Dexponential : public Unit
{
	double m_repeats;
	float threshold, gamma, upperLimit; 
	int32 m_repeatCount;
};

struct Dsumnrand : public Unit
{
	double m_repeats;
	float lo, range; 
	int32 m_repeatCount;
	int n; 
};

struct Dbeta : public Unit
{
	double m_repeats;
	float lo, range, a, b; 
	int32 m_repeatCount;
	int n; 
};

struct Dlinear : public Unit
{
	double m_repeats;
	float lo, range;
	int32 m_repeatCount;
	int favor; 
};

struct Dlogist : public Unit
{
	double m_repeats;
	float lo, range, x, lambda; 
	int32 m_repeatCount;
	};

struct Dsine : public Unit
{
	double m_repeats;
	float lo, range; 
	int32 m_repeatCount;
};

struct Dgamma : public Unit
{
	double m_repeats;
	int32 m_repeatCount;
};

struct Dcauchy : public Unit
{ 
	double m_repeats;
	int32 m_repeatCount; 
};

struct Dlaplace : public Unit
{ 
	double m_repeats;
	int32 m_repeatCount; 
};
	
extern "C"
{
	void Dcoin_next(Dcoin *unit, int inNumSamples);
	void Dcoin_Ctor(Dcoin* unit);
	void Dcoin2_next(Dcoin2 *unit, int inNumSamples);
	void Dcoin2_Ctor(Dcoin2* unit);
	void Dexpon_next(Dexpon *unit, int inNumSamples);
	void Dexpon_Ctor(Dexpon* unit);
	void Dexponential_Ctor(Dexponential *unit);
	void Dexponential_next(Dexponential *unit, int inNumSamples);
	void Dsumnrand_next(Dsumnrand *unit, int inNumSamples);
	void Dsumnrand_Ctor(Dsumnrand* unit);
	void Dbeta_next(Dbeta *unit, int inNumSamples);
	void Dbeta_Ctor(Dbeta* unit);
	void Dlinear_next(Dlinear *unit, int inNumSamples);
	void Dlinear_Ctor(Dlinear* unit);
	void Dlogist_next(Dlogist *unit, int inNumSamples);
	void Dlogist_Ctor(Dlogist* unit);
	void Dsine_next(Dsine *unit, int inNumSamples);
	void Dsine_Ctor(Dsine* unit);
	void Dgamma_next(Dgamma *unit, int inNumSamples);
	void Dgamma_Ctor(Dgamma* unit);
	void Dcauchy_next(Dcauchy *unit, int inNumSamples);
	void Dcauchy_Ctor(Dcauchy* unit);
	void Dlaplace_next(Dlaplace *unit, int inNumSamples);
	void Dlaplace_Ctor(Dlaplace* unit);
};

float BetaValue (float a, float b,Dbeta *unit);
double NonZeroRandom (Dbeta *unit);
float mclip (float input, float min, float max); 


float mclip (float input, float min, float max) {
	input = input < min ? min : input;
	input = input > max ? max : input;
	return input; 
}

void Dcoin_Ctor(Dcoin *unit)
{
	SETCALC(Dcoin_next);
	Dcoin_next(unit, 0);
	OUT0(0) = 0.f;
}

void Dcoin_next(Dcoin *unit, int inNumSamples)
{
	if (inNumSamples) {
		if (unit->m_repeats < 0.) {
			float x = DEMANDINPUT_A(0, inNumSamples);
			unit->m_repeats = sc_isnan(x) ? 0.f : floor(x + 0.5f);
		}
		if (unit->m_repeatCount >= unit->m_repeats) {
			OUT0(0) = NAN;
			return;
		}
		unit->m_repeatCount++;
		float weight = DEMANDINPUT_A(1, inNumSamples);
		
		if(!sc_isnan(weight)) unit->weight = weight;
		
		if (unit->mParent->mRGen->frand() >= unit->weight) OUT0(0) = 0.f;
		else OUT0(0) = 1.f; 	

	} else {
		unit->m_repeats = -1.f;
		unit->m_repeatCount = 0;
	}
}

void Dcoin2_Ctor(Dcoin2 *unit)
{
	SETCALC(Dcoin2_next);
	Dcoin2_next(unit, 0);
	OUT0(0) = 0.f;
}

void Dcoin2_next(Dcoin2 *unit, int inNumSamples)
{
	if (inNumSamples) {
		if (unit->m_repeats < 0.) {
			float x = DEMANDINPUT_A(0, inNumSamples);
			unit->m_repeats = sc_isnan(x) ? 0.f : floor(x + 0.5f);
		}
		if (unit->m_repeatCount >= unit->m_repeats) {
			OUT0(0) = NAN;
			return;
		}
		unit->m_repeatCount++;
		float weight = DEMANDINPUT_A(1, inNumSamples);
		
		if(!sc_isnan(weight)) unit->weight = weight;
		
		if (unit->mParent->mRGen->frand() >= unit->weight) OUT0(0) = -1.f;
		else OUT0(0) = 1.f; 	
		
	} else {
		unit->m_repeats = -1.f;
		unit->m_repeatCount = 0;
	}
}

void Dexpon_Ctor(Dexpon *unit)
{
	SETCALC(Dexpon_next);
	Dexpon_next(unit, 0);
	OUT0(0) = 0.f;
}

void Dexpon_next(Dexpon *unit, int inNumSamples)
{
	if (inNumSamples) {
		if (unit->m_repeats < 0.) {
			float x = DEMANDINPUT_A(0, inNumSamples);
			unit->m_repeats = sc_isnan(x) ? 0.f : floor(x + 0.5f);
		}
		if (unit->m_repeatCount >= unit->m_repeats) {
			OUT0(0) = NAN;
			return;
		}
		unit->m_repeatCount++;
		float lo = DEMANDINPUT_A(1, inNumSamples);
		float hi = DEMANDINPUT_A(2, inNumSamples);
		
		if(!sc_isnan(lo)) { unit->lo = lo;}
		if(!sc_isnan(hi)) { unit->range = hi - lo; }
		float	x = unit->mParent->mRGen->frand();
				x = x * x * unit->range + unit->lo;
				OUT0(0) = x;
	} else {
		unit->m_repeats = -1.f;
		unit->m_repeatCount = 0;
	}
}


void Dexponential_Ctor(Dexponential *unit)
{
	SETCALC(Dexponential_next);
	Dexponential_next(unit, 0);
	OUT0(0) = 0.f;
}

void Dexponential_next(Dexponential *unit, int inNumSamples)
{
	if (inNumSamples) {
		if (unit->m_repeats < 0.) {
			float x = DEMANDINPUT_A(0, inNumSamples);
			unit->m_repeats = sc_isnan(x) ? 0.f : floor(x + 0.5f);
		}
		if (unit->m_repeatCount >= unit->m_repeats) {
			OUT0(0) = NAN;
			return; 
		}
		unit->m_repeatCount++;
		
		float threshold = DEMANDINPUT_A(1, inNumSamples);
		float gamma = DEMANDINPUT_A(2, inNumSamples);
		float upperLimit = DEMANDINPUT_A(3, inNumSamples);
		
		float value; do {
		value = threshold + -1 *log(unit->mParent->mRGen->frand())/gamma;
		} while(value > upperLimit);   
	
		OUT0(0) = value;
	
	} else {
		unit->m_repeats = -1.f;
		unit->m_repeatCount = 0;
	}
}


void Dsumnrand_Ctor(Dsumnrand *unit)
{
	SETCALC(Dsumnrand_next);
	Dsumnrand_next(unit, 0);
	OUT0(0) = 0.f;
}

void Dsumnrand_next(Dsumnrand *unit, int inNumSamples)
{
	if (inNumSamples) {
		
		if (unit->m_repeats < 0.) {
			float x = DEMANDINPUT_A(0, inNumSamples);
			unit->m_repeats = sc_isnan(x) ? 0.f : floor(x + 0.5f);
		}
		if (unit->m_repeatCount >= unit->m_repeats) {
			OUT0(0) = NAN;
			return;
		}
		unit->m_repeatCount++;
		float lo = DEMANDINPUT_A(1, inNumSamples);
		float hi = DEMANDINPUT_A(2, inNumSamples);
		int	  n  = DEMANDINPUT_A(3, inNumSamples);
		
		if(!sc_isnan(lo)) unit->lo = lo; 
		if(!sc_isnan(hi)) unit->range = hi - lo; 
		if(!sc_isnan(n))  unit->n = (int) n; 
		if(unit->n <= 0)  unit->n = 1; 
		
		float x = 0; int i = 0;
		
		while (i < unit->n) {
			x = x + unit->mParent->mRGen->frand();
			i++; 	
		}
			
		x = ((x / unit->n) * unit->range) + unit->lo;
		OUT0(0) = x;
		
		// printf ("sumnrand output: %f\n",x);
		
	} else {
		unit->m_repeats = -1.f;
		unit->m_repeatCount = 0;
	}
}



void Dbeta_Ctor(Dbeta *unit)
{
	SETCALC(Dbeta_next);
	Dbeta_next(unit, 0);
	OUT0(0) = 0.f;
}

float BetaValue (float a, float b,Dbeta *unit) {	
	double y1,y2,sum;
	do {
		y1 = pow(NonZeroRandom(unit),1.0/a);
		y2 = pow(NonZeroRandom(unit),1.0/b);
		sum = y1+y2;
	}
	while (sum  > 1.0);
	return y1 / sum;
}

double NonZeroRandom (Dbeta *unit) {
	double value;
	do value = unit->mParent->mRGen->frand();
	while (value == 0.0);
	// non-zero random number output
	return value;
}

void Dbeta_next(Dbeta *unit, int inNumSamples)
{
	if (inNumSamples) {
		if (unit->m_repeats < 0.) {
			float x = DEMANDINPUT_A(0, inNumSamples);
			unit->m_repeats = sc_isnan(x) ? 0.f : floor(x + 0.5f);
		}
		if (unit->m_repeatCount >= unit->m_repeats) {
			OUT0(0) = NAN;
			return;
		}
		unit->m_repeatCount++;
		float lo = DEMANDINPUT_A(1, inNumSamples);
		float hi = DEMANDINPUT_A(2, inNumSamples);
		float a	 = DEMANDINPUT_A(3, inNumSamples);
		float b	 = DEMANDINPUT_A(4, inNumSamples);
				
		if(!sc_isnan(lo)) unit->lo = lo;
		if(!sc_isnan(hi)) unit->range = hi - lo; 
		if(!sc_isnan(a))  unit->a = a; 
		if(!sc_isnan(b))  unit->b = b; 
	
		OUT0(0) = BetaValue(a,b,unit) * unit->range + unit->lo;
	
		} else {
		unit->m_repeats = -1.f;
		unit->m_repeatCount = 0;
	}
}


void Dlinear_Ctor(Dlinear *unit)
{
	SETCALC(Dlinear_next);
	Dlinear_next(unit, 0);
	OUT0(0) = 0.f;
}


void Dlinear_next(Dlinear *unit, int inNumSamples)
{
	if (inNumSamples) {
		if (unit->m_repeats < 0.) {
			float x = DEMANDINPUT_A(0, inNumSamples);
			unit->m_repeats = sc_isnan(x) ? 0.f : floor(x + 0.5f);
		}
		if (unit->m_repeatCount >= unit->m_repeats) {
			OUT0(0) = NAN;
			return;
		}
		unit->m_repeatCount++;
		float lo = DEMANDINPUT_A(1, inNumSamples);
		float hi = DEMANDINPUT_A(2, inNumSamples);
		float fv = DEMANDINPUT_A(3, inNumSamples);
		
		if(!sc_isnan(lo)) unit->lo = lo;
		if(!sc_isnan(hi)) unit->range = hi - lo; 
		if(!sc_isnan(fv)) unit->favor = (int) fv;
				
		float x = unit->mParent->mRGen->frand();
		
		if (unit->favor == 0) x = 1.f - sqrt(x); // tendency to lower values
		else if (unit->favor == 1) x = sqrt(x);  // tendency to higher values
		
		OUT0(0) = x * unit->range + unit->lo;
	
	} else {
		unit->m_repeats = -1.f;
		unit->m_repeatCount = 0;
	}
}


void Dlogist_Ctor(Dlogist *unit)
{
	unit->x = DEMANDINPUT_A(4, 1);
	SETCALC(Dlogist_next);
	Dlogist_next(unit, 0);
	OUT0(0) = 0.f;
}


void Dlogist_next(Dlogist *unit, int inNumSamples)
{
	if (inNumSamples) {
		if (unit->m_repeats < 0.) {
			float x = DEMANDINPUT_A(0, inNumSamples);
			unit->m_repeats = sc_isnan(x) ? 0.f : floor(x + 0.5f);
		}
		if (unit->m_repeatCount >= unit->m_repeats) {
			OUT0(0) = NAN;
			return;
		}
		unit->m_repeatCount++;
		float lo = DEMANDINPUT_A(1, inNumSamples);
		float hi = DEMANDINPUT_A(2, inNumSamples);
		float lambda = DEMANDINPUT_A(3, inNumSamples);
		
		if(!sc_isnan(lo)) unit->lo = lo;
		if(!sc_isnan(hi)) unit->range = hi - lo; 
		if(!sc_isnan(lambda)) unit->lambda = lambda;
		
		float x = unit->x; 
		
		x = lambda * x * (1.0 - x); 
		
		OUT0(0) = x * unit->range + unit->lo;
		
		unit->x = x; 
		
	} else {
		unit->m_repeats = -1.f;
		unit->m_repeatCount = 0;
	}
}


void Dsine_Ctor(Dsine *unit)
{
	SETCALC(Dsine_next);
	Dsine_next(unit, 0);
	OUT0(0) = 0.f;
}

void Dsine_next(Dsine *unit, int inNumSamples)
{
	if (inNumSamples) {
		if (unit->m_repeats < 0.) {
			float x = DEMANDINPUT_A(0, inNumSamples);
			unit->m_repeats = sc_isnan(x) ? 0.f : floor(x + 0.5f);
		}
		if (unit->m_repeatCount >= unit->m_repeats) {
			OUT0(0) = NAN;
			return;
		}
		unit->m_repeatCount++;
		float lo = DEMANDINPUT_A(1, inNumSamples);
		float hi = DEMANDINPUT_A(2, inNumSamples);
			
		if(!sc_isnan(lo)) unit->lo = lo;
		if(!sc_isnan(hi)) unit->range = hi - lo; 
			
		float	x = unit->mParent->mRGen->frand();
				x = acos(x * 2 - 1) / pi;
	
		OUT0(0) = x * unit->range + unit->lo;
		
	} else {
		unit->m_repeats = -1.f;
		unit->m_repeatCount = 0;
	}
}

void Dgamma_Ctor(Dgamma *unit)
{
	SETCALC(Dgamma_next);
	Dgamma_next(unit, 0);
	OUT0(0) = 0.f;
}

void Dgamma_next(Dgamma *unit, int inNumSamples)
{
	if (inNumSamples) {
		if (unit->m_repeats < 0.) {
			float x = DEMANDINPUT_A(0, inNumSamples);
			unit->m_repeats = sc_isnan(x) ? 0.f : floor(x + 0.5f);
		}
		if (unit->m_repeatCount >= unit->m_repeats) {
			OUT0(0) = NAN;
			return;
		}
		
		unit->m_repeatCount++;
		
		float lo = DEMANDINPUT_A(1, inNumSamples);
		float hi = DEMANDINPUT_A(2, inNumSamples);
		int nu = DEMANDINPUT_A(3, inNumSamples);
				
		float range = hi - lo; 
		
		// pointer to the randomness functions. 
		RGen & rgen = *unit->mParent->mRGen; 
		
		// sum the random values to achieve a kind of convolution.  
		float gamma = 0;
		
		for (int n = 0; n < nu; n++) {
			gamma = (mclip(-1 * log(rgen.frand()*0.9999+0.0001),0,10.0) / 10) + gamma;
		};
		
		// divide again with mu to get back again in the range 0 - 1 
		gamma = gamma / nu;
		// scale into lo hi range
		gamma = gamma * range + lo; 
		OUT0(0) = gamma; 
		
	} else {
		unit->m_repeats = -1.f;
		unit->m_repeatCount = 0;
	}
}



void Dcauchy_Ctor(Dcauchy *unit)
{
		SETCALC(Dcauchy_next);
		Dcauchy_next(unit, 0);
		OUT0(0) = 0.f;
}
	
void Dcauchy_next(Dcauchy *unit, int inNumSamples)
{
	if (inNumSamples) {
		if (unit->m_repeats < 0.) {
		float x = DEMANDINPUT_A(0, inNumSamples);
		unit->m_repeats = sc_isnan(x) ? 0.f : floor(x + 0.5f);
		}
		if (unit->m_repeatCount >= unit->m_repeats) {
			OUT0(0) = NAN;
			return;
		}
		unit->m_repeatCount++;
		
		float lo = DEMANDINPUT_A(1, inNumSamples);
		float hi = DEMANDINPUT_A(2, inNumSamples);
		float param = DEMANDINPUT_A(3, inNumSamples);
		
		float range = hi - lo; 
		
		// pointer to the randomness functions. 
		RGen & rgen = *unit->mParent->mRGen; 
		
		if (param < 0.01) { param = 0.01; }; 
		
		// sum the random values to achieve a kind of convolution.  
		float value = 0;
		while (value > -0.001 && value < 0.001) {
			   value = rgen.frand2() * param; 
		};
	
		// process random number with the cumulative distribution function: 
		// parameter t is omitted in this case.
		value = 1.0 / (3.14159265359 * atan(value)); 
		// divide again with max offset to get in the range of -1 and 1. 
		// max offset: 1.0 / (pi * atan(0.001)) = 318		
		value = (value / 318.4) * 0.5 + 0.5; 
		OUT0(0) = value * range + lo; 
			
	} else {
		unit->m_repeats = -1.f;
			unit->m_repeatCount = 0;
	}
}


void Dlaplace_Ctor(Dlaplace *unit)
{
	SETCALC(Dlaplace_next);
	Dlaplace_next(unit, 0);
	OUT0(0) = 0.f;
}
	
void Dlaplace_next(Dlaplace *unit, int inNumSamples)
{
	if (inNumSamples) {
		if (unit->m_repeats < 0.) {
			float x = DEMANDINPUT_A(0, inNumSamples);
			unit->m_repeats = sc_isnan(x) ? 0.f : floor(x + 0.5f);
		}
		if (unit->m_repeatCount >= unit->m_repeats) {
			OUT0(0) = NAN;
			return;
		}
		unit->m_repeatCount++;
		
		float lo = DEMANDINPUT_A(1, inNumSamples);
		float hi = DEMANDINPUT_A(2, inNumSamples);
		float tau = DEMANDINPUT_A(3, inNumSamples);
		
		float range = hi - lo; 
		
		// pointer to the randomness functions. 
		RGen & rgen = *unit->mParent->mRGen; 
		
		// sum the random values to achieve a kind of convolution.  
		float value = rgen.frand() * 2.0; 
		
		if ( value > 1.0) { 
			value = -1 * tau * log(2.0 - value); }
		else {
			value = tau * log(value); 
		}; 
		
		OUT0(0) = value * range + lo; 
		
	} else {
		unit->m_repeats = -1.f;
		unit->m_repeatCount = 0;
	}
}




PluginLoad(Demand)
{
	ft = inTable;
	DefineSimpleUnit(Dcoin);
	DefineSimpleUnit(Dcoin2);
	DefineSimpleUnit(Dexpon);
	DefineSimpleUnit(Dexponential);
	DefineSimpleUnit(Dsumnrand);
	DefineSimpleUnit(Dbeta);
	DefineSimpleUnit(Dlinear);
	DefineSimpleUnit(Dlogist);
	DefineSimpleUnit(Dsine);
	DefineSimpleUnit(Dgamma);
	DefineSimpleUnit(Dcauchy);
	DefineSimpleUnit(Dlaplace);
}






