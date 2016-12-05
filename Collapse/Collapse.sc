/**
 *	(C)opyright 2006-2007 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	The Collapse class is useful for deferring actions to a certain clock
 *	or scheduling them while reducing system load to a minimum. The Collapse
 *	takes a function to be deferred, a delta time span and a clock
 *	to defer to. An action is deferred by calling the defer method with
 *	arbitrary arguments. The function's value method is called with these
 *	arguments after the schedule delay. When defer is called before the
 *	function was executed, the function is deferred again by the schedule delay
 *	and the pending call is cancelled. The new arguments overwrite the previous
 *	(pending) arguments.
 *
 *	An application example: responding to a MIDI
 *	controller while allowing the user to adjust the MIDI dial within a 100ms
 *	window for example. The function is deferred until no more controller updates
 *	occur for the given delta time of 100ms. Continuous rotations will update the
 *	arguments (the controller value) while postponing the function until the
 *	user releases the dial.
 *
 *	Class dependancies:
 *
 *	Changelog:
 *		31-Mar-06		former defer method is renamed to listDefer, former defer2 method becomes defer !
 *		16-Jun-06		added cancel + reschedule methods
 *		30-Jan-07		removed TypeSafe calls ; added instantaneous ; uses thisThread.seconds
 *
 *	@version	30-Jan-07
 *	@author	Hanns Holger Rutz
 */
Collapse : Object
{
	/**
	 *	the arguments passed to defer
	 */
	var <args;
	
	/**
	 *	the deferred function
	 */
	var <func;
	
	/**
	 *	the scheduling delta time
	 */
	var <delta;
	
	/**
	 *	the clock to execute the function within
	 */
	var <clock;
	
	/**
	 *	true if the function was deferred.
	 *	after the function is execute, the started
	 *	value is reset to false
	 */
	var <started	= false;
	
	/**
	 *	true if the cancel was called.
	 *	reset to false when reschedule is called
	 */
	var <cancelled = false;
	
	var execTime, collapseFunc;

	/**
	 *	Creates a new Collapse.
	 *
	 *	@param	func		the function to execute when deferring; nil is allowed
	 *	@param	delta	the amount of time to defer in seconds, defaults to 0.0
	 *	@param	clock	the clock to execute the function within, defaults to AppClock
	 */
	*new { arg func, delta = 0.0, clock;
		^super.new.prInit( func, delta, clock );
	}
	
	prInit { arg argFunc, argDelta, argClock;
		func			= argFunc;
		delta 		= argDelta;
		clock		= argClock ? AppClock;
		
//		TypeSafe.checkArgClasses( thisMethod,
//			[ func, delta, clock ], [ Function, Number, Meta_Clock ], [ true, false, false ]);
		
		collapseFunc	= {
			var now;
			if( cancelled.not, {
				now = Main.elapsedTime;
				if( now < execTime, {	// too early, reschedule
//					clock.sched( execTime - now + 0.01, collapseFunc );
					execTime - now; // + 0.001; why was this extra delay originally needed? XXX
				}, {					// ok, execute function
					func.valueArray( args );
					started = false;
					nil;
				});
			}, {
				started = false;
				nil;
			});
		};
	}
	
	cancel {
		cancelled = true;
	}
	
	/**
	 *	(Re)schedules the function for execution
	 *	with the given list of arguments.
	 *
	 *	@param	args		zero or more arguments which are passed to the function upon execution
	 */
	defer { arg ... args;
		if( cancelled.not, {
			this.prSetArgs( args );
			this.reschedule;
		});
	}
	
	value {
		|...args|
		this.defer(*args);
	}
	
	/**
	 *	Resets the scheduling delay to the original delta.
	 *	If the collapse was not yet scheduled, this method will do it.
	 *	The cancel status is cleared.
	 */
	reschedule {
		execTime	= Main.elapsedTime + delta;
		if( started.not, {
			started		= true;
			cancelled		= false;
			clock.sched( delta, collapseFunc );
		});
	}

	/**
	 *	Similiarly to defer, this sets the function
	 *	args and schedules the collapse if it hadn't been
	 *	started. Unlike defer, the scheduling delay is
	 *	not reset.
	 *
	 *	@param	args		zero or more arguments which are passed to the function upon execution
	 */
	instantaneous { arg ... args;
		if( started.not, {
			this.defer( *args );
		}, {
			this.prSetArgs( args );
		});
	}
	
	/**
	 *	(Re)schedules the function for execution
	 *	with the arguments provided as an array.	
	 *
	 *	@param	args		an array of zero or more arguments which are passed to the function upon execution
	 */
	listDefer { arg args;
		if( cancelled.not, {
			this.prSetArgs( args );
			this.reschedule;
		});
	}
	
	/**
	 *	Similiarly to defer, this sets the function
	 *	args and schedules the collapse if it hadn't been
	 *	started. Unlike defer, the scheduling delay is
	 *	not reset.
	 *
	 *	@param	args		an array of zero or more arguments which are passed to the function upon execution
	 */
	listInstantaneous { arg args;
		if( started.not, {
			this.listDefer( args );
		}, {
			this.prSetArgs( args );
		});
	}

	// ------------- private -------------

	prSetArgs { arg argArgs;
		args = argArgs;
	}
}
