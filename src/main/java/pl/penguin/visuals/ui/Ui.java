package pl.penguin.visuals.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import pl.penguin.visuals.PenguinConfig;

/**
 * Wspólny silnik wyglądu: kolory motywu, zaokrąglone prostokąty, cienie, tekst,
 * ikony i pixel-art pingwin. Wszystko rysowane przez te metody respektuje globalną
 * przezroczystość (używaną do animacji pojawiania się menu).
 */
public final class Ui {
	public static final int[] ACCENTS = {0x4FC3F7, 0xFF5722, 0xB388FF, 0x4DD0A1, 0xFF6FA8};
	public static final String[] ACCENT_NAMES = {"Lód", "Lawa", "Neon", "Mięta", "Róż"};

	private static final String[] PENGUIN = {
		"...KKKK...",
		"..KKKKKK..",
		"..KWKKWK..",
		"..KKOOKK..",
		".KKWWWWKK.",
		".KKWWWWKK.",
		"KKKWWWWKKK",
		"KKKWWWWKKK",
		".KKWWWWKK.",
		"..KWWWWK..",
		"..OO..OO.."
	};

	private static float globalAlpha = 1f;

	private Ui() {
	}

	// ------------------------------------------------------------------ kolory

	public static int accent() {
		int index = Math.floorMod(PenguinConfig.get().accentIndex, ACCENTS.length);
		return 0xFF000000 | ACCENTS[index];
	}

	public static int withAlpha(int color, int alpha) {
		return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0xFFFFFF);
	}

	/** Miesza dwa kolory ARGB (t od 0 do 1). */
	public static int lerp(int a, int b, float t) {
		t = Math.max(0f, Math.min(1f, t));
		int result = 0;
		for (int shift = 24; shift >= 0; shift -= 8) {
			int ca = (a >>> shift) & 0xFF;
			int cb = (b >>> shift) & 0xFF;
			result |= ((int) (ca + (cb - ca) * t) & 0xFF) << shift;
		}
		return result;
	}

	/** Przyciemnia kolor (factor poniżej 1) albo rozjaśnia (powyżej 1). */
	public static int shade(int color, float factor) {
		int r = Math.min(255, (int) (((color >> 16) & 0xFF) * factor));
		int g = Math.min(255, (int) (((color >> 8) & 0xFF) * factor));
		int b = Math.min(255, (int) ((color & 0xFF) * factor));
		return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
	}

	public static float ease(float t) {
		t = Math.max(0f, Math.min(1f, t));
		float inv = 1f - t;
		return 1f - inv * inv * inv;
	}

	// ------------------------------------------------------------------ globalna przezroczystość

	public static void setAlpha(float alpha) {
		globalAlpha = Math.max(0f, Math.min(1f, alpha));
	}

	public static int fade(int color) {
		if (globalAlpha >= 0.999f) {
			return color;
		}
		int a = (int) (((color >>> 24) & 0xFF) * globalAlpha);
		return (a << 24) | (color & 0xFFFFFF);
	}

	// ------------------------------------------------------------------ prymitywy

	public static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	public static void rect(DrawContext c, int x, int y, int w, int h, int color) {
		color = fade(color);
		if ((color >>> 24) == 0 || w <= 0 || h <= 0) {
			return;
		}
		c.fill(x, y, x + w, y + h, color);
	}

	public static void gradient(DrawContext c, int x, int y, int w, int h, int top, int bottom) {
		if (w <= 0 || h <= 0) {
			return;
		}
		c.fillGradient(x, y, x + w, y + h, fade(top), fade(bottom));
	}

	private static int inset(int r, int row) {
		double dy = r - row - 0.5;
		return (int) Math.ceil(r - Math.sqrt(r * r - dy * dy));
	}

	/** Wypełniony prostokąt z zaokrąglonymi rogami. */
	public static void round(DrawContext c, int x, int y, int w, int h, int r, int color) {
		color = fade(color);
		if ((color >>> 24) == 0 || w <= 0 || h <= 0) {
			return;
		}
		r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
		if (r == 0) {
			c.fill(x, y, x + w, y + h, color);
			return;
		}
		for (int i = 0; i < r; i++) {
			int in = inset(r, i);
			c.fill(x + in, y + i, x + w - in, y + i + 1, color);
			c.fill(x + in, y + h - i - 1, x + w - in, y + h - i, color);
		}
		if (h - 2 * r > 0) {
			c.fill(x, y + r, x + w, y + h - r, color);
		}
	}

	/** Obramowanie (1 px) z zaokrąglonymi rogami. */
	public static void roundOutline(DrawContext c, int x, int y, int w, int h, int r, int color) {
		color = fade(color);
		if ((color >>> 24) == 0 || w <= 2 || h <= 2) {
			return;
		}
		r = Math.max(1, Math.min(r, Math.min(w, h) / 2));
		c.fill(x + r, y, x + w - r, y + 1, color);
		c.fill(x + r, y + h - 1, x + w - r, y + h, color);
		c.fill(x, y + r, x + 1, y + h - r, color);
		c.fill(x + w - 1, y + r, x + w, y + h - r, color);
		for (int i = 0; i < r; i++) {
			int in = inset(r, i);
			int prev = i == 0 ? r : inset(r, i - 1);
			int span = Math.max(in + 1, prev);
			c.fill(x + in, y + i, x + span, y + i + 1, color);
			c.fill(x + w - span, y + i, x + w - in, y + i + 1, color);
			c.fill(x + in, y + h - i - 1, x + span, y + h - i, color);
			c.fill(x + w - span, y + h - i - 1, x + w - in, y + h - i, color);
		}
	}

	/** Miękki cień pod prostokątem. */
	public static void shadow(DrawContext c, int x, int y, int w, int h, int r, int layers) {
		for (int i = layers; i >= 1; i--) {
			int alpha = (int) (0x1E * (1f - (i - 1f) / layers));
			round(c, x - i, y - i + 3, w + 2 * i, h + 2 * i, r + i, alpha << 24);
		}
	}

	/** Poświata wokół okrągłego elementu (np. gałki suwaka). */
	public static void glow(DrawContext c, int cx, int cy, int radius, int rgb, int alpha) {
		for (int i = 3; i >= 1; i--) {
			int rr = radius + i;
			round(c, cx - rr, cy - rr, rr * 2, rr * 2, rr, withAlpha(rgb, alpha));
		}
	}

	// ------------------------------------------------------------------ tekst

	public static void text(DrawContext c, TextRenderer tr, String s, int x, int y, int color, boolean shadow) {
		color = fade(color);
		if ((color >>> 24) < 6) {
			return;
		}
		c.drawText(tr, s, x, y, color, shadow);
	}

	public static void textCentered(DrawContext c, TextRenderer tr, String s, int cx, int y, int color, boolean shadow) {
		text(c, tr, s, cx - tr.getWidth(s) / 2, y, color, shadow);
	}

	public static void scaledText(DrawContext c, TextRenderer tr, String s, int x, int y, float scale,
								  int color, boolean shadow) {
		color = fade(color);
		if ((color >>> 24) < 6) {
			return;
		}
		c.getMatrices().push();
		c.getMatrices().translate((float) x, (float) y, 0f);
		c.getMatrices().scale(scale, scale, 1f);
		c.drawText(tr, s, 0, 0, color, shadow);
		c.getMatrices().pop();
	}

	// ------------------------------------------------------------------ elementy złożone

	public static void panel(DrawContext c, int x, int y, int w, int h) {
		shadow(c, x, y, w, h, 7, 8);
		round(c, x, y, w, h, 7, 0xF2101722);
		gradient(c, x + 1, y + 7, w - 2, h - 14, withAlpha(accent(), 0x14), 0x00000000);
		roundOutline(c, x, y, w, h, 7, withAlpha(accent(), 0x80));
	}

	/** Przycisk z zaokrągleniem; hover od 0 do 1 (animowany przez wywołującego). */
	public static void button(DrawContext c, TextRenderer tr, int x, int y, int w, int h,
							  String text, float hover, int color) {
		int base = lerp(shade(color, 0.78f), color, hover);
		round(c, x, y + 2, w, h, 4, 0x55000000);
		round(c, x, y, w, h, 4, base);
		gradient(c, x + 2, y + 1, w - 4, Math.max(1, h / 2 - 1), 0x34FFFFFF, 0x00FFFFFF);
		textCentered(c, tr, text, x + w / 2, y + (h - 8) / 2, 0xFF0B1018, false);
	}

	public static void drawPenguin(DrawContext c, int x, int y, int scale) {
		for (int row = 0; row < PENGUIN.length; row++) {
			String line = PENGUIN[row];
			for (int col = 0; col < line.length(); col++) {
				int color;
				switch (line.charAt(col)) {
					case 'K':
						color = 0xFF2B3A55;
						break;
					case 'W':
						color = 0xFFFFFFFF;
						break;
					case 'O':
						color = 0xFFFFA726;
						break;
					default:
						continue;
				}
				rect(c, x + col * scale, y + row * scale, scale, scale, color);
			}
		}
	}

	/** Rysuje ikonę z tablicy tekstowej ('#' = piksel). */
	public static void drawIcon(DrawContext c, String[] rows, int x, int y, int color) {
		for (int row = 0; row < rows.length; row++) {
			String line = rows[row];
			for (int col = 0; col < line.length(); col++) {
				if (line.charAt(col) == '#') {
					rect(c, x + col, y + row, 1, 1, color);
				}
			}
		}
	}

	/** Mały krzyżyk 7x7 (przycisk zamykania). */
	public static void drawCross(DrawContext c, int x, int y, int color) {
		for (int i = 0; i < 7; i++) {
			rect(c, x + i, y + i, 1, 1, color);
			rect(c, x + 6 - i, y + i, 1, 1, color);
		}
	}
}
