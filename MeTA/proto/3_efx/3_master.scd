/////make MAster FX

(
q.efx.master = MasterFX.new(server: s,
	numChannels: q.numChans,
	slotNames: [\in, \dbComp, \ring, \ampFin], // order is important here.
	busIndex: 0
);

// pre-set ons:

q.efx.master.pxChain.add('in', 1);
q.efx.master.pxChain.add('dbComp', 1);
q.efx.master.pxChain.add('ampFin', 1);


q.efx.window = q.efx.master.gui('masterFX', 14); // adjust number of visible slots here
q.efx.window.moveTo(790, 1200);
);

// tweak
q.efx.master.pxChain.proxy.fadeTime = 3;
// add Panic Button?
/*
q.efx.master .pxChain . dump
q.efx.master .pxChain . proxy.dump

MasterFX.clear('localhost')
*/


