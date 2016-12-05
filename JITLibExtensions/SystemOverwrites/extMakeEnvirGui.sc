+ Spec {
	*guess { |key, value|
		if (value.isKindOf(SimpleNumber).not) { ^nil };

		// label units as \guess so one can throw spec away later.
		^if (value.abs > 0) {
			ControlSpec(value/20, value*20, \exp, 0, value, \guess)
		} {
			ControlSpec(-2, 2, \lin, 0, value, \guess)
		};
	}
}

+ TaskProxy {
	// use \orderNames in halo for maintaining non-alphabetic order of names
	controlKeys {
		var cKeys = this.getHalo(\orderedNames);
		if (cKeys.notNil) { ^cKeys };
		cKeys = if (envir.notNil) { envir.keys(Array).sort } { [] };
		^cKeys;
	}
}

+ TaskProxyGui {

	object_ { |obj|
		super.object_(obj);
		if(envirGui.notNil) {
			envirGui.useHalo(this.object);
		};
	}

	makeEnvirGui { |lineWidth, height|
		zone.decorator.nextLine.shift(0, 2);

		envirGui = ParamGui(
			try { this.object.envir },
			numItems,
			zone,
			Rect(0, 20, lineWidth, numItems * height),
			false
		);
	}

}

+ NdefGui {
	object_ { |obj|
		super.object_(obj);
		if(paramGui.notNil) {
			paramGui.useHalo(this.object);
		};
	}
}

+ EnvirGui {
	    // obj is an envir or nil
	    // clear specs when object changes
	object_ { |obj|
		if (this.accepts(obj)) {
			object = obj;
			specs.clear.parent_(nil);
			this.checkUpdate;
		};
	}

	// new getSpec logic
	// - if there is a spec from object or its lookup halo, use that
	// - if there is a handmade local spec, keep it.
	// - if no spec yet, and there is a value, guess a spec

	getSpec { |key, value|
		var localSpec, objSpec;
		if (key.isNil) { ^specs };

		localSpec = specs[key];
		objSpec = object.getSpec(key)
		// specs.parent may be the halo of e.g. a tdef that owns the envir
		?? { if (specs.parent.notNil) { specs.parent[key] }
			?? { Spec.specs[key] }
		};
		// always override with new spec from object.
		if (objSpec.notNil) {
			specs.put(key, objSpec);
			^objSpec;
		};

		if (localSpec.isNil and: value.notNil) {
			localSpec = Spec.guess(key, value);
			specs.put(key, localSpec);
		};
		^localSpec
	}

	useHalo { |haloObject|
		var objSpecs;
		if (haloObject.isNil) { ^this };
		this.addHalo(\specObject, haloObject);
		this.addHaloSpecsAsParent;
		this.checkUpdate;
	}

	addHaloSpecsAsParent {
		specs.parent = this.getHalo(\specObject).getSpec;
	}


	updateSliderSpecs { |editKeys|

		editKeys.do { |key, i|
			var currVal, newSpec;
			var widge = this.widgets[i];
			if (widge.isKindOf(EZSlider) or: { widge.isKindOf(EZRanger) }) {
				currVal = object[key];
				newSpec = this.getSpec(key, currVal);
				// "% - newSpec: %\n".postf(thisMethod, newSpec);
				if (newSpec.notNil and: { newSpec != widge.controlSpec }) {
					widge.controlSpec_(newSpec);
					widge.value_(currVal);
				};
			};
		}
	}

	// also get specs as state that may have changed
	getState {
		var newKeys, overflow, specsToUse;

		if (object.isNil) { ^(editKeys: [], overflow: 0, keysRotation: 0) };

		newKeys = object.keys.asArray.sort;
		overflow = (newKeys.size - numItems).max(0);
		keysRotation = keysRotation.clip(0, overflow);
		newKeys = newKeys.drop(keysRotation).keep(numItems);
		specsToUse = newKeys.collect { |key| this.getSpec(key, object[key]) };

		^(  object: object.copy,
			editKeys: newKeys,
			overflow: overflow,
			keysRotation: keysRotation,
			specsToUse: specsToUse
		)
	}

	checkUpdate {
		var newState, newKeys;
		this.addHaloSpecsAsParent;

		newState = this.getState;
		newKeys = newState[\editKeys];

		this.updateButtons;

		if (newState == prevState) {
			prevState = newState;
			^this
		};

		if (object.isNil) {
			prevState = newState;
			^this.clearFields(0);
		};

		if (newState[\overflow] > 0) {
			scroller.visible_(true);
			scroller.numItems_(object.size);
			scroller.value_(newState[\keysRotation]);

		} {
			scroller.visible_(false);
		};

		if (newKeys == prevState[\editKeys]) {
			this.setByKeys(newKeys);
		} {
			this.setByKeys(newKeys);
			if (newState[\overflow] == 0) { this.clearFields(newKeys.size) };
		};

		// need to update slider/paramview specs:
		if (newState[\specsToUse] != prevState[\specsToUse]) {

			if (this.respondsTo(\widgets)) {
				// pre 3.8 compatibility:
				this.updateSliderSpecs(newKeys);
			} {
				// from 3.8 on, using ParamViews
				// if (\ParamView.asClass.notNil) ...
				this.updateViewSpecs(
					[newKeys, newState[\specsToUse]].flop
				);
			};
		};

		prevState = newState;
	}
}

+ NdefParamGui {

	// getState and checkUpdate are copied from 3.7.0
	// for backwards compatibility with pre-3.6.6 versions.
	// delete when obsolete

	getState {
		var settings, newKeys, overflow, specsToUse;

		if (object.isNil) {
			^(name: 'anon', settings: [], editKeys: [], overflow: 0, keysRotation: 0)
		};

		settings = object.getKeysValues;
		newKeys = settings.collect(_[0]);

		overflow = (newKeys.size - numItems).max(0);
		keysRotation = keysRotation.clip(0, overflow);
		newKeys = newKeys.drop(keysRotation).keep(numItems);
		specsToUse = newKeys.collect { |key|
			var pair = settings.detect { |pair| pair[0] == key };
			this.getSpec(key, pair[1]);
		};

		^(object: object, editKeys: newKeys, settings: settings,
			overflow: overflow, keysRotation: keysRotation,
			specsToUse: specsToUse
		)
	}

	checkUpdate {
		var newState = this.getState;
		var newKeys = newState[\editKeys];

		if (newState == prevState) {
			^this
		};

		if (object.isNil) {
			prevState = newState;
			^this.clearFields(0);
		};

		if (newState[\overflow] > 0) {
			scroller.visible_(true);
			scroller.numItems_(newState[\settings].size);
			scroller.value_(newState[\keysRotation]);

		} {
			scroller.visible_(false);
		};

		if (newKeys == prevState[\editKeys]) {
			this.setByKeys(newKeys, newState[\settings]);
		} {
			this.setByKeys(newKeys, newState[\settings]);
			this.clearFields(newKeys.size);
			this.paintWetParams(newKeys);
		};

		if (newState[\specsToUse] != prevState[\specsToUse]) {
			if (this.respondsTo(\widgets)) {
				// pre 3.8 compatibility:
				this.updateSliderSpecs(newKeys);
			} {
				// from 3.8 on, using ParamViews
				this.updateViewSpecs(
					[newKeys, newState[\specsToUse]].flop
				);
			};
		};

		prevState = newState;
	}

	paintWetParams { |editKeys|
		editKeys.do { |key, i|
			if (replaceKeys[key].notNil) {
				key.postln;
				paramViews[i].background_(Color.green);
			} {
				paramViews[i].background_(skin.background);
			}
		}
	}

	// compat with pre-3.8 only
	updateSliderSpecs { |editKeys|
		var currState;

		if (object.isNil) { specs.clear; ^this };

		currState = object.getKeysValues;

		editKeys.do { |key, i|
			var currValue = currState.detect { |pair| pair[0] == key }[1];
			var oldSpec;
			var newSpec = this.getSpec(key, currValue);
			var widge = this.widgets[i];
			if (widge.respondsTo(\controlSpec)) {
				oldSpec = widge.controlSpec;
				// "% - newSpec: %\n".postf(thisMethod, newSpec);
				if (newSpec != oldSpec) {
					specs.put(key, newSpec);
					widge.controlSpec = newSpec;
					widge.value_(currValue);
				};
			};
		}
	}
}

// temp fix, should go in main 3.8 distro
+ EnvirGui {
	updateViewSpecs { |newSpecs|
		newSpecs.do { |pair|
			var name, spec, pv;
			#name, spec = pair;
			if (spec.notNil) {
				pv = this.viewForParam(name);
				if (pv.notNil) {
					pv.spec_(spec);
					pv.value_(pv.value);
				};
			};
		};
	}
}
