

/* 

Like SoundRepresentation but without the space. For internal sounds of the player

*/

FPSoundRepresentation : EntityRepresentation {

  var >input, >release = 0.2;
  var <nodeProxy;

  *new { arg  repManager, collisionFunc, input, release;
    ^super.new(repManager, collisionFunc)
          .input_(input)
          .release_(release);
  }

  init {
    super.init;
    release = release ?? {0.2};

    nodeProxy = NodeProxy(Server.default, 'audio', 2);
    nodeProxy.play;

    /* make some sound */
    this.add;

  }

  /* public */

  remove{
    Routine{
      //clear everything with given realease time
      nodeProxy.clear(release);
      //wait for the release to finish
      release.wait;
      repManager.remove(this);
      attached = false;
    }.play(TempoClock.default);
  }

  add {
      this.addSource; //Using JitLib the source will be added after the Server's default latency
      this.addAll(delay: Server.default.latency);
  }

  /* private */

  addSource{
      nodeProxy.source = { arg dt; var speedValue, in;

        dt = this.dt;

        speedValue = Control.names(\speed).kr(speed);
        speedValue = Ramp.kr(speedValue, dt);

        /* play default if input is not supplied */
        if(input == nil,
          {
            in = Impulse.ar(speedValue.linlin(0,10, 5, rrand(50, 200.0)));
            in = BPF.ar(in, rrand(2000, 18000.0)*rrand(0.3, 2.0), 0.4);
          },
          {in = input.value(speedValue)}
        );

        in;

      };
  }

  preUpdate{ arg theChanged, transPosition;
    /* set the syth with the new position values */
    nodeProxy.set('speed', speed);
  }

}

