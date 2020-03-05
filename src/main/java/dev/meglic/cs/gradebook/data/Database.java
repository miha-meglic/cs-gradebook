package dev.meglic.cs.gradebook.data;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Database {
	
	private static Database instance;
	
	private Connection con;
	
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
	
	// GET CONNECTION
	// Package-private
	Connection getCon () {
		return con;
	}
	
	public Data getDataInstance() {
		if (con != null)
			return Data.getInstance();
		return null;
	}
	
	// CLOSE CONNECTION
	public void close () {
		try {
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
		
		// Create tables
		try {
			Statement stmt = con.createStatement();
			
			stmt.execute(DatabaseRef.CTABLE_SUBJECT);
			stmt.execute(DatabaseRef.CTABLE_GRADE);
			stmt.execute(DatabaseRef.CTABLE_GRADETYPE);
			stmt.execute(DatabaseRef.CTABLE_ENTRIES);
			
			stmt.close();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage());
			return false;
		}
		
		return true;
	}
}
