package pl.penguin.visuals;

import net.minecraft.client.gui.DrawContext;
import pl.penguin.visuals.ui.Ui;

import java.awt.Color;

/** Animowane obramowanie rysowane na ekranie wokół prostokąta (cel albo podgląd w menu). */
public final class TargetOutline {
	public static final String[] STYLE_NAMES = {"Kometa", "Płynąca", "Przerywana", "Kropki", "Narożniki"};
	public static final String[] COLOR_NAMES = {
		"Motyw", "Biały", "Czerwony", "Niebieski", "Zielony", "Fioletowy", "Żółty", "Tęcza"
	};
	private static final int[] SOLID = {0, 0xFFFFFF, 0xFF3B3B, 0x3B8BFF, 0x3BFF6B, 0xB04BFF, 0xFFE03B};

	private TargetOutline() {
	}

	public static void draw(DrawContext c, int x0, int y0, int x1, int y1, PenguinConfig cfg) {
		int w = x1 - x0;
		int h = y1 - y0;
		if (w < 6 || h < 6) {
			return;
		}

		int thickness = Math.max(1, Math.min(4, cfg.outlineThickness));
		double time = System.currentTimeMillis() / 1000.0;
		double speed = Math.max(0.1, cfg.outlineSpeed);
		double perimeter = 2.0 * (w + h);
		double head = ((time * speed * 0.4) % 1.0) * perimeter;

		switch (Math.floorMod(cfg.outlineStyle, STYLE_NAMES.length)) {
			case 0 -> comet(c, x0, y0, x1, y1, cfg, thickness, time, perimeter, head);
			case 1 -> flow(c, x0, y0, x1, y1, cfg, thickness, time, speed, perimeter);
			case 2 -> dashes(c, x0, y0, x1, y1, cfg, thickness, time, speed, perimeter);
			case 3 -> dots(c, x0, y0, x1, y1, cfg, thickness, time, perimeter, head);
			default -> corners(c, x0, y0, x1, y1, cfg, thickness, time, speed);
		}
	}

	// ------------------------------------------------------------------ style

	/** Jasny punkt z ogonem krąży wokół celu. */
	private static void comet(DrawContext c, int x0, int y0, int x1, int y1, PenguinConfig cfg, int t,
							  double time, double perimeter, double head) {
		double tail = perimeter * 0.35;
		for (double s = 0; s < perimeter; s += t) {
			double d = head - s;
			if (d < 0) {
				d += perimeter;
			}
			double a = d < tail ? 1.0 - d / tail : 0.0;
			int color = argb(rgb(cfg, time * 0.2 - d / perimeter * 0.8), Math.max(0.15, a));
			plot(c, x0, y0, x1, y1, s, t, color);
		}
	}

	/** Cała ramka z falą jasności, która przesuwa się po obwodzie. */
	private static void flow(DrawContext c, int x0, int y0, int x1, int y1, PenguinConfig cfg, int t,
							 double time, double speed, double perimeter) {
		for (double s = 0; s < perimeter; s += t) {
			double wave = 0.5 + 0.5 * Math.sin(Math.PI * 2.0 * (s / perimeter * 2.0 - time * speed * 0.6));
			int color = argb(rgb(cfg, s / perimeter - time * speed * 0.2), 0.25 + 0.75 * wave);
			plot(c, x0, y0, x1, y1, s, t, color);
		}
	}

	/** Przerywana linia, która jedzie dookoła. */
	private static void dashes(DrawContext c, int x0, int y0, int x1, int y1, PenguinConfig cfg, int t,
							   double time, double speed, double perimeter) {
		double offset = time * speed * 40.0;
		for (double s = 0; s < perimeter; s += t) {
			if (((s + offset) % 14.0) < 8.0) {
				plot(c, x0, y0, x1, y1, s, t, argb(rgb(cfg, s / perimeter + time * 0.1), 1.0));
			}
		}
	}

	/** Osiem kropek krążących po słabo widocznej ramce. */
	private static void dots(DrawContext c, int x0, int y0, int x1, int y1, PenguinConfig cfg, int t,
							 double time, double perimeter, double head) {
		int faint = argb(rgb(cfg, time * 0.1), 0.2);
		Ui.rect(c, x0, y0, x1 - x0, 1, faint);
		Ui.rect(c, x0, y1 - 1, x1 - x0, 1, faint);
		Ui.rect(c, x0, y0, 1, y1 - y0, faint);
		Ui.rect(c, x1 - 1, y0, 1, y1 - y0, faint);

		for (int i = 0; i < 8; i++) {
			double s = (head + i * perimeter / 8.0) % perimeter;
			plot(c, x0, y0, x1, y1, s, t + 2, argb(rgb(cfg, i / 8.0 + time * 0.1), 1.0));
		}
	}

	/** Same narożniki, które pulsują. */
	private static void corners(DrawContext c, int x0, int y0, int x1, int y1, PenguinConfig cfg, int t,
								double time, double speed) {
		double pulse = 0.65 + 0.35 * Math.sin(time * speed * 4.0);
		int len = Math.max(6, Math.min(x1 - x0, y1 - y0) / 4);
		int color = argb(rgb(cfg, time * 0.2), pulse);

		Ui.rect(c, x0, y0, len, t, color);
		Ui.rect(c, x0, y0, t, len, color);
		Ui.rect(c, x1 - len, y0, len, t, color);
		Ui.rect(c, x1 - t, y0, t, len, color);
		Ui.rect(c, x0, y1 - t, len, t, color);
		Ui.rect(c, x0, y1 - len, t, len, color);
		Ui.rect(c, x1 - len, y1 - t, len, t, color);
		Ui.rect(c, x1 - t, y1 - len, t, len, color);
	}

	/** Kolor próbki dla danego indeksu koloru obrysu (RGB, bez alfa) - do menu. */
	public static int swatch(int index) {
		int i = Math.floorMod(index, COLOR_NAMES.length);
		if (i == 0) {
			return Ui.accent() & 0xFFFFFF;
		}
		if (i == COLOR_NAMES.length - 1) {
			return Color.HSBtoRGB((float) ((System.currentTimeMillis() % 4000L) / 4000.0), 0.75f, 1f) & 0xFFFFFF;
		}
		return SOLID[i];
	}

	// ------------------------------------------------------------------ pomocnicze

	private static int rgb(PenguinConfig cfg, double phase) {
		int index = Math.floorMod(cfg.outlineColor, COLOR_NAMES.length);
		if (index == 0) {
			return Ui.accent() & 0xFFFFFF;
		}
		if (index == COLOR_NAMES.length - 1) {
			float hue = (float) (phase - Math.floor(phase));
			return Color.HSBtoRGB(hue, 0.75f, 1f) & 0xFFFFFF;
		}
		return SOLID[index];
	}

	private static int argb(int rgb, double alpha) {
		int a = (int) Math.round(Math.max(0.0, Math.min(1.0, alpha)) * 255.0);
		return (a << 24) | (rgb & 0xFFFFFF);
	}

	/** Rysuje kwadrat na obwodzie prostokąta. s liczone od lewego górnego rogu, zgodnie z zegarem. */
	private static void plot(DrawContext c, int x0, int y0, int x1, int y1, double s, int size, int color) {
		int w = x1 - x0;
		int h = y1 - y0;
		double px;
		double py;
		if (s < w) {
			px = x0 + s;
			py = y0;
		} else if (s < w + h) {
			px = x1;
			py = y0 + (s - w);
		} else if (s < 2 * w + h) {
			px = x1 - (s - w - h);
			py = y1;
		} else {
			px = x0;
			py = y1 - (s - 2 * w - h);
		}
		int ix = (int) Math.round(px) - size / 2;
		int iy = (int) Math.round(py) - size / 2;
		c.fill(ix, iy, ix + size, iy + size, color);
	}
}
