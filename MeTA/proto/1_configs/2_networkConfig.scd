"\n**********  Network Config:  **********\n".postln;

m.config.myIPs = OSCExplorer.getMyIPs;
NetAddr.broadcastFlag_(true);
if (NetAddr.langPort != 57120) {
	m.warnWin(
		"NetAddr.langPort\nshould be 57120,\nbut is %!".format(NetAddr.langPort)
		++ "\nBetter reboot SC.",
		"NetAddr.langPort is wrong!");
};

m.config.broadcastAddr = NetAddr("255.255.255.255", NetAddr.langPort);

"network setup : \n"
"\tIP addresses : % \n".postf(m.config.myIPs);
"\tNetAddr.langPort : % \n".postf(NetAddr.langPort);
"\tq.broad : % \n".postf(m.config.broadcastAddr);
"\n********************\n".postln;

nil; // return value
