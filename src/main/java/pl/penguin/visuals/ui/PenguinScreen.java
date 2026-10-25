package pl.penguin.visuals.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import pl.penguin.visuals.HitEffects;
import pl.penguin.visuals.LicenseManager;
import pl.penguin.visuals.PenguinConfig;
import pl.penguin.visuals.PenguinVisualsClient;
import pl.penguin.visuals.SkyPreset;
import pl.penguin.visuals.TargetOutline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Główne menu moda (klawisz 9) z zakładkami. */
public class PenguinScreen extends Screen {
	private static final String[] TABS = {"Niebo", "Walka", "Cel", "Obrys", "HUD", "Motyw", "Licencja"};
	private static final int PW = 390;
	private static final int PH = 244;
	private static final int SIDE = 98;
	private static final int HEAD = 28;

	private static int selectedTab = 0;

	private final List<Control> controls = new ArrayList<>();
	private Control dragging;
	private int px;
	private int py;

	public PenguinScreen() {
		super(Text.literal("Penguin Visuals"));
	}

	@Override
	protected void init() {
		px = (width - PW) / 2;
		py = (height - PH) / 2;
		rebuild();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	// ------------------------------------------------------------------ zawartość zakładek

	private void rebuild() {
		controls.clear();
		PenguinConfig cfg = PenguinConfig.get();
		int cx = px + SIDE + 14;
		int cw = PW - SIDE - 28;
		int[] y = {py + HEAD + 14};

		switch (selectedTab) {
			case 0 -> {
				add(cx, y, cw, new Toggle("Własne niebo", () -> cfg.skyEnabled, v -> cfg.skyEnabled = v));
				add(cx, y, cw, new Cycle("Kolor nieba",
					() -> SkyPreset.current().label,
					() -> cfg.skyPreset = SkyPreset.current().next().name(),
					() -> cfg.skyPreset = SkyPreset.current().previous().name()));
				add(cx, y, cw, new Slider("Czerwony", 0, 255, () -> (double) cfg.customR, v -> {
					cfg.customR = (int) Math.round(v);
					cfg.skyPreset = SkyPreset.CUSTOM.name();
				}, v -> String.valueOf(Math.round(v))));
				add(cx, y, cw, new Slider("Zielony", 0, 255, () -> (double) cfg.customG, v -> {
					cfg.customG = (int) Math.round(v);
					cfg.skyPreset = SkyPreset.CUSTOM.name();
				}, v -> String.valueOf(Math.round(v))));
				add(cx, y, cw, new Slider("Niebieski", 0, 255, () -> (double) cfg.customB, v -> {
					cfg.customB = (int) Math.round(v);
					cfg.skyPreset = SkyPreset.CUSTOM.name();
				}, v -> String.valueOf(Math.round(v))));
				add(cx, y, cw, new Preview());
			}
			case 1 -> {
				add(cx, y, cw, new Toggle("Dźwięk uderzenia", () -> cfg.hitSound, v -> cfg.hitSound = v));
				add(cx, y, cw, new Cycle("Dźwięk",
					() -> HitEffects.SOUND_NAMES[Math.floorMod(cfg.hitSoundIndex, HitEffects.SOUND_NAMES.length)],
					() -> cfg.hitSoundIndex = Math.floorMod(cfg.hitSoundIndex + 1, HitEffects.SOUND_NAMES.length),
					() -> cfg.hitSoundIndex = Math.floorMod(cfg.hitSoundIndex - 1, HitEffects.SOUND_NAMES.length)));
				add(cx, y, cw, new Slider("Głośność", 0.1, 1.0, () -> (double) cfg.hitVolume,
					v -> cfg.hitVolume = (float) (double) v,
					v -> Math.round(v * 100) + "%"));
				add(cx, y, cw, new Slider("Wysokość tonu", 0.5, 2.0, () -> (double) cfg.hitPitch,
					v -> cfg.hitPitch = (float) (double) v,
					v -> String.format(Locale.ROOT, "%.2f", v)));
				add(cx, y, cw, new Button("Odtwórz próbkę", HitEffects::playSound));
				add(cx, y, cw, new Toggle("Cząsteczki", () -> cfg.hitParticles, v -> cfg.hitParticles = v));
				add(cx, y, cw, new Cycle("Rodzaj cząsteczek",
					() -> HitEffects.PARTICLE_NAMES[Math.floorMod(cfg.particleIndex, HitEffects.PARTICLE_NAMES.length)],
					() -> cfg.particleIndex = Math.floorMod(cfg.particleIndex + 1, HitEffects.PARTICLE_NAMES.length),
					() -> cfg.particleIndex = Math.floorMod(cfg.particleIndex - 1, HitEffects.PARTICLE_NAMES.length)));
			}
			case 2 -> {
				add(cx, y, cw, new Toggle("Panel przy graczu", () -> cfg.targetPanel, v -> cfg.targetPanel = v));
				add(cx, y, cw, new Toggle("Pokaż nick", () -> cfg.panelNick, v -> cfg.panelNick = v));
				add(cx, y, cw, new Toggle("Pokaż set", () -> cfg.panelSet, v -> cfg.panelSet = v));
				add(cx, y, cw, new Toggle("Pokaż wytrzymałość", () -> cfg.panelDurability, v -> cfg.panelDurability = v));
				add(cx, y, cw, new Slider("Zasięg wykrywania", 5, 64, () -> (double) cfg.targetRange,
					v -> cfg.targetRange = (float) (double) v,
					v -> Math.round(v) + " m"));
			}
			case 3 -> {
				add(cx, y, cw, new Toggle("Obramowanie", () -> cfg.outline, v -> cfg.outline = v));
				add(cx, y, cw, new Cycle("Styl",
					() -> TargetOutline.STYLE_NAMES[Math.floorMod(cfg.outlineStyle, TargetOutline.STYLE_NAMES.length)],
					() -> cfg.outlineStyle = Math.floorMod(cfg.outlineStyle + 1, TargetOutline.STYLE_NAMES.length),
					() -> cfg.outlineStyle = Math.floorMod(cfg.outlineStyle - 1, TargetOutline.STYLE_NAMES.length)));
				add(cx, y, cw, new Cycle("Kolor",
					() -> TargetOutline.COLOR_NAMES[Math.floorMod(cfg.outlineColor, TargetOutline.COLOR_NAMES.length)],
					() -> cfg.outlineColor = Math.floorMod(cfg.outlineColor + 1, TargetOutline.COLOR_NAMES.length),
					() -> cfg.outlineColor = Math.floorMod(cfg.outlineColor - 1, TargetOutline.COLOR_NAMES.length)));
				add(cx, y, cw, new Slider("Szybkość", 0.2, 3.0, () -> (double) cfg.outlineSpeed,
					v -> cfg.outlineSpeed = (float) (double) v,
					v -> String.format(Locale.ROOT, "x%.1f", v)));
				add(cx, y, cw, new Slider("Grubość", 1, 4, () -> (double) cfg.outlineThickness,
					v -> cfg.outlineThickness = (int) Math.round(v),
					v -> String.valueOf(Math.round(v))));
				add(cx, y, cw, new OutlinePreview());
			}
			case 4 -> {
				add(cx, y, cw, new Toggle("Znak wodny", () -> cfg.hudWatermark, v -> cfg.hudWatermark = v));
				add(cx, y, cw, new Toggle("Licznik FPS", () -> cfg.hudFps, v -> cfg.hudFps = v));
				add(cx, y, cw, new Toggle("Współrzędne", () -> cfg.hudCoords, v -> cfg.hudCoords = v));
			}
			case 5 -> {
				add(cx, y, cw, new Cycle("Kolor motywu",
					() -> Ui.ACCENT_NAMES[Math.floorMod(cfg.accentIndex, Ui.ACCENTS.length)],
					() -> cfg.accentIndex = Math.floorMod(cfg.accentIndex + 1, Ui.ACCENTS.length),
					() -> cfg.accentIndex = Math.floorMod(cfg.accentIndex - 1, Ui.ACCENTS.length)));
				add(cx, y, cw, new Label("Zmienia kolor menu i HUD-u.", 0xFF8B95A7));
			}
			default -> {
				add(cx, y, cw, new Label("Status: aktywna", 0xFF6BE08A));
				add(cx, y, cw, new Label("Klucz: " + LicenseManager.maskedKey(), 0xFFD7DEE9));
				add(cx, y, cw, new Label("Wersja moda: 1.0.0", 0xFF8B95A7));
				add(cx, y, cw, new Button("Dezaktywuj licencję", () -> {
					LicenseManager.deactivate();
					if (client != null) {
						client.setScreen(new LicenseScreen());
					}
				}));
			}
		}
	}

	private void add(int x, int[] y, int w, Control control) {
		control.x = x;
		control.y = y[0];
		control.w = w;
		controls.add(control);
		y[0] += control.h + 6;
	}

	// ------------------------------------------------------------------ rysowanie

	@Override
	public void render(DrawContext c, int mouseX, int mouseY, float delta) {
		c.fill(0, 0, width, height, 0x99050810);
		Ui.panel(c, px, py, PW, PH);

		// nagłówek
		c.fillGradient(px + 1, py + 1, px + PW - 1, py + HEAD, 0xFF1A2230, 0xFF131A25);
		Ui.rect(c, px + 1, py + HEAD, PW - 2, 2, Ui.accent());
		Ui.drawPenguin(c, px + 8, py + 3, 2);
		c.drawText(textRenderer, "PENGUIN", px + 36, py + 10, Ui.accent(), true);
		c.drawText(textRenderer, " VISUALS", px + 36 + textRenderer.getWidth("PENGUIN"), py + 10, 0xFFFFFFFF, true);
		String version = "v1.0.0";
		c.drawText(textRenderer, version, px + PW - 10 - textRenderer.getWidth(version), py + 10, 0xFF6B7587, false);

		// zakładki
		Ui.rect(c, px + SIDE, py + HEAD + 2, 1, PH - HEAD - 3, 0xFF222B3A);
		for (int i = 0; i < TABS.length; i++) {
			int tx = px + 8;
			int ty = py + HEAD + 12 + i * 26;
			int tw = SIDE - 14;
			boolean selected = i == selectedTab;
			boolean hover = Ui.inside(mouseX, mouseY, tx, ty, tw, 22);
			if (selected) {
				Ui.rect(c, tx, ty, tw, 22, Ui.withAlpha(Ui.accent(), 0x33));
				Ui.rect(c, tx, ty, 3, 22, Ui.accent());
			} else if (hover) {
				Ui.rect(c, tx, ty, tw, 22, 0x18FFFFFF);
			}
			c.drawText(textRenderer, TABS[i], tx + 10, ty + 7, selected ? 0xFFFFFFFF : 0xFF9AA5B8, false);
		}

		// zawartość
		for (Control control : controls) {
			control.render(c, mouseX, mouseY);
		}

		c.drawText(textRenderer, "ESC / 9 - zamknij", px + 10, py + PH - 14, 0xFF6B7587, false);
	}

	// ------------------------------------------------------------------ sterowanie

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == 0) {
			for (int i = 0; i < TABS.length; i++) {
				if (Ui.inside(mx, my, px + 8, py + HEAD + 12 + i * 26, SIDE - 14, 22)) {
					selectedTab = i;
					rebuild();
					return true;
				}
			}
		}
		for (Control control : controls) {
			if (control.contains(mx, my) && control.click(mx, my, button)) {
				PenguinConfig.save();
				return true;
			}
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
		if (dragging != null) {
			dragging.drag(mx);
			return true;
		}
		return super.mouseDragged(mx, my, button, dx, dy);
	}

	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		if (dragging != null) {
			dragging = null;
			PenguinConfig.save();
			return true;
		}
		return super.mouseReleased(mx, my, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (PenguinVisualsClient.OPEN_KEY != null && PenguinVisualsClient.OPEN_KEY.matchesKey(keyCode, scanCode)) {
			close();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void removed() {
		PenguinConfig.save();
	}

	// ------------------------------------------------------------------ kontrolki

	private abstract class Control {
		int x;
		int y;
		int w;
		int h = 18;

		abstract void render(DrawContext c, int mx, int my);

		boolean click(double mx, double my, int button) {
			return false;
		}

		void drag(double mx) {
		}

		boolean contains(double mx, double my) {
			return Ui.inside(mx, my, x, y, w, h);
		}

		void hover(DrawContext c, int mx, int my) {
			if (contains(mx, my)) {
				Ui.rect(c, x - 4, y - 1, w + 8, h + 2, 0x14FFFFFF);
			}
		}
	}

	private class Toggle extends Control {
		private final String label;
		private final BooleanSupplier get;
		private final Consumer<Boolean> set;

		Toggle(String label, BooleanSupplier get, Consumer<Boolean> set) {
			this.label = label;
			this.get = get;
			this.set = set;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			hover(c, mx, my);
			c.drawText(textRenderer, label, x, y + 5, 0xFFD7DEE9, false);
			boolean on = get.getAsBoolean();
			int pw = 28;
			int ph = 12;
			int bx = x + w - pw;
			int by = y + 3;
			Ui.rect(c, bx, by, pw, ph, on ? Ui.accent() : 0xFF3A4252);
			int kx = on ? bx + pw - ph + 1 : bx + 1;
			Ui.rect(c, kx, by + 1, ph - 2, ph - 2, 0xFFFFFFFF);
		}

		@Override
		boolean click(double mx, double my, int button) {
			if (button != 0) {
				return false;
			}
			set.accept(!get.getAsBoolean());
			return true;
		}
	}

	private class Slider extends Control {
		private final String label;
		private final double min;
		private final double max;
		private final Supplier<Double> get;
		private final Consumer<Double> set;
		private final Function<Double, String> format;

		Slider(String label, double min, double max, Supplier<Double> get, Consumer<Double> set,
			   Function<Double, String> format) {
			this.label = label;
			this.min = min;
			this.max = max;
			this.get = get;
			this.set = set;
			this.format = format;
			this.h = 22;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			hover(c, mx, my);
			double value = get.get();
			c.drawText(textRenderer, label, x, y, 0xFFD7DEE9, false);
			String text = format.apply(value);
			c.drawText(textRenderer, text, x + w - textRenderer.getWidth(text), y, Ui.accent(), false);

			int ty = y + 13;
			Ui.rect(c, x, ty, w, 4, 0xFF2A3140);
			double t = (value - min) / (max - min);
			int filled = (int) Math.round(Math.max(0, Math.min(1, t)) * w);
			Ui.rect(c, x, ty, filled, 4, Ui.accent());
			Ui.rect(c, x + Math.max(0, Math.min(w - 6, filled - 3)), ty - 3, 6, 10, 0xFFFFFFFF);
		}

		@Override
		boolean click(double mx, double my, int button) {
			if (button != 0) {
				return false;
			}
			dragging = this;
			drag(mx);
			return true;
		}

		@Override
		void drag(double mx) {
			double t = Math.max(0, Math.min(1, (mx - x) / w));
			set.accept(min + t * (max - min));
		}
	}

	private class Cycle extends Control {
		private final String label;
		private final Supplier<String> value;
		private final Runnable next;
		private final Runnable previous;

		Cycle(String label, Supplier<String> value, Runnable next, Runnable previous) {
			this.label = label;
			this.value = value;
			this.next = next;
			this.previous = previous;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			hover(c, mx, my);
			c.drawText(textRenderer, label, x, y + 5, 0xFFD7DEE9, false);
			int bw = 120;
			int bx = x + w - bw;
			Ui.rect(c, bx, y, bw, h, 0xFF1E2531);
			Ui.rect(c, bx, y + h - 1, bw, 1, Ui.withAlpha(Ui.accent(), 0x90));
			String text = "< " + value.get() + " >";
			c.drawText(textRenderer, text, bx + (bw - textRenderer.getWidth(text)) / 2, y + 5, 0xFFFFFFFF, false);
		}

		@Override
		boolean click(double mx, double my, int button) {
			if (button == 0) {
				next.run();
				return true;
			}
			if (button == 1) {
				previous.run();
				return true;
			}
			return false;
		}
	}

	private class Button extends Control {
		private final String text;
		private final Runnable action;

		Button(String text, Runnable action) {
			this.text = text;
			this.action = action;
			this.h = 20;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			Ui.button(c, textRenderer, x, y, w, h, text, contains(mx, my), Ui.accent());
		}

		@Override
		boolean click(double mx, double my, int button) {
			if (button != 0) {
				return false;
			}
			action.run();
			return true;
		}
	}

	private class Label extends Control {
		private final String text;
		private final int color;

		Label(String text, int color) {
			this.text = text;
			this.color = color;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			c.drawText(textRenderer, text, x, y + 5, color, false);
		}
	}

	private class Preview extends Control {
		Preview() {
			this.h = 14;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			SkyPreset preset = SkyPreset.current();
			int rgb = preset.rgb();
			c.drawText(textRenderer, "Podgląd", x, y + 3, 0xFFD7DEE9, false);
			int bx = x + 60;
			int bw = w - 60;
			c.fillGradient(bx, y, bx + bw, y + h, 0xFF000000 | rgb, 0xFF000000 | dim(rgb));
		}

		private int dim(int rgb) {
			int r = (int) (((rgb >> 16) & 0xFF) * 0.55);
			int g = (int) (((rgb >> 8) & 0xFF) * 0.55);
			int b = (int) ((rgb & 0xFF) * 0.55);
			return (r << 16) | (g << 8) | b;
		}
	}

	private class OutlinePreview extends Control {
		OutlinePreview() {
			this.h = 50;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			int cx = x + w / 2;
			Ui.drawPenguin(c, cx - 10, y + h / 2 - 11, 2);
			TargetOutline.draw(c, cx - 26, y + 3, cx + 26, y + h - 3, PenguinConfig.get());
		}
	}
}
