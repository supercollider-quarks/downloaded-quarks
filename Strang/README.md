# Strang
Experimental nested String and UTF-8 support for SuperCollider

Thanks to Laureano López.

The String class doesn't support UTF-8 (http://en.wikipedia.org/wiki/UTF-8), while most editors that are in use with SuperCollider allow strings to be written in unicode. Such strings are internally represented in a way that doesn't split the string into correct characters. This class makes it easier to use UTF-8 encodings.

This works well in many cases, but is experimental. Improvements welcome!

## Etymology
"Strang" is German for string, rope, strand, cord, skein.

The Deutsches Wörterbuch von Jacob Grimm und Wilhelm Grimm writes:

"strang, m. , dickes, gedrehtes seil. herkunft und form.
altnord. strengr, ags. streng, engl. string, fries. stringe, mnl. strenge, stringe, strenc, nl. streng, stranc. das deutsche hat formen der a-, i- und n-declination sowie masc. und fem. genus nebeneinander: im ahd. sg. nom. stranc, strangi, strengi, strenki, pl. nom. strangun, dat. strangon, strengin, s. Graff ahd. sprachsch. 6, 755; im mhd. st. masc. stranc neben sw. masc. und fem. strange; im mnd. sg. strank, strange, pl. nom. strenge, dat. strengen, acc. strengen, strenge. […]"

http://woerterbuchnetz.de/DWB/?sigle=DWB&mode=Vernetzung&lemid=GS50779#XGS50779

