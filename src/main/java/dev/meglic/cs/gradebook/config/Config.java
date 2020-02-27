package dev.meglic.cs.gradebook.config;

public final class Config {
	
	private static Config instance;
	
	// TODO: Add config storage
	
	// TODO: Implement file reader
	private Config () {
	
	}
	
	// TODO: Implement getConfig()
	public String getConfig(String parameter) {
		return "";
	}
	
	public static Config getInstance() {
		if (instance == null)
			instance = new Config();
		
		return instance;
	}
}
