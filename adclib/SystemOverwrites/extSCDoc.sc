/*
Overwrite template for help files with less cluttered version.
// test:
Class.allClasses.reject(_.isMetaClass).choose.makeHelp;
*/

+ Class {
	makeHelp {
		var str = SCDoc.makeClassTemplate(SCDocEntry.newUndocClass(this));
		^Document(this.name ++ ".schelp", str);
	}
}

// clutterfree class help file template

+ SCDoc {
	*makeClassTemplate {|doc|
		var name = doc.title;
		var cats = doc.categories;
		var class = doc.klass;
		var n, m, f, c;

		f = {|cm|
			var txt,c,m,l,last,sym;
			if(cm) {
				txt = "\nCLASSMETHODS::\n\n";
				n = doc.undoccmethods;
				c = class.class;
			} {
				txt = "\nINSTANCEMETHODS::\n\n";
				n = doc.undocimethods;
				c = class;
			};
			n.do {|x|
				txt = txt ++ "METHOD::" + x ++ "\n";
				sym = x.asSymbol;
				m = c.findRespondingMethodFor(sym.asSetter);
				m = m ?? {c.findRespondingMethodFor(sym)};
				m !? {
					l = m.argNames;
					last = l.size-1;
					l.do {|a,i|
						if (i>0) { //skip 'this' (first arg)
							txt = txt ++ "argument:: ";
							if(i==last and: {m.varArgs}) {
								txt = txt ++ " ... ";
							};
							txt = txt ++ a ++ "\n";
						}
					}
				};
				txt = txt ++ "\n";
			};
			txt;
		};

		^ "TITLE::"+name
		++"\nsummary:: _short_description_here_\n"
		++"categories::"+cats.join(", ")
		++"\nrelated:: Classes/SomeRelatedClass, Reference/SomeRelatedStuff\n\n"
		++"DESCRIPTION::\n_put_long_description_here_\n\n"
		++ "\nFirst code examples: \n\ncode::\n_some_example_code_\n::\n"
		++ f.(true) ++ f.(false)
		++"\nEXAMPLES::\n\ncode::\n_some_example_code_\n::\n";
	}
}