ViewActionUpdater {
	classvar funcs, onCloseFunc;

	*initClass {
		funcs = MultiLevelIdentityDictionary();
		onCloseFunc = {
			|view, actionName, func|
			ViewActionUpdater.disable(view, actionName, func);
		};
	}

	*actionFunc {
		|propertyName\value, signalName=\value|
		var func = funcs.at(propertyName, signalName);
		if (func.isNil) {
			func = "{ |view ...args| view.changed('%', view.%) }".format(signalName, propertyName).interpret;
			funcs.put(propertyName, signalName, func);
		};
		^func;
	}

	*isConnected {
		|view, actionName, actionFunc|
		var isConnected = false;
		isConnected == isConnected || (view.perform(actionName) == actionFunc);
		if (view.perform(actionName).isKindOf(FunctionList)) {
			isConnected = isConnected || view.perform(actionName).array.includes(actionFunc);
		};
		^isConnected;
	}

	*enable {
		|view, actionName=\action, propertyName=\value, signalName=\value|
		var func = this.actionFunc(propertyName, signalName);
		if (this.isConnected(view, actionName, func).not) {
			view.perform(actionName.asSetter, view.perform(actionName).addFunc(func));
			view.onClose = view.onClose.addFunc(onCloseFunc.value(_, actionName, func));
		}
	}

	*disable {
		|view, actionName, func|
		view.perform(actionName.asSetter, view.perform(actionName).removeFunc(func));
	}
}