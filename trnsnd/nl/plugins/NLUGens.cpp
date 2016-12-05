/*
 NLUGens by yota morimoto (http://yota.tehis.net/)
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

#include "SC_PlugIn.h"

#define LATTICE 10
#define GENEBIT 16

static InterfaceTable *ft;

struct Logist : public Unit {
	double x;
	float counter;
};
struct Nagumo : public Unit {
	double u, v;
};
struct FIS : public Unit {
};
struct CML : public Unit {
	double x[LATTICE];
	float counter;
};
struct GCM : public Unit {
	double x[LATTICE];
	float counter;
};
struct HCM : public Unit {
	unsigned short x[GENEBIT];
	float counter;
};
struct TLogist : public Logist {
	double trig;
};

extern "C" {
	void Logist_next(Logist *unit, int inNumSamples);
	void Logist_Ctor(Logist *unit);
	void Nagumo_next(Nagumo *unit, int inNumSamples);
	void Nagumo_Ctor(Nagumo *unit);
	void FIS_next(FIS *unit, int inNumSamples);
	void FIS_Ctor(FIS *unit);
	void CML_next(CML *unit, int inNumSamples);
	void CML_Ctor(CML *unit);
	void GCM_next(GCM *unit, int inNumSamples);
	void GCM_Ctor(GCM *unit);
	void HCM_next(HCM *unit, int inNumSamples);
	void HCM_Ctor(HCM *unit);
	void TLogist_next(TLogist *unit, int inNumSamples);
	void TLogist_Ctor(TLogist *unit);
}

inline double logist(double r, double x);
inline double logist(double r, double x)
{
	return 1.l - r * x * x;
}

void Logist_next(Logist *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float freq = ZIN0(0);
	double r = ZIN0(1);
	
	double x = unit->x;
	float counter = unit->counter;
	
	float spc;
	if(freq < SAMPLERATE)
		spc = SAMPLERATE / sc_max(freq, 0.001f);
	else spc = 1.f;
	
	LOOP(inNumSamples,
		 if(counter >= spc){
			 counter -= spc;
			 x = logist(r, x);
		 }
		 counter++;		
		 ZXP(out) = x;
		 )
	unit->x = x;
	unit->counter = counter;
}

void Logist_Ctor(Logist *unit)
{
	SETCALC(Logist_next);
	unit->x = IN0(2);
	unit->counter = 0.f;
	Logist_next(unit, 1);
}


void Nagumo_next(Nagumo *unit, int inNumSamples)
{
	float *out = ZOUT(0);

	double uh = ZIN0(0);
	double vh = ZIN0(1);
	float *pulse = ZIN(2);
		
	double u = unit->u;
	double v = unit->v;
	
	LOOP(inNumSamples,
		float zPulse = ZXP(pulse);
		u += uh * (10.l * (- v + u - 0.3333333l*u*u*u + zPulse));
		v += vh * (u - 0.8l * v + 0.7l);
		ZXP(out) = u * 0.3;
	)
	unit->u = u;
	unit->v = v;
}

void Nagumo_Ctor(Nagumo *unit)
{
	SETCALC(Nagumo_next);
	unit->u = 0.1l;
	unit->v = 0.l;
	Nagumo_next(unit, 1);
}

void FIS_next(FIS *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *r = ZIN(0);
	float *x = ZIN(1);
	int n = ZIN0(2);

	LOOP(inNumSamples,
		double zx = ZXP(x);
		double zr = ZXP(r);
		for(int i=0; i<n; i++)
			zx = sin(zr * zx);
		ZXP(out) = zx;
	)	
}

void FIS_Ctor(FIS *unit)
{
	SETCALC(FIS_next);
	FIS_next(unit, 1);
}

void CML_next(CML *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float freq = ZIN0(0);
	double r = ZIN0(1);
	double g = ZIN0(2);
	double x[LATTICE];
//	double t[LATTICE];

	memcpy(x, unit->x, sizeof(unit->x));
	float counter = unit->counter;

	float spc;
	float slope;
	if(freq < SAMPLERATE){
		spc = SAMPLERATE / sc_max(freq, 0.001f);
		slope = 1.f / spc;
	} 
	else spc = slope = 1.f;

	LOOP(inNumSamples,
		if(counter >= spc){
			counter -= spc;
			for (int i=1; i<LATTICE-1; i++) {
				// x[i] = x[i] rather than new = old
				// or wrapping? sc_wrap(i-1, 0, MAXWIDTH-1)
				x[i] = (1.l - g) * logist(r, x[i]) + 0.5 * g * (logist(r, x[i+1]) + logist(r, x[i-1]));//no wrapping
				// t = x then x = t
				//t[i] = (1.l - g) * logist(r, x[i]) + 0.5 * g * (logist(r, x[i+1]) + logist(r, x[i-1]));//no wrapping
			}
			//memcpy(x, t, sizeof(t));
		}
		counter++;
		//ZXP(out) = t[5];
		 ZXP(out) = x[5];
	)
	unit->counter = counter;
	memcpy(unit->x, x, sizeof(x));
}

void CML_Ctor(CML *unit)
{
	SETCALC(CML_next);
	for (int i=0; i<LATTICE; i++) unit->x[i] = IN0(3);
	unit->counter = 0.f;
	CML_next(unit, 1);
}


void GCM_next(GCM *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float freq = ZIN0(0);
	double r = ZIN0(1);
	double g = ZIN0(2);
	double x[LATTICE];
	double reciprocal = 1.l / LATTICE;

	memcpy(x, unit->x, sizeof(unit->x));
	float counter = unit->counter;

	float spc;
	if(freq < SAMPLERATE)
		spc = SAMPLERATE / sc_max(freq, 0.001f);
	else spc = 1.f;
	
	double sum = 0;
	for (int i=0; i<LATTICE; i++) sum += logist(r, x[i]);//in theory should be in LOOP
	
	LOOP(inNumSamples,
		if(counter >= spc){
			counter -= spc;
			for (int i=0; i<LATTICE; i++)
				// x[i] = x[i] rather than new = old
				x[i] = (1.l - g) * logist(r, x[i]) + g * reciprocal * sum;
		}
		counter++;
		ZXP(out) = x[5];
	)
	unit->counter = counter;
	memcpy(unit->x, x, sizeof(x));
}

void GCM_Ctor(GCM *unit)
{
	SETCALC(GCM_next);
	for (int i=0; i<LATTICE; i++) unit->x[i] = IN0(3);
	unit->counter = 0.f;
	GCM_next(unit, 1);
}


inline unsigned flip(unsigned x, unsigned bit);
inline unsigned flip(unsigned x, unsigned bit)
{
	return x ^ (1UL << bit);
}
inline double i2f(unsigned short s);
inline double i2f(unsigned short s)
{
	return s / 32768.l - 1.l;
}
inline unsigned short f2i(double f);
inline unsigned short f2i(double f)
{
	return (unsigned short)(f * 32768.l + 32768.l);
}
void HCM_next(HCM *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float freq = ZIN0(0);
	double r = ZIN0(1);
	double g = ZIN0(2);
	unsigned short x[GENEBIT];
	double reciprocal = 1.l / GENEBIT;
	
	memcpy(x, unit->x, sizeof(unit->x));
	float counter = unit->counter;
	
	float spc;
	if(freq < SAMPLERATE)
		spc = SAMPLERATE / sc_max(freq, 0.001f);
	else spc = 1.f;
	
	double sum = 0;
	double tmp;
	
	LOOP(inNumSamples,
		 if(counter >= spc){
			 counter -= spc;
			 for (int i=0; i<GENEBIT; i++){
				 for (int j=0; j<GENEBIT; j++) sum += logist(r, i2f(flip(x[j], i)));
				 tmp = (1.l - g) * logist(r, i2f(x[i])) + g * reciprocal * sum;
				 x[i] = f2i(tmp);
			 }
		 }
		 counter++;
		 ZXP(out) = i2f(x[4]);
	)
	memcpy(unit->x, x, sizeof(x));
	unit->counter = counter;
}

void HCM_Ctor(HCM *unit)
{
	SETCALC(HCM_next);
	for (int i=0; i<GENEBIT; i++) unit->x[i] = 1;
	unit->counter = 0.f;
	HCM_next(unit, 1);
}


void TLogist_next(TLogist *unit, int inNumSamples)
{	
	float trig = ZIN0(2);
	if (trig > 0.f && unit->trig <= 0.f) {
		double r = ZIN0(0);
		ZOUT0(0) = unit->x = 1.f - r * unit->x * unit->x;
	} else {
		ZOUT0(0) = unit->x;
	}
	unit->trig = trig;
}

void TLogist_Ctor(TLogist *unit)
{	
	double r = ZIN0(0);
	ZOUT0(0) = unit->x = ZIN0(1);
	SETCALC(TLogist_next);
	unit->trig = ZIN0(2);
}

PluginLoad(NL)
{
	ft = inTable;
	DefineSimpleUnit(Logist);
	DefineSimpleUnit(Nagumo);
	DefineSimpleUnit(FIS);
	DefineSimpleUnit(CML);
	DefineSimpleUnit(GCM);
	DefineSimpleUnit(HCM);
	DefineSimpleUnit(TLogist);
}
