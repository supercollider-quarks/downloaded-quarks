/*
  from:
        http://brain.cc.kogakuin.ac.jp/~kanamaru/Chaos/e/
  2d:
  Pnagumo
  Prossler
  Pduffing
  parametric pendulum
  various pendulums
  double pendulum
  extensible pendulm
  brain dynamics
*/

Plogist : Pattern {
	var <>r, <>xi, <>length;

	*new {| r=1.5, xi=0.1, length=inf |
		^super.newCopyArgs(r, xi, length);
	}

	embedInStream {|inval|
		var xn = xi;
		var rStrm = r.asStream;
		var rVal;
		length.do({
			rVal = rStrm.next(inval);
			if(rVal.isNil, { ^inval });
			xn = 1.0 - (rVal * xn.squared);
			inval = xn.yield;
		});
		^inval;
	}
}

Pcml : ListPattern {
	var <>r, <>g;

	*new {| list, r=1.5, g=0.1, repeats=inf |
		^super.new(list, repeats).r_(r).g_(g);
	}

	f {|r,x| ^1.0 - (r * x.squared) }//logistic

	evolve {| prev, r, g |
		var next = Array.newClear(prev.size), halfG = g * 0.5;
		prev.size.do({|i|
			next[i] = ((1.0 - g) * this.f(r, prev[i]))
						+ (halfG * (this.f(r, prev.wrapAt(i+1)) + this.f(r, prev.wrapAt(i-1))));
		});
		^next;
	}

	embedInStream {|inval|
		var items = list.copy;
		var rStrm = r.asStream, gStrm = g.asStream;
		var rVal, gVal;
		repeats.do({
			rVal = rStrm.next(inval);
			gVal = gStrm.next(inval);
			if (rVal.isNil || gVal.isNil, { ^inval });
			items = this.evolve(items, rVal, gVal);
			inval = items.yield;
		});
		^inval;
	}

	plot {| n=500 |
		var rct, cell, stream = this.asStream, strVal;
		var win;
		rct = Rect(0, 0, list.size*2, n*2);
		win = Window("r: " ++ r ++ " g: " ++ g ++ " size: " ++ list.size ++ "x" ++ n, rct, false);
		win.view.background = Color.black;
		win.drawFunc = {
			n.do {|i|
				strVal = stream.next;
				strVal.do {|item,j|
					Pen.fillColor = Color.gray(item);
					Pen.fillRect(Rect(j*2, i*2, 2, 2));
				};
			};

		};
		win.front;
		CmdPeriod.doOnce { win.close };
	}
}

Pgcm : Pcml {

	evolve {| prev, r, g |
		var next = Array.newClear(prev.size), nG = g / prev.size, sum = 0;
		prev.do {| item, i | sum = sum + this.f(r, item) };
		prev.do {| item, i | next[i] = ((1.0 - g) * this.f(r, item)) + (nG * sum) };
		^next;
	}
}

// Plorenz1 : Pattern {
// 	var <>h, <>s, <>r, <>b, <>xi, <>yi, <>zi, <>length;
// 	//s: the fluid viscosity of a substance to its thermal conductivity
// 	//r: the difference in temperature between the top and bottom of the gaseous system.
// 	//b: the width to height ratio of the box which is being used to hold the gas
//
// 	*new {| h=0.01, s=10, r=28, b=2.667, xi=1, yi=0, zi=0, length=inf |
// 		^super.newCopyArgs(h, s, r, b, xi, yi, zi, length);
// 	}
//
// 	embedInStream {|inval|
// 		var xn = xi, yn = yi, zn = zi;
// 		var sStrm = s.asStream, rStrm = r.asStream, bStrm = b.asStream;
// 		var sVal, rVal, bVal;
// 		length.do {
// 			sVal = sStrm.next(inval);
// 			rVal = rStrm.next(inval);
// 			bVal = bStrm.next(inval);
// 			if (sVal.isNil || rVal.isNil || bVal.isNil, { ^inval });
// 			xn = xn + (h * sVal * (yn - xn));
// 			yn = yn + (h * (xn * (rVal - zn) - yn));
// 			zn = zn + (h * ((xn * yn) - (bVal * zn)));
// 			inval = [xn, yn, zn].yield;
// 		};
// 		^inval;
// 	}
// }
// coupled lorenz
// Plorenz2 : Pattern {
// 	var <>h, <>g, <>s, <>r, <>b, <>xi, <>yi, <>zi, <>length;
//
// 	*new {| h=0.01, g=3.82, s=10, r=28, b=2.667, xi=#[1,1], yi=#[0,0], zi=#[0,0], length=inf |
// 		^super.newCopyArgs(h, g, s, r, b, xi, yi, zi, length);
// 	}
//
// 	embedInStream {|inval|
// 		var xn0 = xi[0], yn0 = yi[0], zn0 = zi[0];
// 		var xn1 = xi[1], yn1 = yi[1], zn1 = zi[1];
// 		var gStrm = g.asStream, sStrm = s.asStream, rStrm = r.asStream, bStrm = b.asStream;
// 		var gVal, sVal, rVal, bVal;
// 		length.do {
// 			gVal = gStrm.next(inval);
// 			sVal = sStrm.next(inval);
// 			rVal = rStrm.next(inval);
// 			bVal = bStrm.next(inval);
// 			if (gVal.isNil || sVal.isNil || rVal.isNil || bVal.isNil, { ^inval });
// 			xn0 = xn0 + (h * ((sVal * (yn0 - xn0)) + (gVal * (xn1 - xn0))));
// 			yn0 = yn0 + (h * (xn0 * (rVal - zn0) - yn0));
// 			zn0 = zn0 + (h * ((xn0 * yn0) - (bVal * zn0)));
// 			xn1 = xn1 + (h * ((sVal * (yn1 - xn1)) + (gVal * (xn0 - xn1))));
// 			yn1 = yn1 + (h * (xn1 * (rVal - zn1) - yn1));
// 			zn1 = zn1 + (h * ((xn1 * yn1) - (bVal * zn1)));
// 			inval = [xn0, yn0, zn0, xn1, yn1, zn1].yield;
// 		};
// 		^inval;
// 	}
// }