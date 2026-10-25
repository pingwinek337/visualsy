package pl.penguin.visuals;

import java.awt.Color;

/** Gotowe kolory nieba. */
public enum SkyPreset {
	DEFAULT("Domyślne", -1),
	BLUE("Niebieskie", 0x2E7BFF),
	RED("Czerwone", 0xE02020),
	PURPLE("Fioletowe", 0x9B30FF),
	GREEN("Zielone", 0x30D060),
	PINK("Różowe", 0xFF5FB0),
	ORANGE("Pomarańczowe", 0xFF8A1F),
	ICE("Lodowe", 0x66E0FF),
	RAINBOW("Tęcza", -2),
	CUSTOM("Własne", -3);

	public final String label;
	private final int rgb;

	SkyPreset(String label, int rgb) {
		this.label = label;
		this.rgb = rgb;
	}

	public static SkyPreset byName(String name) {
		try {
			return valueOf(name);
		} catch (Exception e) {
			return DEFAULT;
		}
	}

	public static SkyPreset current() {
		return byName(PenguinConfig.get().skyPreset);
	}

	public SkyPreset next() {
		values();
		return values()[(ordinal() + 1) % values().length];
	}

	public SkyPreset previous() {
		return values()[(ordinal() + values().length - 1) % values().length];
	}

	/** Kolor RGB danego presetu (0xRRGGBB). */
	public int rgb() {
		PenguinConfig cfg = PenguinConfig.get();
		switch (this) {
			case DEFAULT:
				return 0x78A7FF;
			case RAINBOW:
				float hue = (System.currentTimeMillis() % 10000L) / 10000f;
				return Color.HSBtoRGB(hue, 0.6f, 1f) & 0xFFFFFF;
			case CUSTOM:
				return (clamp(cfg.customR) << 16) | (clamp(cfg.customG) << 8) | clamp(cfg.customB);
			default:
				return rgb;
		}
	}

	/** Kolor nieba do podmiany albo null, gdy niebo ma zostać zwykłe. */
	public static Integer overrideRgb() {
		if (!LicenseManager.isActive()) {
			return null;
		}
		PenguinConfig cfg = PenguinConfig.get();
		if (!cfg.skyEnabled) {
			return null;
		}
		SkyPreset preset = current();
		return preset == DEFAULT ? null : preset.rgb();
	}

	private static int clamp(int v) {
		return Math.max(0, Math.min(255, v));
	}
}
