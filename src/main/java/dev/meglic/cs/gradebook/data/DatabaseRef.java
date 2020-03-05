package dev.meglic.cs.gradebook.data;

final class DatabaseRef {
	
	static final String CTABLE_SUBJECT = "CREATE TABLE Predmet (\n" +
			"\tid_predmeta INTEGER PRIMARY KEY AUTOINCREMENT not null,\n" +
			"\time VARCHAR(25) not null,\n" +
			"\tkratica VARCHAR(6) not null\n" +
			");";
	
	static final String CTABLE_GRADE = "CREATE TABLE Ocena (\n" +
			"\tid_ocene INTEGER PRIMARY KEY AUTOINCREMENT not null,\n" +
			"\tocena INT not null,\n" +
			"\time VARCHAR(15) not null\n" +
			");";
	
	static final String CTABLE_GRADETYPE = "CREATE TABLE TipOcene (\n" +
			"\tid_tipa INTEGER PRIMARY KEY AUTOINCREMENT not null,\n" +
			"\time VARCHAR(15) not null,\n" +
			"\tbarva VARCHAR(7)\n" +
			");";
	
	static final String CTABLE_ENTRIES = "CREATE TABLE Ocene (\n" +
			"\tid_vnosa INTEGER PRIMARY KEY AUTOINCREMENT not null,\n" +
			"\tid_predmeta INT not null,\n" +
			"\tid_ocene INT not null,\n" +
			"\tid_tipa INT not null,\n" +
			"\tpolletje INT not null,\n" +
			"\topombe VARCHAR(255),\n\n" +
			"\tCHECK (polletje > 0 AND polletje <= 2),\n\n" +
			"\tFOREIGN KEY (id_predmeta) REFERENCES Predmet(id_predmeta),\n" +
			"\tFOREIGN KEY (id_ocene) REFERENCES Ocena(id_ocene),\n" +
			"\tFOREIGN KEY (id_tipa) REFERENCES TipOcene(id_tipa)\n" +
			");";
	
	static final String QTABLE_SUBJECT = "SELECT * FROM Predmet";
	static final String SUBJECT_ID = "id_predmeta";
	static final String SUBJECT_NAME = "ime";
	static final String SUBJECT_ABBR = "kratica";
	
	static final String ADD_SUBJECT = "INSERT INTO Predmet (ime, kratica) VALUES ('%s', '%s')";
	static final String UPDATE_SUBJECT = "UPDATE Predmet SET ime = '%s', kratica = '%s' WHERE id_predmeta = %d";
	static final String DELETE_SUBJECT = "DELETE FROM Predmet WHERE id_predmeta = %d";
	
	static final String QTABLE_GRADE = "SELECT * FROM Ocena";
	static final String GRADE_ID = "id_ocene";
	static final String GRADE_GRADE = "ocena";
	static final String GRADE_NAME = "ime";
	
	static final String ADD_GRADE = "INSERT INTO Ocena (ocena, ime) VALUES (%d, '%s')";
	static final String UPDATE_GRADE = "UPDATE Ocena SET ocena = %d, ime = '%s' WHERE id_ocene = %d";
	static final String DELETE_GRADE = "DELETE FROM Ocena WHERE id_ocene = %d";
	
	static final String QTABLE_GRADETYPE = "SELECT * FROM TipOcene";
	static final String GRADETYPE_ID = "id_tipa";
	static final String GRADETYPE_NAME = "ime";
	static final String GRADETYPE_COLOR = "barva";
	
	static final String ADD_GRADETYPE = "INSERT INTO TipOcene (ime, barva) VALUES ('%s', '%s')";
	static final String UPDATE_GRADETYPE = "UPDATE TipOcene SET ime = '%s', barva = '%s' WHERE id_tipa = %d";
	static final String DELETE_GRADETYPE = "DELETE FROM TipOcene WHERE id_tipa = %d";
	
	static final String QTABLE_ENTRIES = "SELECT * FROM Ocene";
	static final String ENTRIES_ID = "id_vnosa";
	static final String ENTRIES_SUBJECT = "id_predmeta";
	static final String ENTRIES_GRADE = "id_ocene";
	static final String ENTRIES_GRADETYPE = "id_tipa";
	static final String ENTRIES_SEMESTER = "polletje";
	static final String ENTRIES_NOTES = "opombe";
	
	static final String ADD_ENTRY = "INSERT INTO Ocene (id_predmeta, id_ocene, id_tipa, polletje, opombe) VALUES (%d, %d, %d, %d, '%s')";
	static final String UPDATE_ENTRY = "UPDATE Ocene SET id_predmeta = %d, id_ocene = %d, id_tipa = %d, polletje = %d, opombe = '%s' WHERE id_vnosa = %d";
	static final String DELETE_ENTRY = "DELETE FROM Ocene WHERE id_vnosa = %d";
	static final String LASTID_ENTRY = "SELECT * FROM Ocene WHERE id_vnosa = (SELECT MAX(id_vnosa) FROM Ocene)";
}
