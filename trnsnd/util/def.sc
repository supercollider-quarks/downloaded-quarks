// resource management
Def {
	var <key;
	classvar <>all;

	*initClass { all = IdentityDictionary.new }

	*new { |key val|
		var b = all[key];
		if(b.isNil, {
			all[key] = val;
			^all[key]
		}, {
			if(val.notNil, {
				all[key] = val;
				^all[key]
			});
		});
		^b
	}
}
Cdef {
	var <key;
	classvar <>all;

	*initClass { all = IdentityDictionary.new }

	*clear { all.do(_.free)	}

	*new { |key n|
		var k = all[key];
		if(k.isNil, {
			all[key] = Bus.control(Server.default, 1);
			^all[key]
		}, {
			if(n.notNil, {
				all[key].free;
				all[key] = Bus.control(Server.default, n);
				^all[key]
			});
		});
		^k
	}
}
Adef {
	var <key;
	classvar <>all;

	*initClass { all = IdentityDictionary.new }

	*clear { all.do(_.free)	}

	*new { |key n|
		var a = all[key];
		if(a.isNil, {
			all[key] = Bus.audio(Server.default, 1);
			^all[key]
		}, {
			if(n.notNil, {
				all[key].free;
				all[key] = Bus.audio(Server.default, n);
				^all[key]
			});
		});
		^a
	}
}

Bdef {
	var <key;
	classvar <>all;

	*initClass { all = IdentityDictionary.new }

	*clear {
		all.do { |v|
			if(v.isKindOf(SequenceableCollection), {
				v.do(_.free)
			}, {
				v.free
			});
		};
		this.initClass;
	}
	*dir { |key file|
		var b = all[key];
		if(b.isNil, {
			all[key] = Buffer.read(Server.default, F.dir +/+ file);
			^all[key]
		}, {
			if(file.notNil, {
				all[key].free;
				all[key] = Buffer.read(Server.default, F.dir +/+ file);
				^all[key]
			});
		});
		^b
	}
	*new { |key nframes nchannels|
		var b = all[key];
		if(b.isNil, {
			all[key] = Buffer.alloc(Server.default, nframes, nchannels);
			^all[key]
		}, {
			if(nframes.notNil, {
				all[key].free;
				all[key] = Buffer.alloc(Server.default, nframes, nchannels);
				^all[key]
			});
		});
		^b
	}
	*read { |key path|
		var b = all[key];
		if(b.isNil, {
			all[key] = Buffer.read(Server.default, path);
			^all[key]
		}, {
			if(path.notNil, {
				all[key].free;
				all[key] = Buffer.read(Server.default, path);
				^all[key]
			});
		});
		^b
	}
	*channel { |key path channels|
		var b = all[key];
		if(b.isNil, {
			all[key] = Buffer.readChannel(Server.default, path, channels: channels);
			^all[key]
		}, {
			if(path.notNil, {
				all[key].free;
				all[key] = Buffer.readChannel(Server.default, path, channels: channels);
				^all[key]
			});
		});
		^b
	}
	*vosc { |key n nframes|
		var b = all[key];
		if(b.isNil, {
			all[key] = Buffer.allocConsecutive(n, Server.default, nframes);
			^all[key]
		}, {
			if(n.notNil, {
				all[key].free;
				all[key] = Buffer.allocConsecutive(n, Server.default, nframes);
				^all[key]
			});
		});
		^b
	}
}