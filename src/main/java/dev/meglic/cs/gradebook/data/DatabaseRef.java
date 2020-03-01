package dev.meglic.cs.gradebook.data;

final class DatabaseRef {
	
	static final String CTABLE_PREDMETI = "CREATE TABLE Predmeti (" +
			"id_predmeta INT not null," +
			"ime VARCHAR(25) not null," +
			"kratica VARCHAR(6) not null," +
			"PRIMARY KEY (id_predmeta)" +
			");";
	
	static final String CTABLE_OCENA = "CREATE TABLE Ocena (" +
			"id_ocene INT not null," +
			"ocena INT not null," +
			"ime VARCHAR(15) not null," +
			"PRIMARY KEY (id_ocene)" +
			");";
	
	static final String CTABLE_TIPOCENE = "CREATE TABLE TipOcene (" +
			"id_tipa INT not null," +
			"ime VARCHAR(15) not null," +
			"PRIMARY KEY (id_tipa)" +
			");";
	
	static final String CTABLE_OCENE = "CREATE TABLE Ocene (" +
			"id_vnosa INT not null," +
			"id_predmeta INT not null," +
			"id_ocene INT not null," +
			"id_tipa INT not null," +
			"polletje INT not null," +
			"opombe VARCHAR(255)," +
			"CHECK (polletje > 0 AND polletje <= 2)," +
			"PRIMARY KEY (id_vnosa)," +
			"FOREIGN KEY (id_predmeta) REFERENCES Predmeti(id_predmeta)," +
			"FOREIGN KEY (id_ocene) REFERENCES Ocena(id_ocene)," +
			"FOREIGN KEY (id_tipa) REFERENCES TipOcene(id_tipa)" +
			");";
}
