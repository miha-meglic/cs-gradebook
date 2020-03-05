package dev.meglic.cs.gradebook.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Data {
	
	private static Data instance;
	
	private HashMap<Integer, Entry> entries;
	private HashMap<Integer, Subject> subjects;
	private HashMap<Integer, Grade> grades;
	private HashMap<Integer, GradeType> gradeTypes;
	
	private Logger logger;
	
	private Data () {
		logger = Logger.getLogger(this.getClass().getName());
		
		entries = new HashMap<>();
		subjects = new HashMap<>();
		grades = new HashMap<>();
		gradeTypes = new HashMap<>();
		
		loadData();
	}
	
	static Data getInstance() {
		if (instance == null)
			instance = new Data();
		return instance;
	}
	
	private void loadData () {
		// Load Subjects
		try (Statement stmt = Database.getInstance().getCon().createStatement();
			 ResultSet predmet = stmt.executeQuery(DatabaseRef.QTABLE_SUBJECT)) {
			if (predmet != null) {
				while (predmet.next()) {
					int id = predmet.getInt(DatabaseRef.SUBJECT_ID);
					String name = predmet.getString(DatabaseRef.SUBJECT_NAME);
					String abbr = predmet.getString(DatabaseRef.SUBJECT_ABBR);
					
					subjects.put(id, new Subject(id, name, abbr));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Load Grades
		try (Statement stmt = Database.getInstance().getCon().createStatement();
			 ResultSet ocena = stmt.executeQuery(DatabaseRef.QTABLE_GRADE)) {
			if (ocena != null) {
				while (ocena.next()) {
					int id = ocena.getInt(DatabaseRef.GRADE_ID);
					int grade = ocena.getInt(DatabaseRef.GRADE_GRADE);
					String name = ocena.getString(DatabaseRef.GRADE_NAME);
					
					grades.put(id, new Grade(id, grade, name));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Load Grade Types
		try (Statement stmt = Database.getInstance().getCon().createStatement();
			 ResultSet tipOcene = stmt.executeQuery(DatabaseRef.QTABLE_GRADETYPE)) {
			if (tipOcene != null) {
				while (tipOcene.next()) {
					int id = tipOcene.getInt(DatabaseRef.GRADETYPE_ID);
					String name = tipOcene.getString(DatabaseRef.GRADETYPE_NAME);
					String color = tipOcene.getString(DatabaseRef.GRADETYPE_COLOR);
					
					gradeTypes.put(id, new GradeType(id, name, color));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Load Entries
		try (Statement stmt = Database.getInstance().getCon().createStatement();
			 ResultSet ocene = stmt.executeQuery(DatabaseRef.QTABLE_ENTRIES)) {
			if (ocene != null) {
				while (ocene.next()) {
					int id = ocene.getInt(DatabaseRef.ENTRIES_ID);
					Subject subject = subjects.get(ocene.getInt(DatabaseRef.ENTRIES_SUBJECT));
					Grade grade = grades.get(ocene.getInt(DatabaseRef.ENTRIES_GRADE));
					GradeType gradeType = gradeTypes.get(ocene.getInt(DatabaseRef.ENTRIES_GRADETYPE));
					int semester = ocene.getInt(DatabaseRef.ENTRIES_SEMESTER);
					String notes = ocene.getString(DatabaseRef.ENTRIES_NOTES);
					
					entries.put(id, new Entry(id, subject, grade, gradeType, semester, notes));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// GET ENTRIES BY SUBJECT
	public HashMap<Subject, ArrayList<Entry>> getEntriesBySubject () {
		HashMap<Subject, ArrayList<Entry>> ret = new HashMap<>();
		
		// Populate HashMap by iterating through all entries
		entries.values().forEach(val -> {
			Subject sub = val.getSubject();
			
			// If array doesn't exist for specific subject, create one
			if (ret.getOrDefault(sub, null) == null)
				ret.put(sub, new ArrayList<>());
			
			// Add entry to array
			ret.get(sub).add(val);
		});
		
		// Return HashMap
		return ret;
	}
	
	// Creates a new entry with id -1 (so it can be later identified)
	public Entry createNewEntry (Subject subject, Grade grade, GradeType gradeType, int semester, String notes) {
		if (semester >= 1 && semester <= 2 && notes.length() <= 255)
			return new Entry(-1, subject, grade, gradeType, semester, notes);
		return null;
	}
	
	// Adds entry to database if id -1
	public boolean addEntry (Entry entry) {
		if (entry.getId() == -1) {
			// Add item to database
			try (Statement stmt = Database.getInstance().getCon().createStatement()) {
				String query = String.format(DatabaseRef.ADD_ENTRY, entry.subject.id, entry.grade.id, entry.gradeType.id, entry.semester, entry.notes);
				stmt.execute(query);
			} catch (SQLException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
			// Get item ID
			try (Statement stmt = Database.getInstance().getCon().createStatement();
				 ResultSet rs = stmt.executeQuery(DatabaseRef.LASTID_ENTRY)) {
				if (rs.next()) {
					entry.id = rs.getInt(DatabaseRef.ENTRIES_ID);
					entries.put(entry.id, entry);
					return true;
				}
			} catch (SQLException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
		}
		return false;
	}
	
	public HashMap<Integer, Subject> getSubjects () {
		return subjects;
	}
	
	public HashMap<Integer, Grade> getGrades () {
		return grades;
	}
	
	public HashMap<Integer, GradeType> getGradeTypes () {
		return gradeTypes;
	}
	
	
	/*
	============================
	INNER CLASSES
	============================
	 */
	
	public class Entry {
		private int id;
		private Subject subject;
		private Grade grade;
		private GradeType gradeType;
		private int semester;
		private String notes;
		
		public Entry (int id, Subject subject, Grade grade, GradeType gradeType, int semester, String notes) {
			this.id = id;
			this.subject = subject;
			this.grade = grade;
			this.gradeType = gradeType;
			this.semester = semester;
			this.notes = notes;
		}
		
		public void update () {
			try (Statement stmt = Database.getInstance().getCon().createStatement()) {
				String query = String.format(DatabaseRef.UPDATE_ENTRY, this.subject.getId(), this.grade.getId(), this.gradeType.getId(), this.semester, this.notes, this.id);
				stmt.execute(query);
			} catch (SQLException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
		}
		
		public int getId () {
			return id;
		}
		
		public Subject getSubject () {
			return subject;
		}
		
		public void setSubject (Subject subject) {
			this.subject = subject;
		}
		
		public Grade getGrade () {
			return grade;
		}
		
		public void setGrade (Grade grade) {
			this.grade = grade;
		}
		
		public GradeType getGradeType () {
			return gradeType;
		}
		
		public void setGradeType (GradeType gradeType) {
			this.gradeType = gradeType;
		}
		
		public int getSemester () {
			return semester;
		}
		
		public boolean setSemester (int semester) {
			if (semester >= 1 && semester <= 2) {
				this.semester = semester;
				update();
				return true;
			}
			return false;
		}
		
		public String getNotes () {
			return notes;
		}
		
		public boolean setNotes (String notes) {
			if (notes.length() <= 255) {
				this.notes = notes;
				update();
				return true;
			}
			return false;
		}
	}
	
	public class Subject {
		private int id;
		private String name;
		private String abbr;
		
		public Subject (int id, String name, String abbr) {
			this.id = id;
			this.name = name;
			this.abbr = abbr;
		}
		
		public void update () {
			try (Statement stmt = Database.getInstance().getCon().createStatement()) {
				String query = String.format(DatabaseRef.UPDATE_SUBJECT, this.name, this.abbr, this.id);
				stmt.execute(query);
			} catch (SQLException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
		}
		
		public int getId () {
			return id;
		}
		
		public String getName () {
			return name;
		}
		
		public boolean setName (String name) {
			if (name.length() <= 25) {
				this.name = name;
				update();
				return true;
			}
			return false;
		}
		
		public String getAbbr () {
			return abbr;
		}
		
		public boolean setAbbr (String abbr) {
			if (abbr.length() <= 6) {
				this.abbr = abbr;
				update();
				return true;
			}
			return false;
		}
		
		@Override
		public String toString () {
			return String.format("%s (%s)", this.name, this.abbr);
		}
	}
	
	public class Grade {
		private int id;
		private int grade;
		private String name;
		
		public Grade (int id, int grade, String name) {
			this.id = id;
			this.grade = grade;
			this.name = name;
		}
		
		public void update () {
			try (Statement stmt = Database.getInstance().getCon().createStatement()) {
				String query = String.format(DatabaseRef.UPDATE_GRADE, this.grade, this.name, this.id);
				stmt.execute(query);
			} catch (SQLException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
		}
		
		public int getId () {
			return id;
		}
		
		public int getGrade () {
			return grade;
		}
		
		public void setGrade (int grade) {
			this.grade = grade;
			update();
		}
		
		public String getName () {
			return name;
		}
		
		public boolean setName (String name) {
			if (name.length() <= 15) {
				this.name = name;
				update();
				return true;
			}
			return false;
		}
		
		@Override
		public String toString () {
			return String.format("%s (%d)", this.name, this.grade);
		}
	}
	
	public class GradeType {
		private int id;
		private String name;
		private String color;
		
		public GradeType (int id, String name, String color) {
			this.id = id;
			this.name = name;
			this.color = color;
		}
		
		public void update () {
			try (Statement stmt = Database.getInstance().getCon().createStatement()) {
				String query = String.format(DatabaseRef.UPDATE_GRADETYPE, this.name, this.color, this.id);
				stmt.execute(query);
			} catch (SQLException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
		}
		
		public int getId () {
			return id;
		}
		
		public String getName () {
			return name;
		}
		
		public boolean setName (String name) {
			if (name.length() <= 15) {
				this.name = name;
				update();
				return true;
			}
			return false;
		}
		
		public String getColor () {
			return color;
		}
		
		public boolean setColor (String color) {
			if (color.length() <= 7) {
				this.color = color;
				update();
				return true;
			}
			return false;
		}
		
		@Override
		public String toString () {
			return this.name;
		}
	}
}
