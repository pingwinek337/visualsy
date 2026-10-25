package pl.penguin.visuals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Sprawdza klucze licencyjne. W jarze nie ma samych kluczy, tylko ich hashe SHA-256
 * (plik assets/penguinvisuals/licenses.hashes). Bez aktywnej licencji mod nic nie robi.
 */
public final class LicenseManager {
	private static final String SALT = "penguin-v1:";
	private static final Set<String> HASHES = new HashSet<>();
	private static boolean active = false;

	private LicenseManager() {
	}

	public static void init() {
		HASHES.clear();
		try (InputStream in = LicenseManager.class.getResourceAsStream("/assets/penguinvisuals/licenses.hashes")) {
			if (in != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (!line.isEmpty()) {
						HASHES.add(line.toLowerCase(Locale.ROOT));
					}
				}
			}
		} catch (Exception e) {
			PenguinVisualsClient.LOGGER.error("Nie udalo sie wczytac listy licencji", e);
		}
		active = isValid(PenguinConfig.get().licenseKey);
		PenguinVisualsClient.LOGGER.info("Licencja: {}", active ? "aktywna" : "brak");
	}

	public static boolean isActive() {
		return active;
	}

	/** Zwraca true i zapisuje klucz, jesli jest poprawny. */
	public static boolean activate(String raw) {
		String key = normalize(raw);
		if (!isValid(key)) {
			return false;
		}
		PenguinConfig.get().licenseKey = key;
		PenguinConfig.save();
		active = true;
		return true;
	}

	public static void deactivate() {
		PenguinConfig.get().licenseKey = "";
		PenguinConfig.save();
		active = false;
	}

	/** Np. PENG-AB12-****-****-WXYZ */
	public static String maskedKey() {
		String key = PenguinConfig.get().licenseKey;
		String[] parts = key.split("-");
		if (parts.length != 5) {
			return key.isEmpty() ? "-" : "****";
		}
		return parts[0] + "-" + parts[1] + "-****-****-" + parts[4];
	}

	private static boolean isValid(String key) {
		String normalized = normalize(key);
		return !normalized.isEmpty() && HASHES.contains(sha256(SALT + normalized));
	}

	private static String normalize(String raw) {
		return raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
	}

	private static String sha256(String text) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : digest) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
