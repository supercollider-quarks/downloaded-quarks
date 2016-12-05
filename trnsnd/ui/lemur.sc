Lemur {
	classvar <addr;
	classvar acc, white=8355711, blue=3489356, dark=2500134;
	*iPhone6 { |xL yL xyLOn xyLOff xR yR xyROn xyROff|
		if(addr.isNil, { addr = NetAddr("yPhone6.local", 8000) });
		// L
		xL=xL.asSpec;
		yL=yL.asSpec;
		xyLOn=xyLOn?{"xyL: on".postln};
		xyLOff=xyLOff?{"xyL: off".postln};
		OSCFunc({ |msg| Cdef(\xL).set(xL.map(msg[1])) }, '/xyL/x');
		OSCFunc({ |msg| Cdef(\yL).set(yL.map(msg[1])) }, '/xyL/y');
		OSCFunc({ |msg| if(msg[1]==1, xyLOn, xyLOff) }, '/xyL/z');
		// R
		xR=xR.asSpec;
		yR=yR.asSpec;
		xyROn=xyROn?{"xyR: on".postln};
		xyROff=xyROff?{"xyR: off".postln};
		OSCFunc({ |msg| Cdef(\xR).set(xL.map(msg[1])) }, '/xyR/x');
		OSCFunc({ |msg| Cdef(\yR).set(yL.map(msg[1])) }, '/xyR/y');
		OSCFunc({ |msg| if(msg[1]==1, xyROn, xyROff) }, '/xyR/z');
	}
}