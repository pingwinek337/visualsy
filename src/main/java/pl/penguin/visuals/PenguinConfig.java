package pl.penguin.visuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Ustawienia moda zapisywane w config/penguinvisuals.json. */
public class PenguinConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("penguinvisuals.json");
	private static PenguinConfig instance = new PenguinConfig();

	// licencja
	public String licenseKey = "";

	// niebo
	public boolean skyEnabled = true;
	public String skyPreset = "DEFAULT";
	public int customR = 80;
	public int customG = 160;
	public int customB = 255;

	// walka
	public boolean hitSound = true;
	public int hitSoundIndex = 0;
	public float hitVolume = 0.8f;
	public float hitPitch = 1.0f;
	public boolean hitParticles = true;
	public int particleIndex = 0;

	// hud
	public boolean hudWatermark = true;
	public boolean hudFps = true;
	public boolean hudCoords = false;

	// cel
	public boolean targetPanel = true;
	public boolean panelNick = true;
	public boolean panelSet = true;
	public boolean panelDurability = true;
	public float targetRange = 32f;

	// obramowanie
	public boolean outline = true;
	public int outlineStyle = 0;
	public int outlineColor = 0;
	public float outlineSpeed = 1.0f;
	public int outlineThickness = 2;

	// motyw
	public int accentIndex = 0;

	public static PenguinConfig get() {
		return instance;
	}

	public static void load() {
		try {
			if (Files.exists(PATH)) {
				try (Reader reader = Files.newBufferedReader(PATH)) {
					PenguinConfig loaded = GSON.fromJson(reader, PenguinConfig.class);
					if (loaded != null) {
						instance = loaded;
					}
				}
			}
		} catch (Exception e) {
			PenguinVisualsClient.LOGGER.warn("Nie udalo sie wczytac configu, uzywam domyslnego", e);
		}
		save();
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (Exception e) {
			PenguinVisualsClient.LOGGER.warn("Nie udalo sie zapisac configu", e);
		}
	}
}
