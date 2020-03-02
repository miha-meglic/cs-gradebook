package dev.meglic.cs.gradebook.gui;

import dev.meglic.cs.gradebook.config.Config;
import dev.meglic.cs.gradebook.data.Database;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.sql.Statement;
import javafx.util.Pair;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class Controller {
	
	private Config conf;
	private File database;
	
	private Database db;
	private Statement stmt;
	
	@FXML
	private GridPane gradebook;
	
	@FXML
	public void initialize () {
		conf = Config.getInstance();
		db = Database.getInstance();
		
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
	}
	
	private void dbConnect (File dbFile) {
		if (db.connect(dbFile)) {
			if ((stmt = db.getStatement()) == null) {
				exit(1);
			}
		} else {
			exit(1);
		}
	}
	
	private void exit (int x) {
		db.closeAll();
		System.exit(x);
	}
	
	private Pair<String, Boolean> getDBLocationDialog () {
		Dialog<Pair<String, Boolean>> dialog = new Dialog<>();
		dialog.setTitle("Lokacija podatkovne baze");
		dialog.setHeaderText("Doloèi lokacijo podatkovne baze");
		
		// TODO: Add icon
		// dialog.setGraphic(new ImageView(this.getClass().getResource("icon.png").toString()));
		
		ButtonType confirm = new ButtonType("Izberi", ButtonBar.ButtonData.OK_DONE);
		ButtonType cancel = new ButtonType("Preklièi", ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes().addAll(confirm, cancel);
		
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
			if (button == confirm && dirSet.get() && fileSet.get()) {
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
}
