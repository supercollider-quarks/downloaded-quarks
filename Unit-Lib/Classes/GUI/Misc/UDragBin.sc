UDragBin {
	
	classvar <>current;
	
	var <>view;
	var <>canReceiveDragHandler;
	var <>task;
	var <>color;
	
	*new { |parent, bounds|
		^super.new.view_( UserView( parent, bounds ) ).init;
	}
	
	init {
		color = Color.blue;
		view.canFocus_( false );
		view.canReceiveDragHandler_({ |vw, x,y|
			var last;
			if( x.notNil ) {
				last = current;
				current = vw;
				last !? _.refresh;
				vw.refresh;
			};
			canReceiveDragHandler.value( vw, x, y );
		});
		view.drawFunc = { |vw|
			if( View.currentDrag.notNil && {
				canReceiveDragHandler.value == true;
			}) {
				Pen.width = 2;
				if( current === vw ) {
					Pen.color = color.copy.alpha_(1);
				} {
					Pen.color = color.copy.alpha_(0.25);
				};
				Pen.roundedRect( vw.bounds.moveTo(0,0).insetBy(1,1), 3 );
				Pen.stroke;
				if( task.isPlaying.not ) {
					task = Task({
						while { vw.isClosed.not && {							canReceiveDragHandler.value == true
							} 
						} {
							0.25.wait;
						};
						if( vw.isClosed.not ) {
							vw.refresh;
						};
					}, AppClock).start;
				};
			};
		};
	}
	
	doesNotUnderstand { arg ... args;
		var result = view.perform( *args );
		^if( result === view, { this }, { result }); // be sure to replace view with base
	}
}