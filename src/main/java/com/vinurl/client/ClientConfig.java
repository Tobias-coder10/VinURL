package com.vinurl.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.vinurl.util.Url;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.vinurl.util.Constants.LOGGER;

/**
 * Client settings, saved as config/vinurl.json.
 * Replaces the owo-lib config. Settings from the old owo file (VinURLConfig.json5) are imported once.
 */
public class ClientConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
	private static final Path FILE = CONFIG_DIR.resolve("vinurl.json");
	private static final Path OLD_OWO_FILE = CONFIG_DIR.resolve("VinURLConfig.json5");

	// general
	public boolean downloadEnabled = true;
	public boolean updatesOnStartup = true;
	public boolean showDescription = true;
	public List<String> urlWhitelist = new ArrayList<>();

	// download settings
	public AudioQuality audioBitrate = AudioQuality.MEDIUM;
	public String parameters = "";

	public enum AudioQuality {
		LOW("48K"),
		MEDIUM("96K"),
		HIGH("128K");

		private final String value;

		AudioQuality(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	// Accessors with the same names the owo-generated wrapper had
	public boolean downloadEnabled() {return downloadEnabled;}
	public boolean updatesOnStartup() {return updatesOnStartup;}
	public boolean showDescription() {return showDescription;}
	public List<String> urlWhitelist() {return urlWhitelist;}
	public AudioQuality audioBitrate() {return audioBitrate;}
	public String parameters() {return parameters;}

	public static ClientConfig load() {
		ClientConfig config = null;

		if (Files.exists(FILE)) {
			config = read(FILE);
		} else if (Files.exists(OLD_OWO_FILE)) {
			config = read(OLD_OWO_FILE);
			if (config != null) {LOGGER.info("Imported settings from {}", OLD_OWO_FILE.getFileName());}
		}

		if (config == null) {config = new ClientConfig();}
		config.sanitize();
		config.save();
		return config;
	}

	private static ClientConfig read(Path path) {
		try {
			String text = Files.readString(path);
			try (Reader reader = new StringReader(text); JsonReader json = new JsonReader(reader)) {
				// lenient: also accepts the comments and trailing commas of the old .json5 file
				json.setStrictness(Strictness.LENIENT);
				return GSON.fromJson(json, ClientConfig.class);
			}
		} catch (Exception e) {
			LOGGER.error("Could not read config {}, using defaults", path.getFileName(), e);
			return null;
		}
	}

	/** Fixes missing values and removes invalid whitelist entries. */
	public void sanitize() {
		if (urlWhitelist == null) {urlWhitelist = new ArrayList<>();}
		urlWhitelist = new ArrayList<>(urlWhitelist.stream()
			.filter(entry -> entry != null && !entry.isBlank())
			.map(String::trim)
			.filter(Url::isValid)
			.distinct()
			.toList());
		if (audioBitrate == null) {audioBitrate = AudioQuality.MEDIUM;}
		if (parameters == null) {parameters = "";}
	}

	public void save() {
		try {
			Files.createDirectories(CONFIG_DIR);
			Files.writeString(FILE, GSON.toJson(this));
		} catch (IOException e) {
			LOGGER.error("Could not save config", e);
		}
	}
}
