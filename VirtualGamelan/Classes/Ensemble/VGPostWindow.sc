VGPostWindow {
	classvar <>doc, <>deferToPostWindow = false;
	
	*show {
		if ((doc.notNil and: { try { doc.front; true } }).not) { 
			doc = Document("gamelan post", "").onClose_({  doc = nil });
			doc.background =  Color.new255(176, 210, 136); 
		}
	}
			// maybe write to log file and only keep end of posts...
			
	*post { arg obj, args;
		var oldstr;
		var str = obj.asString;
		if(args.notNil) { str = (str ++ "\n").format(*args) };
		if(doc.isNil) {
			if(deferToPostWindow) { 
				str.postln 
			}
		} { 
			// still too cpu expensive? only works with document.
			defer { 
				if (doc.notNil) { 
					doc.selectRange(10000000000, 0);
					doc.selectedString = ( str ++ "\n") 
				};
			};
		}
	}
}