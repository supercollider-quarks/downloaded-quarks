(
m = MPush(\push);

q = ();
q.dt = 0.055003051944088;
m.elementAt(\ribbon, \bend).action = {|el|
	q.dt = el.value.linexp(0, 1, 0.005, 0.1);
};

q.taskPlaying = true;
Tdef(\player).quant = 0;
m.elementAt(\btCtl, \play).action = {|el|
	(el.value > 0).if{
		q.taskPlaying = q.taskPlaying.not;
		el.value = q.taskPlaying.asInteger;
		q.taskPlaying.if({
			Tdef(\player).resume;
		},{
			Tdef(\player).pause;
		})
	}

};

m.elementAt(\btCtl, \record).action = {|el|
	(el.value > 0).if{
		Tdef(\player).stop.play.pause;
		q.taskPlaying = false;
		m.elementAt(\btCtl, \play).deviceValue = 0;
		q.allElements.do(_.deviceValue_(0));
	}
};
m.elementAt(\btCtl, \record).deviceValue = 128;

q.allElements = 8.collect{|i|
	m.elementAt(\pad, \on, i)
}.flat;

Tdef(\player, {
	inf.do{|j|
		q.color = MPush.lightDict.padColor.keys.choose;
		q.intensity = [\dim, \full][j%2];
		q.allElements.scramble.do{|el|
			el.deviceValue_(MPush.padLightCode(intensity: q.intensity, color: q.color));
			q.dt.wait;
		}
	}
});
Tdef(\player).play.pause
)


