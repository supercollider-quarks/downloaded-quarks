# ProteinBioSynthesis
SuperCollider Pattern class for the sonification of DNA information in terms of the aminoacids it encodes in a living cell.

The superclass for a number of pattern classes that are related with mapping DNA data to aminoacid names and numbers.

In a living cell, the DNA is transcribed into an complementary messenger-RNA (mRNA). The activity of translating the mRNA to proteins is carried out by the ribosomes, arranging chains of aminoacids by translating base triplets (codons). This process, called protein biosynthesis, is the same in all living beings down to bacteria and even partly in viruses.

The related pattern classes model the simplest part of this translation and transcription process. The combinations of the DNA specific nucleic acids, guanine (G), adenine (A), thymine (T), or cytosine (C) thus serve as a code for a larger set of corresponding amino acids.

The class ProteinBioSynthesis also allows to open a textfile containing genetic information and extract it. In real life, the startcodons and stopcodons distinguish the part of the code representing a protein and the one that remains unused.

