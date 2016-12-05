
PrePostView {
	classvar <>preCol, <>postCol, <>preSatCol, <>satCol;
	var <uv, <preAmp = 0, <postAmp = 0;

	*initClass {
		preCol = Color.yellow(1.0, 0.4);
		postCol = Color.green(0.7, 0.4);
		preSatCol = Color.red(1, 0.4);
		satCol = Color.red(1, 0.6);
	}

	*new { |parent, bounds|
		^super.new.init(parent, bounds);
	}

	*forMonitor { |monitorGui|
		var sliderZone = monitorGui.zone.children[0];
		var slider = sliderZone.children[1];

		^this.new(sliderZone, slider.bounds);
	}

	init { |parent, bounds|
		uv = UserView(parent, bounds);
		uv.background_(Color(1,1,1,0));
		uv.acceptsMouse_(false);

		// horizontal for now:
		uv.drawFunc = { |uv|
			var bounds = uv.bounds;
			var maxwid = bounds.width - 4;
			var height = bounds.height - 4;
			var satPreLeft, satLeft;

			if (preAmp > 0) {
				Pen.color_(preCol);
				Pen.addRect(Rect(2,2, preAmp * maxwid, height));
				Pen.fill;
				if (preAmp > 1.0) {
					Pen.color_(preSatCol);
					satPreLeft = (1 - (preAmp - 1).clip(0, 1)) * maxwid;
					Pen.addRect(Rect( satPreLeft, 2, maxwid, height));
					Pen.fill;
				};
			};

			if (postAmp > 0) {
				Pen.color_(postCol);
				Pen.addRect(Rect(2,2, postAmp * maxwid, height));
				Pen.fill;
				if (postAmp > 1.0) {
					Pen.color_(satCol);
					satLeft = (1 - (postAmp - 1).clip(0, 1)) * maxwid;
					Pen.addRect(Rect( satLeft, 2, maxwid, height));
					Pen.fill;
				};
			};
		};
	}

	setAmps { |pre = 0, post = 0|
		preAmp = pre.sqrt; postAmp = post.sqrt;
		uv.refresh;
	}

	remove { this.setAmps(0,0) }
}
