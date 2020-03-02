package dev.meglic.cs.gradebook.data;

final class DatabaseRef {
	
	static final String CTABLE_PREDMETI = "CREATE TABLE Predmet (\n" +
			"\tid_predmeta INTEGER PRIMARY KEY AUTOINCREMENT not null,\n" +
			"\time VARCHAR(25) not null,\n" +
			"\tkratica VARCHAR(6) not null\n" +
			");";
	
	static final String CTABLE_OCENA = "CREATE TABLE Ocena (\n" +
			"\tid_ocene INTEGER PRIMARY KEY AUTOINCREMENT not null,\n" +
			"\tocena INT not null,\n" +
			"\time VARCHAR(15) not null\n" +
			");";
	
	static final String CTABLE_TIPOCENE = "CREATE TABLE TipOcene (\n" +
			"\tid_tipa INTEGER PRIMARY KEY AUTOINCREMENT not null,\n" +
			"\time VARCHAR(15) not null,\n" +
			"\tbarva VARCHAR(7)\n" +
			");";
	
	static final String CTABLE_OCENE = "CREATE TABLE Ocene (\n" +
			"\tid_vnosa INTEGER PRIMARY KEY AUTOINCREMENT not null,\n" +
			"\tid_predmeta INT not null,\n" +
			"\tid_ocene INT not null,\n" +
			"\tid_tipa INT not null,\n" +
			"\tpolletje INT not null,\n" +
			"\topombe VARCHAR(255),\n\n" +
			"\tCHECK (polletje > 0 AND polletje <= 2),\n\n" +
			"\tFOREIGN KEY (id_predmeta) REFERENCES Predmeti(id_predmeta),\n" +
			"\tFOREIGN KEY (id_ocene) REFERENCES Ocena(id_ocene),\n" +
			"\tFOREIGN KEY (id_tipa) REFERENCES TipOcene(id_tipa)\n" +
			");";
}
