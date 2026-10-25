package pl.penguin.visuals.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import pl.penguin.visuals.PenguinConfig;

/** Wspólne elementy wyglądu: kolory motywu, panel, pixel-art pingwin, przycisk. */
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

	private Ui() {
	}

	public static int accent() {
		int index = Math.floorMod(PenguinConfig.get().accentIndex, ACCENTS.length);
		return 0xFF000000 | ACCENTS[index];
	}

	public static int withAlpha(int color, int alpha) {
		return (alpha << 24) | (color & 0xFFFFFF);
	}

	public static void rect(DrawContext c, int x, int y, int w, int h, int color) {
		c.fill(x, y, x + w, y + h, color);
	}

	public static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	public static void panel(DrawContext c, int x, int y, int w, int h) {
		rect(c, x + 3, y + 3, w, h, 0x55000000);
		c.fillGradient(x, y, x + w, y + h, 0xF2121A26, 0xF20A0F17);
		int border = withAlpha(accent(), 0x90);
		rect(c, x, y, w, 1, border);
		rect(c, x, y + h - 1, w, 1, border);
		rect(c, x, y, 1, h, border);
		rect(c, x + w - 1, y, 1, h, border);
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

	public static void button(DrawContext c, TextRenderer tr, int x, int y, int w, int h,
							  String text, boolean hover, int color) {
		rect(c, x, y, w, h, withAlpha(color, hover ? 0xFF : 0xC0));
		rect(c, x, y + h - 1, w, 1, 0x40000000);
		int tx = x + (w - tr.getWidth(text)) / 2;
		int ty = y + (h - 8) / 2;
		c.drawText(tr, text, tx, ty, 0xFF0B1018, false);
	}
}
