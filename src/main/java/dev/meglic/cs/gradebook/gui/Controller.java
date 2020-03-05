package dev.meglic.cs.gradebook.gui;

import dev.meglic.cs.gradebook.config.Config;
import dev.meglic.cs.gradebook.data.Data;
import dev.meglic.cs.gradebook.data.Database;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class Controller {
	
	private Config conf;
	private File database;
	private Logger logger;
	
	private Database db;
	private Data data;
	
	@FXML
	private GridPane gradebook;
	
	@FXML
	public void initialize () {
		conf = Config.getInstance();
		db = Database.getInstance();
		
		logger = Logger.getLogger(this.getClass().getName());
		
		// Get database location from config
		String file = conf.getConfig("database");
		
		if (file != null) {
			database = new File(file);
		}
		
		// Handle if no record of database location -> ask user via dialog
		if (file == null) {
			
			// Get dialog result
			Pair<String, Boolean> dialogResult = getDBLocationDialog();
			
			// If result null, terminate execution and alert the user
			if (dialogResult == null) {
				Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
				alert.setHeaderText("Nekaj se je zalomilo :/");
				alert.setContentText("Neutemeljena lokacija!\nProgram se bo zaustavil...");
				alert.showAndWait();
				
				System.exit(1);
			}
			
			database = new File(dialogResult.getKey());
			conf.setConfig("database", database.getAbsolutePath());
			
			// If user checked "new database" option, set up the tables
			if (dialogResult.getValue()) {
				db.createDatabase(database);
			}
		}
		
		// Handle if configure database file doesn't exist
		if (!database.exists()) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
			alert.setHeaderText("Baza se ponovno postavlja");
			alert.setContentText("Na konfigurirani lokaciji baza (še) ne obstaja,\nzato jo bomo ponovno postavili.");
			alert.showAndWait();
			
			// Create database
			db.createDatabase(database);
		}
		
		// Connect to database
		dbConnect(database);
		
		// Get Data instance
		data = db.getDataInstance();
		
		// Display data
		updateTable();
	}
	
	private void dbConnect (File dbFile) {
		if (!db.connect(dbFile)) {
			exit(1);
		}
	}
	
	private void exit (int x) {
		db.close();
		System.exit(x);
	}
	
	private void updateTable () {
		HashMap<Data.Subject, ArrayList<Data.Entry>> tData = data.getEntriesBySubject();
		
		// Clear children (excluding headers)
		gradebook.getChildren().remove(4, gradebook.getChildren().size());
		
		// Ensure correct spacing
		try {
			gradebook.getRowConstraints().get(tData.keySet().size()).getMaxHeight();
		} catch (IndexOutOfBoundsException e) {
			for (int i = 0; i < tData.keySet().size(); i++) {
				gradebook.getRowConstraints().add(new RowConstraints(20.0, 30.0, 30.0));
			}
		}
		
		// Add grades
		tData.forEach((sub, arr) -> {
			Label subject = new Label(sub.toString()); // Subject title
			subject.setFont(new Font("System", 16));
			HBox fSemester = new HBox(2);
			HBox sSemester = new HBox(2);
			
			AtomicReference<Double> average = new AtomicReference<>(0.0);
			
			// Loop through entries for subject
			arr.forEach((ent) -> {
				Hyperlink grade = new Hyperlink(Integer.toString(ent.getGrade().getGrade())); // Create hyperlink
				grade.setFont(new Font("System Bold", 16)); // Set font and size
				grade.setTextFill(Color.web(ent.getGradeType().getColor())); // Set color
				
				average.updateAndGet(v -> (double) (v + ent.getGrade().getGrade()));
				
				// OnClick open edit dialog
				grade.setOnAction(event -> {
					Dialog<Data.Entry> dialog = gradeEntryDialog(ent);
					dialog.setTitle("Uredi oceno");
					
					// Add delete button
					dialog.getDialogPane().getButtonTypes().add(new ButtonType("Izbriši", ButtonBar.ButtonData.OTHER));
					
					dialog.showAndWait();
					
					// Update table
					updateTable();
				});
				
				// Add hyperlink to correct HBox
				if (ent.getSemester() == 1)
					fSemester.getChildren().add(grade);
				else
					sSemester.getChildren().add(grade);
			});
			
			Label avg = new Label(String.format("%.02f", average.get() / arr.size()));
			avg.setFont(new Font("System Bold", 16));
			
			// Add all items into respective grid spaces
			int row = sub.getId();
			gradebook.add(subject, 0, row);
			gradebook.add(fSemester, 1, row);
			gradebook.add(sSemester, 2, row);
			gradebook.add(avg, 3, row);
		});
	}
	
	private Pair<String, Boolean> getDBLocationDialog () {
		Dialog<Pair<String, Boolean>> dialog = new Dialog<>();
		dialog.setTitle("Lokacija podatkovne baze");
		dialog.setHeaderText("Doloèi lokacijo podatkovne baze");
		
		// TODO: Add icon
		// dialog.setGraphic(new ImageView(this.getClass().getResource("icon.png").toString()));
		
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		AtomicBoolean dirSet = new AtomicBoolean(false);
		AtomicBoolean fileSet = new AtomicBoolean(false);
		
		// GridPane START
		GridPane gp = new GridPane();
		gp.setVgap(10);
		gp.setHgap(10);
		gp.setPadding(new Insets(20, 20, 10, 20));
		
		gp.add(new Label("Lokacija:"), 0, 0);
		
		TextField tfPath = new TextField();
		Button btSearch = new Button("Brskaj...");
		TextField tfFile = new TextField();
		CheckBox cbNew = new CheckBox("Ustvari novo bazo (pobriše obstojeèo datoteko)");
		
		tfPath.setEditable(false);
		gp.add(tfPath, 1, 0);
		
		btSearch.setOnAction(event -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle("Izberi lokacijo podatkovne baze");
			File file = directoryChooser.showDialog(gp.getScene().getWindow());
			if (file != null) {
				dirSet.set(true);
				tfPath.setText(file.getAbsolutePath() + "\\" + tfFile.getText().trim() + ".db");
			}
		});
		gp.add(btSearch, 2, 0);
		
		gp.add(new Label("Datoteka:"), 0, 1);
		
		tfFile.setMaxWidth(150);
		tfFile.setOnKeyReleased(event -> {
			TextField source = (TextField) event.getSource();
			String path = tfPath.getText().trim();
			path = path.substring(0, path.lastIndexOf('\\') + 1);
			path += source.getText().trim() + ".db";
			tfPath.setText(path);
			
			if (!source.getText().trim().equals(""))
				fileSet.set(true);
			else
				fileSet.set(false);
		});
		gp.add(tfFile, 1, 1);
		
		cbNew.setSelected(true);
		gp.add(cbNew, 1, 2);
		// GridPane END
		
		dialog.getDialogPane().setContent(gp);
		
		dialog.setResultConverter(button -> {
			if (button == ButtonType.OK && dirSet.get() && fileSet.get()) {
				String path = tfPath.getText().trim();
				if (!path.equals("")) {
					return new Pair<>(path, cbNew.isSelected());
				}
			}
			return null;
		});
		
		Optional<Pair<String, Boolean>> result = dialog.showAndWait();
		return result.orElse(null);
	}
	
	private Dialog<Data.Entry> gradeEntryDialog (Data.Entry existing) {
		Dialog<Data.Entry> dialog = new Dialog<>();
		
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		// GridPane START
		GridPane gp = new GridPane();
		gp.setVgap(10);
		gp.setHgap(10);
		gp.setPadding(new Insets(20, 20, 10, 20));
		
		gp.add(new Label("Predmet"), 0, 0);
		gp.add(new Label("Ocena"), 0, 1);
		gp.add(new Label("Semester"), 0, 3);
		gp.add(new Label("Opombe:"), 0, 4);
		
		ComboBox<Data.Subject> ddSubject = new ComboBox<>();
		ComboBox<Data.Grade> ddGrade = new ComboBox<>();
		ComboBox<Data.GradeType> ddGradeType = new ComboBox<>();
		ComboBox<String> ddSemester = new ComboBox<>();
		TextArea taNotes = new TextArea();
		
		ddSubject.getItems().setAll(data.getSubjects().values());
		if (existing != null)
			ddSubject.getSelectionModel().select(existing.getSubject());
		gp.add(ddSubject, 1, 0);
		
		ddGrade.getItems().addAll(data.getGrades().values());
		if (existing != null)
			ddGrade.getSelectionModel().select(existing.getGrade());
		gp.add(ddGrade, 1, 1);
		
		ddGradeType.getItems().setAll(data.getGradeTypes().values());
		if (existing != null)
			ddGradeType.getSelectionModel().select(existing.getGradeType());
		gp.add(ddGradeType, 1, 2);
		
		ddSemester.getItems().addAll("Prvi (I.)", "Drugi (II.)");
		if (existing != null)
			ddSemester.getSelectionModel().select(existing.getSemester() - 1);
		gp.add(ddSemester, 1, 3);
		
		taNotes.prefColumnCountProperty().setValue(25);
		taNotes.prefRowCountProperty().setValue(3);
		if (existing != null)
			taNotes.setText(existing.getNotes());
		gp.add(taNotes, 0, 5, 2, 1);
		// GridPane END
		
		dialog.getDialogPane().setContent(gp);
		
		dialog.setResultConverter(button -> {
			// Field checks
			if (button == ButtonType.OK && !ddSubject.getSelectionModel().isEmpty() &&
					!ddGrade.getSelectionModel().isEmpty() && !ddGradeType.getSelectionModel().isEmpty() &&
					!ddSemester.getSelectionModel().isEmpty()) {
				// Get data
				Data.Subject subject = ddSubject.getSelectionModel().getSelectedItem();
				Data.Grade grade = ddGrade.getSelectionModel().getSelectedItem();
				Data.GradeType gradeType = ddGradeType.getSelectionModel().getSelectedItem();
				int semester = ddSemester.getSelectionModel().getSelectedIndex() + 1; // (Index + 1) gives either a 1 or a 2
				String notes = taNotes.getText().trim();
				
				if (existing == null) {
					// New entry with id -1
					return data.createNewEntry(subject, grade, gradeType, semester, notes);
				} else {
					// Update old entry
					existing.setSubject(subject);
					existing.setGrade(grade);
					existing.setGradeType(gradeType);
					existing.setSemester(semester);
					existing.setNotes(notes);
					return existing;
				}
			}
			
			// Delete button handle
			if (button.getButtonData() == ButtonBar.ButtonData.OTHER && existing != null) {
				data.removeEntry(existing);
			}
			
			return null;
		});
		
		return dialog;
	}
	
	@FXML
	public void handleNewGrade (ActionEvent actionEvent) {
		// Open dialog
		Dialog<Data.Entry> dialog = gradeEntryDialog(null);
		dialog.setTitle("Dodaj oceno");
		
		Optional<Data.Entry> newEntry = dialog.showAndWait();
		
		// Add entry
		newEntry.ifPresent(entry -> data.addEntry(entry));
		
		// Update table
		updateTable();
	}
}
