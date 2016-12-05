// a

a = ();
b = WatchList.envir(a);

a.put(\x, 1);
b.list;
a.put(\y, 1);
b.list;
a.put(\x, nil);  // keep old items in list
b.list;
a.put(\a, 123);
b.list;

Ndef(\a);
c = WatchList.proxy(Ndef(\a));
c.list;
Ndef(\a).set(\freq, 234);
c.list;
Ndef(\a, { SinOsc.ar(\freq.kr) * \amp.kr(0.2) }).play;
c.list;
Ndef(\a, { \xyz.kr; SinOsc.ar(\freq.kr) * \amp.kr(0.2) }).play;
c.list;

p = ProxySpace.push(s);
d = WatchList.spaceAr(p);
e = WatchList.spaceKr(p);
p.getHalo;

~a.ar;
d.list;
~z.ar;
d.list;
~b.ar;
d.list;
~k.kr;
e.list;

f = WatchList.proxy(Tdef(\a));
Tdef(\a).set(\xyz, 123);
f.list;
Tdef(\a).set(\abc, 234);
f.list;
Tdef(\a).set(\xyz, nil);
f.list; // missing name remains until
f.clear.update;
f.list;

// test with update by hand, no skipjack:
b = WatchList.envir(a, skip: false);
a.put(\x, 1); b.update.list;
a.put(\y, 1); b.update.list;
a.put(\x, nil); b.update.list;
a.put(\a, 123); b.update.list;

b.makeSkip;
a.put(\b, 123); b.list;
