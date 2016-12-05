m.views.pxmix.isNil.if({
	m.views.pxmix = NdefMixer(m.server, numItems: 16);
	m.views.pxmix.skipjack.dt = 0.05;
	m.views.pxmix.moveTo(5, 5);
	m.views.pxmix.parent.onClose = {m.views.pxmix = nil};	// remove traces when closed
	ProxyMeter.addMixer(m.views.pxmix);
}, {
	m.views.pxmix.parent.front;
})


