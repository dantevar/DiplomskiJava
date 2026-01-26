Sada ažuriraj test da usporedi sve tri verzije:

Edited
GAWalkTest.java+47-21
Napravio sam:

GA2opt.java - GA Standard + 2-opt local search
Ažurirao test za usporedbu svih 4 metode
Zašto je 2-opt tako moćan na random instancama?

Razlog	Objašnjenje
Glatka fitness površina	Random težine [0,1] ne stvaraju duboke lokalne optimume
Mali prostor pretrage	Za N=24, samo 276 mogućih 2-opt poteza po iteraciji
Nema strukture	Nema clustera, nema geografskih ograničenja
Jednostavna geometrija	Uniformna distribucija = lako naći optimum
Kada 2-opt NIJE dovoljan?

TSPLIB benchmarki - realni gradovi s geografskom strukturom
Cluster instance - gradovi grupirani
N > 100 - previše lokalnih optimuma
Asimetrični problemi - 2-opt pretpostavlja simetriju
Pokreni test da vidiš usporedbu svih metoda!