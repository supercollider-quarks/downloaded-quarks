TaskUMapDef : UMapDef {
	
	classvar <>activeUnits;
	
	var <>valueIsMapped = true;
	var <>taskFunc;
	
	*initClass {
		activeUnits = IdentitySet();
		CmdPeriod.add( this );
	}
	
	*cmdPeriod {
		activeUnits = IdentitySet();
	}
	
	*new { |name, taskFunc, args, category, addToAll=true|
		^this.basicNew( name, args ? [], addToAll )
			.initFunc( taskFunc ).category_( category ? \default ); 
	}
	
	initFunc { |inTaskFunc|
		taskFunc = inTaskFunc;
		argSpecs = ([
			[ \value, 0, ControlSpec(0,1) ], 
			[ \u_task, nil, AnythingSpec(), true ], // func can store things here
			[ \u_release_task, nil, AnythingSpec(), true ],
			[ \u_dur, 1, ControlSpec(0,inf), true ],
			[ \u_spec, [0,1].asSpec, ControlSpecSpec(), true ],
		] ++ argSpecs).collect(_.asArgSpec);
		argSpecs.do(_.mode_( \init ));
		this.setSpecMode( \value, \nonsynth );
		mappedArgs = [ \value ];
		allowedModes = [ \sync, \normal ];
		this.canUseUMap = false;
		this.changed( \init );
	}
	
	makeSynth { |unit, target, startPos = 0, synthAction|
		if( unit.u_task.isPlaying.not ) {
			unit.u_task.start;
		};
		if( unit.u_release_task.isPlaying.not && unit.u_release_task.notNil  ) {
			unit.u_release_task.start;
		};
		^nil 
	}
	
	prepare { |servers, unit, action, startPos|
		var task;
		if( unit.u_task.isPlaying.not ) {
			task = Task({
				taskFunc.value( unit, unit.getDur, startPos );
			});
			unit.set( \u_task, task );
			if( unit.getDur != inf ) {
				unit.u_release_task = Task({
					(( unit.getDur ? 1 ) - startPos).max(0).wait;
					task.stop;
				});
			} { 
				unit.u_release_task = nil 
			};
		};
		action.value;
	}
	
	needsPrepare { ^true }
	
	stop { |unit|
		unit.get( \u_task ).stop;
		unit.get( \u_release_task ).stop;
	}
	
	hasBus { ^false }
	
	isMappedArg { |name|
		if( name == \value ) {
			^valueIsMapped;
		} {
			^mappedArgs.notNil && { mappedArgs.includes( name ) };
		};
	}
	
	value { |unit|
		if( valueIsMapped ) {
			^(unit.get( \u_spec ) ?? { [0,1].asSpec }).map( 
				unit.getSpec( \value ).unmap( unit.value )
			);
		} {
			^unit.value
		};
	}
	
	setSynth { |unit ...keyValuePairs|
		keyValuePairs.clump(2).do({ |item|
			switch( item[0],
				 \value, { if( unit.unit.notNil ) { 
					 //unit.unit.synthSet( unit.unitArgName, unit ); 
					 unit.unitSet;
				} },
			)
		});
	}
}