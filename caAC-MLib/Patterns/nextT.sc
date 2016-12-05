
+ Stream {
	nextT { arg n = 100, iDur = 2.2675736961451e-05,inval,action;
		var return = Array.new;
		r {
			n do: { return = return.add(this.next(inval)); iDur.wait; };
			wait(n*iDur);
			{ action.(return); }.defer;
		}.play;

	}
}

/*
Pseg(Pseq([0,1]),Pseq([200]*(1/44100))).asStream.nextT(200,action:{|array| ~temp=array.plot});
*/