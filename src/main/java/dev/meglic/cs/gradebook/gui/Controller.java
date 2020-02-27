package dev.meglic.cs.gradebook.gui;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Controller {
	
	private Connection con;
	private Statement stmt;
	
	
	@FXML
	private GridPane gradebook;
	
	@FXML
	public void initialize() {
	
	}
	
	private boolean dbConnect(String conString) {
		try {
			con = DriverManager.getConnection(conString);
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
	
	private boolean dbDisconnect() {
		try {
			con.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
}
