var
  register = IdentityDictionary.new,
  asuuid = { arg uuid;
    (uuid ?? {9999999999999.rand}).asSymbol;
  },
  get = { arg uuid;
    var p = register[uuid.asSymbol];
    if(p.isNil, {
      Error("Player not in registery:" + uuid.asCompileString).throw;
    });
    p
  },
  set = { arg uuid, player;
    uuid = uuid.asSymbol;
    register[uuid] = player;
    uuid
  };

(
  // creation
  new: { arg reply, uuid, klass ... args;
    var
      player = klass.asSymbol.asClass.new(*args);
    uuid = asuuid.(uuid);
    set.(uuid, player);
    reply.value(uuid);
  },
  load: { arg reply, uuid, path;
    var player = AbstractPlayer.load(path);
    uuid = asuuid.(uuid);
    set.(uuid, player);
    reply.value(uuid);
  },

  // list
  listRegister: { arg reply;
    reply.value(register.keys.as(Array));
  },
  freeAll: { arg reply;
    register.keysValuesDo({ arg k, v;
      v.free;
    });
    register = ();
    reply.()
  },

  // instance
  free: { arg reply, uuid;
    register[uuid].free;
    register.removeAt(uuid);
    reply.value();
  },
  play: { arg reply, uuid, atTime;
    get.(uuid).play(atTime: atTime);
    reply.value();
  },
  stop: { arg reply, uuid, atTime;
    get.(uuid).stop(atTime: atTime);
    reply.value();
  },
  isPlaying: { arg reply, uuid;
    reply.value(get.(uuid).isPlaying);
  },
  info: { arg reply, uuid;
    var p = get.(uuid);
    reply.value((
      name: p.name,
      path: p.path
    ));
  },
  gui: { arg reply, uuid;
    {
      get.(uuid).gui();
      reply.value();
    }.defer;
  },

  // not for remote calling.
  // this is for other apis to get a player object
  _get: { arg reply, uuid;
    reply.value(get.value(uuid));
  },
  _set: { arg reply, uuid, player;
    uuid = asuuid.value(uuid);
    reply.value(set.value(uuid, player));
  },
  prGetOrSet: { arg reply, player;
    var uuid = register.findKeyForValue(player);
    if(uuid.isNil, {
      uuid = asuuid.value(nil);
      set.value(uuid, player);
    });
    reply.value(uuid);
  },
  // curry a dict of handlers
  // of the form { arg player, arg1, arg2; }
  // into { arg reply, uuid, arg1, arg2; }
  // so that you can write simpler handlers
  withObject: { arg reply, e;
    var c = ();
    e.keysValuesDo({ arg name, fn;
      c[name] = { arg reply, uuid ... args;
        var object = get.value(uuid),
          result = fn.valueArray([object] ++ args);
        reply.value(result);
      };
    });
    reply.value(c)
  }
);
