/*
Belangrijk!!

Creeer een load file and save file knop zodat een project opgeslagen kan worden
en weer kan worden opgeroepen in een later stadium.

Bij elke actie schrijf naar een default locatie op schijf
Implementeer de BufRecord objecxt
methodes die aanwezen moeten zijn:
- na opname sla buffer data als geluids bestand in de default locatie op schijf.
- methodes/parameters die nodig zijn:
= record
= doneAction
= makeGui
= recordSettings
= channels
= duration
= metadata

Verder wanneer een bank verandert is, update de onderliggende objecten die de buffers
gebruiken in hun processen. Dit om realtime sampling and processing mogelijk te maken.

Maak een bank slider/selector zodat de EmbedGui objecten en the Score & Controller interface ook
gebruik kan maken van deze buffers.
*/

~bufPool = BufferPool();
~bufPool.makeGui;


(
~xmlDocument =  DOMDocument.new;

~root = ~xmlDocument.createElement("BufferPool");
~root.setAttribute( "selected bank", ~bufPool.currentBankName);
~root.setAttribute( "selected soundfile index", "to be implemented");
~xmlDocument.appendChild(~root);

~bufPoolSetup = ~xmlDocument.createElement("BufferPool Settings");
~root.appendChild(~bufPoolSetup);

~bufPool.soundFileViews keysValuesDo: { |key, soundFileView|
	var bank = ~xmlDocument.createElement("Bank");
	bank.setAttribute( "name", key);
	~bufPoolSetup.appendChild(bank);

	soundFileView.units do: { |bufferObject|
		var bufferElement = ~xmlDocument.createElement("Audio Buffer");
		bufferElement.setAttribute( "name", bufferObject.string);
		bank.appendChild(bufferElement);
	}
};
~xmlDocument.format
)



(
// Create an empty XML document
d = DOMDocument.new; // create empty XML document
~root = d.createElement("sampler");
~root.setAttribute( "a", "1" );
~root.setAttribute( "b", "2" );
d.appendChild(~root);
d.format;
~sample = d.createElement("soundfile");
~sample.setAttribute( "a", "1" );
~sample.setAttribute( "b", "2" );
~root.appendChild(~sample);
~sample = d.createElement("soundfile");
~sample.setAttribute( "a", "100" );
~sample.setAttribute( "b", "200" );
~root.appendChild(~sample);
(0, 1.. 20) do: { |i|
	var temp = d.createElement("point");
	temp.setAttribute( "time", 1000.rand.asString );
	~sample.appendChild(temp);
};
d.format;
)

d.format;

a = File("/Users/MarinusKlaassen/Desktop/test.xml", "w")
a.putString(d.format);
a.close

f = File("/Users/MarinusKlaassen/Desktop/test.xml", "r")
g = String.readNew(f);
f.close;
h = DOMDocument.new;
h.parseXML(g);
i = h.getChildNodes;
i.first.getNodeName;
i.first.getNodeType
i.first.hasAttributes;
i.first.getAttributes.keys do: { |x| i.first.getAttributes[x].getNodeName.postln; i.first.getAttributes[x].getNodeType.postln;  }


parseXML { |xml = "", replaceObjects = true|
		var document, root, svgObjects;

		// might take a while for large files...

		if( replaceObjects ) { svgObjects = []; } { svgObjects = objects; };
		document = DOMDocument.new;
		document.parseXML( xml );

		root = document.getDocumentElement;

		width = root.getAttribute( "width" );
		height = root.getAttribute( "height" );

		root.getChildNodes.do({ |node|
				svgObjects = svgObjects.add( SVGObject.prFromDOMElement( node ) );
				});

		objects = svgObjects;
		domDocument = document;
		domRoot = root;

		}

















// wslib 2006/2007
// requires DOMDocument classes from swiki

// SVG is a xml-based format for vector graphics
// SVG-files can be read and written with Adobe illustrator and many other
// graphic applications. There are also plugins for browser display.

// this class is based on the specification as found on the w3c site:
// http://www.w3.org/TR/SVG11/
// and checked with Adobe's SVG web-browser plugin and Illustrator

// the Pen class methods in sc look a lot like the instructions found in
// SVG files, so almost all vector elements can be drawn in SCWindows. (SVGXXX-plot methods)

// the aim is to be able to display svg files and extract their contents
// for use in sc, and vice versa (encode and export). The export can be useful for
// scoring sc events and generating visual feedback from objects. The SVGObjects can also
// be used as placeholders for graphic events within sc.




SVGFile {

	var <>path, <>objects;
	var <>width, <>height, <>userUnit = \px;
	var <domDocument, <domRoot;

	*new { |path, objects, width, height|
		path = (path ? "~/scwork/test.svg").standardizePath;
		path.replaceExtension( "svg" );
		objects = objects ? [];
		^super.newCopyArgs( path, objects, width ? 400, height ? 400 );
		}

	*read { |path| ^SVGFile( path ).read; }

	*parseXML { |xml = "", path| ^SVGFile( path ).parseXML( xml ); }

	*headerFilePath { ^this.class.filenameSymbol.asString.dirname ++ "/SVGHeader.txt"; }

	write { arg overwrite= false, ask= true, postFormat = true;
		var root, gTag, wfsPathTag;
		var d,f;

		d = DOMDocument.new; // create empty XML document
		root = d.createElement("svg");
		if( width.notNil ) { root.setAttribute( "width", width.asString ); };
		if( height.notNil ) { root.setAttribute( "height", height.asString ); };

		// xmlns="&ns_svg;" --> needed for FireFox 2.0 (and others?)
		root.setAttribute( "xmlns", "&ns_svg;" );

		d.appendChild(root);
		objects.do({ |object| object.asDOMElement( d, root ) });

		if(path.notNil)
			{ File.checkDo(
				PathName(path.standardizePath).extension_("svg").fullPath, //force svg extension
				{ |f| var txt, header;
				txt = d.format;
				header = File.use(
					this.class.headerFilePath, "r",
						{|f| f.readAllString } );
				f.putString( header ++ "\n" ++ txt );
				 }, overwrite, ask) // output to file with default formatting
			};

		if( postFormat ) { d.format.postln; };
		}

	format {  // returns svg code as string ( doesn't include header and xmlns )
		var root, gTag, wfsPathTag;
		var d,f;

		d = DOMDocument.new; // create empty XML document
		root = d.createElement("svg");
		if( width.notNil ) { root.setAttribute( "width", width.asString ); };
		if( height.notNil ) { root.setAttribute( "height", height.asString ); };
		d.appendChild(root);
		objects.do({ |object| object.asDOMElement( d, root ) });
		^d.format

		}


	read { |replaceObjects = true|
		var inXML, file;
		//var document, root, svgObjects, lastTime = 0;

		path = path.standardizePath;
		file = File(path, "r");
		inXML = String.readNew(file);
		file.close;
		this.parseXML( inXML, replaceObjects );

		}

	parseXML { |xml = "", replaceObjects = true|
		var document, root, svgObjects;

		// might take a while for large files...

		if( replaceObjects ) { svgObjects = []; } { svgObjects = objects; };
		document = DOMDocument.new;
		document.parseXML( xml );

		root = document.getDocumentElement;

		width = root.getAttribute( "width" );
		height = root.getAttribute( "height" );

		root.getChildNodes.do({ |node|
				svgObjects = svgObjects.add( SVGObject.prFromDOMElement( node ) );
				});

		objects = svgObjects;
		domDocument = document;
		domRoot = root;

		}

	asRect {
 		var xPoints, yPoints, xMin, xMax, yMin, yMax;
	 	var rectPoints;
	 	rectPoints = objects
	 		.collect({ |object|
	 			var rect; rect = object.asRect;
	 			[ rect.leftTop, rect.rightBottom ] }).flat;
		xPoints = rectPoints.collect( _.x );
		yPoints = rectPoints.collect( _.y );
		#xMin, xMax = [ xPoints.minItem, xPoints.maxItem ];
		#yMin, yMax = [ yPoints.minItem, yPoints.maxItem ];
		^Rect.newSides( xMin, yMin, xMax, yMax );
		}

	bounds {
		var rect;
		if( width.isNil or: height.isNil )
			{ rect = this.asRect };
		^Rect(0,0, width.interpretVal ?? { rect.width },  height.interpretVal ?? { rect.height });
		}

	at { |index| ^objects.at( index ); }
	copySeries { |first, second, last| ^objects.copySeries( first, second, last ); }

	put { |index, item| objects.put( index, item ); }

	add { |object| objects = objects.add( object ); }

	printOn { |stream|
		var objectsSize, numGroups;

		numGroups = objects.count({ |object| object.elementName.asSymbol == \g; });
		objectsSize = objects.size - numGroups;
		stream << "SVGFile( " << path.basename.quote << ", " << objectsSize << " object(s)" <<
			( if( numGroups > 0 ) { " / " ++ numGroups + "group(s)"  } { "" } ) << " )";
		}

	postTree { |tabLevel = 0|
		var preTabs;
		preTabs = String.fill( tabLevel, { $\t } );
		(this.class.asString ++ " : ").postln;
		objects.do({ |object, i|
			(preTabs ++ "\t" ++ (i) + " : ").post;
			object.postTree( tabLevel + 1 );
			});
		}

	asPenFunction {  // returns a function
		var penFuncs;
		penFuncs = objects.collect({  |object| object.asPenFunction });
		^{ penFuncs.do( _.value ); };
		}

	plot { |canUpdate = false|
		var penFunc;
		if( canUpdate )
			{ ^GUI.window.new( path.basename,
				this.bounds.moveTo( 128,64 )
					).drawHook_( { this.draw; } ).front; }
			{ penFunc = this.asPenFunction;
			^GUI.window.new( path.basename,
				this.bounds.moveTo( 128,64 )
					).drawHook_( { penFunc.value } ).front;
			}
		}

	draw { objects.do({ |object| object.draw }); }

	allObjects { ^objects.collect( _.allObjects ).flat; }

	hasCurves {
		^this.allObjects
			.select({ |item| item.class == SVGPath })
			.any({ |svgPath| svgPath.segments
				.any({ |segment|
					[ \curveTo, \sCurveTo ].includes( segment.type.firstToLower );
					})
				});
		}

	}