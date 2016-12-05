/*
 SuperCollider real time audio synthesis system
 Copyright (c) 2002 James McCartney. All rights reserved.
 http://www.audiosynth.com
 
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

// CAUGens implemented by Yota Morimoto (http://yota.tehis.net/)
// v1 2010

void CML96_next(CML96 *unit, int inNumSamples)
{
	float smprt = ZIN0(0);

	float r = ZIN0(1);
	float g = ZIN0(2);
	float x[96];
	float counter[96];
	
	for(int i=0; i<96; i++){ x[i] = unit->x[i]; counter[i] = unit->counter[i]; }

	float spc;
	if(smprt < SAMPLERATE)
		spc = SAMPLERATE / sc_max(smprt, 0.001f);
	else spc = 1.f;

	for(int i=0; i<96; ++i){
		float *out = ZOUT(i);
		LOOP(inNumSamples,
			if(counter[i] >= spc){
				counter[i] -= spc;
				x[i] = (1 - g) * (1 - r * x[i] * x[i]) + (0.5 * g) * ((1 - r * x[sc_wrap(i+1, 0, 95)] * x[sc_wrap(i+1, 0, 95)]) + (1 - r * x[sc_wrap(i-1, 0, 95)] * x[sc_wrap(i-1, 0, 95)]));
			}
			counter[i]++;
			ZXP(out) = x[i];
		)
	}

	for(int i=0; i<96; i++){ unit->x[i] = x[i]; unit->counter[i] = counter[i]; }
}

void CML96_Ctor(CML96 *unit)
{
	SETCALC(CML96_next);
	for(int i=0; i<96; i++){ unit->x[i] = i * (1.0/97.f); unit->counter[i] = 0.f; }
	CML96_next(unit, 1);
}