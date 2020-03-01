package dev.meglic.cs.gradebook.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Database {
	
	private static Database instance;
	
	private Connection con;
	private Statement stmt;
	
	private Logger logger;
	
	private Database () {
		logger = Logger.getLogger(this.getClass().getName());
	}
	
	public static Database getInstance () {
		if (instance == null)
			instance = new Database();
		return instance;
	}
	
	// CONNECT
	// ! if unsuccessful, return false
	public boolean connect (File file) {
		try {
			// If connection already exists, return true
			if (con != null) {
				if (!con.isClosed()) {
					return true;
				}
			}
			// Else create connection
			con = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
			return true;
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage());
			return false;
		}
	}
	
	// GET STATEMENT
	// ! if unsuccessful, return null
	// Creates statement if non-existent
	public Statement getStatement () {
		if (con == null)
			return null;
		
		if (stmt == null) {
			try {
				stmt = con.createStatement();
			} catch (SQLException e) {
				logger.log(Level.SEVERE, e.getMessage());
				return null;
			}
		}
		
		return stmt;
	}
	
	// CLOSE ALL CONNECTIONS
	public void closeAll () {
		try {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
			if (con != null) {
				con.close();
				con = null;
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
	}
	
	// CREATE DATABASE
	// ! if unsuccessful, return false
	// ! deletes existing file
	// Creates all necessary tables for program operation
	// Connects to database
	public boolean createDatabase (File file) {
		// If file is null, return false
		if (file == null) {
			return false;
		}
		
		// If file exist, delete it
		if (file.exists()) {
			if (!file.delete()) {
				return false;
			}
		}
		
		// Connect to db
		if (!connect(file))
			return false;
		
		// Create statement
		if (getStatement() == null)
			return false;
		
		// Create tables
		try {
			stmt.execute(DatabaseRef.CTABLE_PREDMETI);
			stmt.execute(DatabaseRef.CTABLE_OCENA);
			stmt.execute(DatabaseRef.CTABLE_TIPOCENE);
			stmt.execute(DatabaseRef.CTABLE_OCENE);
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage());
			return false;
		}
		
		return true;
	}
}
