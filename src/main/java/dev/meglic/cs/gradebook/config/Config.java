package dev.meglic.cs.gradebook.config;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Config {
	
	private static Config instance;
	
	private JSONObject config;
	
	private static String file;
	private Logger logger;
	
	private Config () {
		config = new JSONObject();
		logger = Logger.getLogger(this.getClass().getName());
		
		if (System.getProperty("os.name").startsWith("Windows")) {
			file = System.getenv("APPDATA") + "\\Meglic\\Gradebook\\config.json";
			try {
				File f = new File(file);
				f.getParentFile().mkdirs();
				f.createNewFile();
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
		} else {
			file = null;
		}
		
		loadFromFile();
	}
	
	private void loadFromFile () {
		if (file != null) {
			try (FileReader reader = new FileReader(file)) {
				JSONParser parser = new JSONParser();
				config = (JSONObject) parser.parse(reader);
			} catch (ParseException | IOException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
		}
	}
	
	private void saveToFile () {
		if (config != null && file != null) {
			try (FileWriter writer = new FileWriter(file)) {
				config.writeJSONString(writer);
				writer.flush();
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public String getConfig (String parameter) {
		return (String) config.getOrDefault(parameter, null);
	}
	
	@SuppressWarnings("unchecked")
	public void setConfig (String parameter, String value) {
		config.put(parameter, value);
		saveToFile();
	}
	
	public static Config getInstance () {
		if (instance == null)
			instance = new Config();
		return instance;
	}
}
