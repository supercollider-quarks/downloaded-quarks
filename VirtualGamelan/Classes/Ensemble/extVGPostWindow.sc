+ Object {/*
	postvg { arg ... args;
		VGPostWindow.post(this, args)
	}
	postvgcs { arg ... args;
		VGPostWindow.post(this.asCompileString, args)
	}
*/

	postvg { arg ... args; 
//		var str = this.asString;
//		if(args.notNil) { str = (str ++ "\n").format(*args) };
//		repostln(str)
	}
	postvgcs { arg ... args;
//		postvg(this.asCompileString, args)
	}
}
