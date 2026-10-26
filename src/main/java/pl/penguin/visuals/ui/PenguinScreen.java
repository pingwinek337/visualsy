package pl.penguin.visuals.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import pl.penguin.visuals.HitEffects;
import pl.penguin.visuals.LicenseManager;
import pl.penguin.visuals.PenguinConfig;
import pl.penguin.visuals.PenguinVisualsClient;
import pl.penguin.visuals.SkyPreset;
import pl.penguin.visuals.TargetHud;
import pl.penguin.visuals.TargetOutline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Główne menu moda (klawisz 9): zakładki z ikonami, sekcje, przewijanie i animowane kontrolki. */
public class PenguinScreen extends Screen {
	private static final String[] TABS = {"Niebo", "Walka", "Cel", "Obrys", "HUD", "Motyw", "Licencja"};

	private static final String[][] ICONS = {
		// niebo (chmura)
		{"..###...", ".#####.#", "########", "########", ".######."},
		// walka (miecz)
		{".....###", "....###.", "#..###..", ".####...", "..##....", ".#.##...", "#...#..."},
		// cel (celownik)
		{"...#...", ".#####.", ".#.#.#.", "###.###", ".#.#.#.", ".#####.", "...#..."},
		// obrys (ramka)
		{"#######.", "#.....#.", "#.....#.", "#.....#.", "#.....#.", "#.....#.", "#######."},
		// hud (monitor)
		{"########", "#......#", "#......#", "#......#", "########", "...##...", "..####.."},
		// motyw (kropla)
		{"...#...", "..###..", ".#####.", "#######", "#######", ".#####.", "..###.."},
		// licencja (klucz)
		{".###....", "#...#...", "#...#...", ".###....", "..#.....", "..##....", "..#.....", "..##...."}
	};

	private static final int SIDE = 112;
	private static final int HEAD = 36;
	private static final int FOOT = 20;
	private static final int TAB_H = 24;
	private static final int TAB_GAP = 3;
	private static final int GAP = 6;

	private static int selectedTab = 0;

	private final List<Control> controls = new ArrayList<>();
	private final float[] tabHover = new float[TABS.length];
	private final Fx.Background snow = new Fx.Background(90);

	private Control dragging;
	private int px;
	private int py;
	private int pw;
	private int ph;

	private long openAt = 0L;
	private long lastNanos = System.nanoTime();
	private float frameDt = 0.016f;
	private float tabAnim = selectedTab;
	private float closeHover = 0f;

	private float scroll = 0f;
	private float scrollTarget = 0f;
	private int contentHeight = 0;
	private int cursor = 0;

	public PenguinScreen() {
		super(Text.literal("Penguin Visuals"));
	}

	@Override
	protected void init() {
		pw = Math.min(450, width - 16);
		ph = Math.min(296, height - 16);
		px = (width - pw) / 2;
		py = (height - ph) / 2;
		if (openAt == 0L) {
			openAt = System.currentTimeMillis();
		}
		rebuild();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	// ------------------------------------------------------------------ geometria

	private int contentTop() {
		return py + HEAD + 10;
	}

	private int contentBottom() {
		return py + ph - FOOT - 4;
	}

	private int contentLeft() {
		return px + SIDE + 14;
	}

	private int contentWidth() {
		return pw - SIDE - 14 - 22;
	}

	private int maxScroll() {
		return Math.max(0, contentHeight - (contentBottom() - contentTop()));
	}

	private boolean inViewport(double mx, double my) {
		return Ui.inside(mx, my, px + SIDE + 1, contentTop(), pw - SIDE - 2, contentBottom() - contentTop());
	}

	// ------------------------------------------------------------------ zawartość zakładek

	private <T extends Control> T add(T control) {
		control.x = contentLeft();
		control.w = contentWidth();
		control.rel = cursor;
		controls.add(control);
		cursor += control.h + GAP;
		return control;
	}

	private void section(String title) {
		if (cursor > 0) {
			cursor += 4;
		}
		add(new Section(title));
	}

	private void rebuild() {
		controls.clear();
		cursor = 0;
		scroll = 0f;
		scrollTarget = 0f;
		PenguinConfig cfg = PenguinConfig.get();

		switch (selectedTab) {
			case 0 -> {
				section("Niebo");
				add(new Toggle("Własne niebo", () -> cfg.skyEnabled, v -> cfg.skyEnabled = v));
				add(new Cycle("Kolor nieba",
					() -> SkyPreset.current().label,
					() -> cfg.skyPreset = SkyPreset.current().next().name(),
					() -> cfg.skyPreset = SkyPreset.current().previous().name())
					.swatch(() -> 0xFF000000 | SkyPreset.current().rgb()));
				section("Własny kolor");
				add(new Slider("Czerwony", 0, 255, () -> (double) cfg.customR, v -> {
					cfg.customR = (int) Math.round(v);
					cfg.skyPreset = SkyPreset.CUSTOM.name();
				}, v -> String.valueOf(Math.round(v))).tint(0xFFFF5C5C));
				add(new Slider("Zielony", 0, 255, () -> (double) cfg.customG, v -> {
					cfg.customG = (int) Math.round(v);
					cfg.skyPreset = SkyPreset.CUSTOM.name();
				}, v -> String.valueOf(Math.round(v))).tint(0xFF5CFF8A));
				add(new Slider("Niebieski", 0, 255, () -> (double) cfg.customB, v -> {
					cfg.customB = (int) Math.round(v);
					cfg.skyPreset = SkyPreset.CUSTOM.name();
				}, v -> String.valueOf(Math.round(v))).tint(0xFF5C9BFF));
				add(new SkyPreview());
			}
			case 1 -> {
				section("Dźwięk uderzenia");
				add(new Toggle("Dźwięk włączony", () -> cfg.hitSound, v -> cfg.hitSound = v));
				add(new Cycle("Dźwięk",
					() -> HitEffects.SOUND_NAMES[Math.floorMod(cfg.hitSoundIndex, HitEffects.SOUND_NAMES.length)],
					() -> cfg.hitSoundIndex = Math.floorMod(cfg.hitSoundIndex + 1, HitEffects.SOUND_NAMES.length),
					() -> cfg.hitSoundIndex = Math.floorMod(cfg.hitSoundIndex - 1, HitEffects.SOUND_NAMES.length)));
				add(new Slider("Głośność", 0.1, 1.0, () -> (double) cfg.hitVolume,
					v -> cfg.hitVolume = (float) (double) v,
					v -> Math.round(v * 100) + "%"));
				add(new Slider("Wysokość tonu", 0.5, 2.0, () -> (double) cfg.hitPitch,
					v -> cfg.hitPitch = (float) (double) v,
					v -> String.format(Locale.ROOT, "%.2f", v)));
				add(new Button("Odtwórz próbkę", HitEffects::playSound, 0xFF000000 | Ui.ACCENTS[0]));
				section("Cząsteczki");
				add(new Toggle("Cząsteczki włączone", () -> cfg.hitParticles, v -> cfg.hitParticles = v));
				add(new Cycle("Rodzaj",
					() -> HitEffects.PARTICLE_NAMES[Math.floorMod(cfg.particleIndex, HitEffects.PARTICLE_NAMES.length)],
					() -> cfg.particleIndex = Math.floorMod(cfg.particleIndex + 1, HitEffects.PARTICLE_NAMES.length),
					() -> cfg.particleIndex = Math.floorMod(cfg.particleIndex - 1, HitEffects.PARTICLE_NAMES.length)));
			}
			case 2 -> {
				section("Panel gracza (HP)");
				add(new Toggle("Panel włączony", () -> cfg.targetPanel, v -> cfg.targetPanel = v));
				add(new Toggle("Pokaż nick", () -> cfg.panelNick, v -> cfg.panelNick = v));
				add(new Toggle("Serduszka nad głową", () -> cfg.targetHearts, v -> cfg.targetHearts = v));
				add(new Slider("Rozmiar serduszek", 1, 3, () -> (double) cfg.heartScale,
					v -> cfg.heartScale = (int) Math.round(v),
					v -> String.valueOf(Math.round(v))));
				section("Pozycja panelu");
				add(new Cycle("Miejsce",
					() -> TargetHud.ANCHOR_NAMES[Math.floorMod(cfg.panelAnchor, TargetHud.ANCHOR_NAMES.length)],
					() -> cfg.panelAnchor = Math.floorMod(cfg.panelAnchor + 1, TargetHud.ANCHOR_NAMES.length),
					() -> cfg.panelAnchor = Math.floorMod(cfg.panelAnchor - 1, TargetHud.ANCHOR_NAMES.length)));
				add(new Slider("Przesunięcie X", 0, 400, () -> (double) cfg.panelOffsetX,
					v -> cfg.panelOffsetX = (int) Math.round(v),
					v -> Math.round(v) + " px"));
				add(new Slider("Przesunięcie Y", 0, 300, () -> (double) cfg.panelOffsetY,
					v -> cfg.panelOffsetY = (int) Math.round(v),
					v -> Math.round(v) + " px"));
				section("Wykrywanie");
				add(new Slider("Zasięg", 5, 64, () -> (double) cfg.targetRange,
					v -> cfg.targetRange = (float) (double) v,
					v -> Math.round(v) + " m"));
			}
			case 3 -> {
				section("Obramowanie celu");
				add(new Toggle("Obramowanie włączone", () -> cfg.outline, v -> cfg.outline = v));
				add(new Cycle("Styl",
					() -> TargetOutline.STYLE_NAMES[Math.floorMod(cfg.outlineStyle, TargetOutline.STYLE_NAMES.length)],
					() -> cfg.outlineStyle = Math.floorMod(cfg.outlineStyle + 1, TargetOutline.STYLE_NAMES.length),
					() -> cfg.outlineStyle = Math.floorMod(cfg.outlineStyle - 1, TargetOutline.STYLE_NAMES.length)));
				add(new Cycle("Kolor",
					() -> TargetOutline.COLOR_NAMES[Math.floorMod(cfg.outlineColor, TargetOutline.COLOR_NAMES.length)],
					() -> cfg.outlineColor = Math.floorMod(cfg.outlineColor + 1, TargetOutline.COLOR_NAMES.length),
					() -> cfg.outlineColor = Math.floorMod(cfg.outlineColor - 1, TargetOutline.COLOR_NAMES.length))
					.swatch(() -> 0xFF000000 | TargetOutline.swatch(cfg.outlineColor)));
				add(new Slider("Szybkość", 0.2, 3.0, () -> (double) cfg.outlineSpeed,
					v -> cfg.outlineSpeed = (float) (double) v,
					v -> String.format(Locale.ROOT, "x%.1f", v)));
				add(new Slider("Grubość", 1, 4, () -> (double) cfg.outlineThickness,
					v -> cfg.outlineThickness = (int) Math.round(v),
					v -> String.valueOf(Math.round(v))));
				section("Podgląd");
				add(new OutlinePreview());
			}
			case 4 -> {
				section("Informacje na ekranie");
				add(new Toggle("Znak wodny", () -> cfg.hudWatermark, v -> cfg.hudWatermark = v));
				add(new Toggle("Licznik FPS", () -> cfg.hudFps, v -> cfg.hudFps = v));
				add(new Toggle("Współrzędne", () -> cfg.hudCoords, v -> cfg.hudCoords = v));
			}
			case 5 -> {
				section("Kolor motywu");
				add(new Swatches());
				add(new Label("Zmienia kolor menu, HUD-u i obramowania.", 0xFF8B95A7));
				section("Efekt tła");
				add(new Cycle("Efekt",
					() -> Fx.EFFECT_NAMES[Math.floorMod(cfg.bgEffect, Fx.EFFECT_NAMES.length)],
					() -> cfg.bgEffect = Math.floorMod(cfg.bgEffect + 1, Fx.EFFECT_NAMES.length),
					() -> cfg.bgEffect = Math.floorMod(cfg.bgEffect - 1, Fx.EFFECT_NAMES.length)));
				add(new Toggle("Pokaż też w grze", () -> cfg.bgInGame, v -> cfg.bgInGame = v));
			}
			default -> {
				section("Twoja licencja");
				add(new Card());
				add(new Button("Dezaktywuj licencję", () -> {
					LicenseManager.deactivate();
					if (client != null) {
						client.setScreen(new LicenseScreen());
					}
				}, 0xFFFF6B6B));
			}
		}
		contentHeight = Math.max(0, cursor - GAP);
	}

	// ------------------------------------------------------------------ rysowanie

	@Override
	public void render(DrawContext c, int mouseX, int mouseY, float delta) {
		long now = System.nanoTime();
		frameDt = Math.min(0.1f, (now - lastNanos) / 1_000_000_000f);
		lastNanos = now;

		float open = Ui.ease((System.currentTimeMillis() - openAt) / 300f);
		Ui.setAlpha(open);
		try {
			Ui.gradient(c, 0, 0, width, height, 0xB4080C16, 0xD2030509);
			snow.render(c, width, height);

			c.getMatrices().push();
			c.getMatrices().translate(0f, (1f - open) * 18f, 0f);
			drawFrame(c, mouseX, mouseY);
			drawSidebar(c, mouseX, mouseY);
			drawContent(c, mouseX, mouseY);
			c.getMatrices().pop();
		} finally {
			Ui.setAlpha(1f);
		}
	}

	private void drawFrame(DrawContext c, int mx, int my) {
		Ui.panel(c, px, py, pw, ph);

		// nagłówek
		int headColor = 0xFF161F2C;
		Ui.round(c, px + 1, py + 1, pw - 2, HEAD, 6, headColor);
		Ui.rect(c, px + 1, py + HEAD - 6, pw - 2, 6, headColor);
		Ui.rect(c, px + 1, py + HEAD, pw - 2, 1, Ui.withAlpha(Ui.accent(), 0x40));

		// błysk przesuwający się po linii pod nagłówkiem
		float phase = (System.currentTimeMillis() % 3200L) / 3200f;
		for (int sx = px + 1; sx < px + pw - 1; sx += 2) {
			float pos = (sx - px) / (float) pw;
			float dist = Math.abs(pos - phase);
			dist = Math.min(dist, 1f - dist);
			float a = Math.max(0f, 1f - dist * 7f);
			if (a > 0.02f) {
				Ui.rect(c, sx, py + HEAD - 1, 2, 2, Ui.withAlpha(Ui.accent(), (int) (a * 255)));
			}
		}

		// pingwin i tytuł
		int bob = (int) Math.round(Math.sin(System.currentTimeMillis() / 380.0) * 1.0);
		Ui.drawPenguin(c, px + 11, py + 7 + bob, 2);
		float scale = 1.25f;
		Ui.scaledText(c, textRenderer, "PENGUIN", px + 40, py + 8, scale, Ui.accent(), true);
		int titleW = (int) (textRenderer.getWidth("PENGUIN ") * scale);
		Ui.scaledText(c, textRenderer, "VISUALS", px + 40 + titleW, py + 8, scale, 0xFFFFFFFF, true);
		Ui.text(c, textRenderer, "Fabric 1.21.4  •  v1.0.0", px + 41, py + 22, 0xFF6B7587, false);

		// przycisk zamykania
		int bx = px + pw - 28;
		int by = py + 8;
		boolean hover = Ui.inside(mx, my, bx, by, 20, 20);
		closeHover += ((hover ? 1f : 0f) - closeHover) * Math.min(1f, frameDt * 16f);
		Ui.round(c, bx, by, 20, 20, 5, Ui.withAlpha(0xFF5C5C, (int) (0x60 * closeHover)));
		Ui.drawCross(c, bx + 7, by + 7, Ui.lerp(0xFF8B95A7, 0xFFFFFFFF, closeHover));

		// stopka
		Ui.rect(c, px + 3, py + ph - FOOT, pw - 6, 1, 0xFF1F2938);
		Ui.rect(c, px + 3, py + ph - FOOT + 1, pw - 6, FOOT - 4, 0x30000000);
		Ui.round(c, px + 12, py + ph - FOOT + 7, 6, 6, 3, 0xFF6BE08A);
		Ui.text(c, textRenderer, "Licencja aktywna", px + 24, py + ph - FOOT + 6, 0xFF8B95A7, false);
		String hint = "9 / ESC - zamknij   •   kółko myszy - przewijanie";
		Ui.text(c, textRenderer, hint, px + pw - 12 - textRenderer.getWidth(hint), py + ph - FOOT + 6, 0xFF55607A, false);
	}

	private void drawSidebar(DrawContext c, int mx, int my) {
		Ui.rect(c, px + 2, py + HEAD + 1, SIDE - 2, ph - HEAD - FOOT - 1, 0x2A000000);
		Ui.rect(c, px + SIDE, py + HEAD + 1, 1, ph - HEAD - FOOT - 1, 0xFF1F2938);

		int sx = px + 8;
		int sw = SIDE - 16;
		int startY = py + HEAD + 10;

		tabAnim += (selectedTab - tabAnim) * Math.min(1f, frameDt * 14f);
		int indicatorY = startY + Math.round(tabAnim * (TAB_H + TAB_GAP));
		Ui.round(c, sx, indicatorY, sw, TAB_H, 5, Ui.withAlpha(Ui.accent(), 0x34));
		Ui.round(c, sx, indicatorY + 5, 3, TAB_H - 10, 1, Ui.accent());

		for (int i = 0; i < TABS.length; i++) {
			int ty = startY + i * (TAB_H + TAB_GAP);
			boolean hover = Ui.inside(mx, my, sx, ty, sw, TAB_H);
			tabHover[i] += ((hover ? 1f : 0f) - tabHover[i]) * Math.min(1f, frameDt * 16f);

			if (tabHover[i] > 0.02f) {
				Ui.round(c, sx, ty, sw, TAB_H, 5, Ui.withAlpha(0xFFFFFF, (int) (0x14 * tabHover[i])));
			}
			float active = Math.max(0f, 1f - Math.abs(tabAnim - i));
			float glow = Math.max(active, tabHover[i] * 0.7f);
			int color = Ui.lerp(0xFF7E8AA0, 0xFFFFFFFF, glow);

			String[] icon = ICONS[i];
			Ui.drawIcon(c, icon, sx + 9, ty + (TAB_H - icon.length) / 2, Ui.lerp(color, Ui.accent(), active));
			Ui.text(c, textRenderer, TABS[i], sx + 24, ty + 8, color, false);
		}
	}

	private void drawContent(DrawContext c, int mx, int my) {
		int top = contentTop();
		int bottom = contentBottom();

		scrollTarget = Math.max(0f, Math.min(maxScroll(), scrollTarget));
		scroll += (scrollTarget - scroll) * Math.min(1f, frameDt * 16f);

		c.enableScissor(px + SIDE + 1, top, px + pw - 2, bottom);
		for (Control control : controls) {
			control.y = top + control.rel - Math.round(scroll);
			if (control.y + control.h < top || control.y > bottom) {
				continue;
			}
			boolean hov = (dragging == null || dragging == control)
				&& inViewport(mx, my) && control.contains(mx, my);
			control.hover += ((hov ? 1f : 0f) - control.hover) * Math.min(1f, frameDt * 16f);
			control.render(c, mx, my);
		}
		c.disableScissor();

		// pasek przewijania
		int max = maxScroll();
		if (max > 0) {
			int trackH = bottom - top;
			int thumbH = Math.max(18, trackH * trackH / contentHeight);
			int thumbY = top + Math.round((trackH - thumbH) * (scroll / max));
			int tx = px + pw - 9;
			Ui.round(c, tx, top, 3, trackH, 1, 0x30FFFFFF);
			Ui.round(c, tx, thumbY, 3, thumbH, 1, Ui.withAlpha(Ui.accent(), 0xC0));
		}
	}

	// ------------------------------------------------------------------ sterowanie

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == 0) {
			if (Ui.inside(mx, my, px + pw - 28, py + 8, 20, 20)) {
				close();
				return true;
			}
			for (int i = 0; i < TABS.length; i++) {
				int ty = py + HEAD + 10 + i * (TAB_H + TAB_GAP);
				if (Ui.inside(mx, my, px + 8, ty, SIDE - 16, TAB_H)) {
					if (selectedTab != i) {
						selectedTab = i;
						rebuild();
					}
					return true;
				}
			}
		}
		if (inViewport(mx, my)) {
			for (Control control : controls) {
				if (control.contains(mx, my) && control.click(mx, my, button)) {
					PenguinConfig.save();
					return true;
				}
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
	public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
		if (maxScroll() > 0 && inViewport(mx, my)) {
			scrollTarget = (float) Math.max(0, Math.min(maxScroll(), scrollTarget - vertical * 28));
			return true;
		}
		return super.mouseScrolled(mx, my, horizontal, vertical);
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
		int h = 20;
		int rel;
		float hover = 0f;

		abstract void render(DrawContext c, int mx, int my);

		boolean click(double mx, double my, int button) {
			return false;
		}

		void drag(double mx) {
		}

		boolean contains(double mx, double my) {
			return Ui.inside(mx, my, x - 4, y, w + 8, h);
		}

		void rowHover(DrawContext c) {
			if (hover > 0.02f) {
				Ui.round(c, x - 5, y, w + 10, h, 5, Ui.withAlpha(0xFFFFFF, (int) (0x12 * hover)));
			}
		}
	}

	private class Section extends Control {
		private final String title;

		Section(String title) {
			this.title = title.toUpperCase(Locale.ROOT);
			this.h = 12;
		}

		@Override
		boolean contains(double mx, double my) {
			return false;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			Ui.text(c, textRenderer, title, x, y + 2, Ui.accent(), false);
			int lineX = x + textRenderer.getWidth(title) + 8;
			Ui.rect(c, lineX, y + 6, x + w - lineX, 1, Ui.withAlpha(Ui.accent(), 0x40));
		}
	}

	private class Toggle extends Control {
		private final String label;
		private final BooleanSupplier get;
		private final Consumer<Boolean> set;
		private float on = -1f;

		Toggle(String label, BooleanSupplier get, Consumer<Boolean> set) {
			this.label = label;
			this.get = get;
			this.set = set;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			rowHover(c);
			float target = get.getAsBoolean() ? 1f : 0f;
			on = on < 0f ? target : on + (target - on) * Math.min(1f, frameDt * 18f);

			Ui.text(c, textRenderer, label, x, y + 6, 0xFFD7DEE9, false);

			int pillW = 30;
			int pillH = 14;
			int bx = x + w - pillW;
			int by = y + 3;
			int track = Ui.lerp(0xFF2C3545, Ui.accent(), on);
			Ui.round(c, bx, by, pillW, pillH, 7, track);
			Ui.roundOutline(c, bx, by, pillW, pillH, 7, Ui.withAlpha(0xFFFFFF, 0x1C));
			int kx = bx + 2 + Math.round(on * (pillW - pillH));
			Ui.round(c, kx, by + 2, 10, 10, 5, 0xFFFFFFFF);
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
		private int tint = 0;

		Slider(String label, double min, double max, Supplier<Double> get, Consumer<Double> set,
			   Function<Double, String> format) {
			this.label = label;
			this.min = min;
			this.max = max;
			this.get = get;
			this.set = set;
			this.format = format;
			this.h = 28;
		}

		Slider tint(int color) {
			this.tint = color;
			return this;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			rowHover(c);
			double value = get.get();
			int color = tint != 0 ? tint : Ui.accent();

			Ui.text(c, textRenderer, label, x, y + 2, 0xFFD7DEE9, false);
			String text = format.apply(value);
			int pillW = textRenderer.getWidth(text) + 12;
			Ui.round(c, x + w - pillW, y, pillW, 12, 4, 0xFF1A2230);
			Ui.text(c, textRenderer, text, x + w - pillW + 6, y + 2, color, false);

			int ty = y + 18;
			Ui.round(c, x, ty, w, 4, 2, 0xFF273041);
			double t = Math.max(0, Math.min(1, (value - min) / (max - min)));
			int filled = (int) Math.round(t * w);
			if (filled > 0) {
				Ui.round(c, x, ty, Math.max(4, filled), 4, 2, color);
			}

			int kx = x + Math.max(0, Math.min(w - 10, filled - 5));
			float active = dragging == this ? 1f : hover;
			if (active > 0.05f) {
				Ui.glow(c, kx + 5, ty + 2, 5, color, (int) (0x1C * active));
			}
			Ui.round(c, kx, ty - 3, 10, 10, 5, 0xFFFFFFFF);
			Ui.round(c, kx + 2, ty - 1, 6, 6, 3, color);
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
		private Supplier<Integer> swatch;

		Cycle(String label, Supplier<String> value, Runnable next, Runnable previous) {
			this.label = label;
			this.value = value;
			this.next = next;
			this.previous = previous;
			this.h = 22;
		}

		Cycle swatch(Supplier<Integer> swatch) {
			this.swatch = swatch;
			return this;
		}

		private int boxWidth() {
			return 136;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			rowHover(c);
			int bw = boxWidth();
			int bx = x + w - bw;
			boolean in = contains(mx, my) && inViewport(mx, my);
			boolean left = in && mx >= bx && mx < bx + 22;
			boolean right = in && mx >= bx + bw - 22 && mx < bx + bw;

			Ui.text(c, textRenderer, label, x, y + 7, 0xFFD7DEE9, false);
			Ui.round(c, bx, y, bw, h, 5, 0xFF1A2230);
			Ui.roundOutline(c, bx, y, bw, h, 5, Ui.withAlpha(Ui.accent(), (int) (0x44 + 0x55 * hover)));

			Ui.text(c, textRenderer, "<", bx + 8, y + 7, left ? Ui.accent() : 0xFF6B7587, false);
			Ui.text(c, textRenderer, ">", bx + bw - 14, y + 7, right ? Ui.accent() : 0xFF6B7587, false);

			String text = value.get();
			int textW = textRenderer.getWidth(text);
			int sw = swatch != null ? 12 : 0;
			int tx = bx + (bw - textW - sw) / 2 + sw;
			if (swatch != null) {
				Ui.round(c, tx - 12, y + 7, 8, 8, 2, swatch.get());
			}
			Ui.text(c, textRenderer, text, tx, y + 7, 0xFFFFFFFF, false);
		}

		@Override
		boolean click(double mx, double my, int button) {
			int bw = boxWidth();
			int bx = x + w - bw;
			if (mx < bx) {
				return false;
			}
			if (button == 1 || (button == 0 && mx < bx + 22)) {
				previous.run();
				return true;
			}
			if (button == 0) {
				next.run();
				return true;
			}
			return false;
		}
	}

	private class Button extends Control {
		private final String text;
		private final Runnable action;
		private final int color;

		Button(String text, Runnable action, int color) {
			this.text = text;
			this.action = action;
			this.color = color;
			this.h = 22;
		}

		@Override
		boolean contains(double mx, double my) {
			return Ui.inside(mx, my, x, y, w, h);
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			Ui.button(c, textRenderer, x, y, w, h, text, hover, color);
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
			this.h = 12;
		}

		@Override
		boolean contains(double mx, double my) {
			return false;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			Ui.text(c, textRenderer, text, x, y + 2, color, false);
		}
	}

	/** Karta ze statusem licencji. */
	private class Card extends Control {
		Card() {
			this.h = 62;
		}

		@Override
		boolean contains(double mx, double my) {
			return false;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			int green = 0xFF6BE08A;
			Ui.round(c, x, y, w, h, 6, 0xFF151D29);
			Ui.roundOutline(c, x, y, w, h, 6, Ui.withAlpha(green, 0x60));
			Ui.glow(c, x + 14, y + 15, 4, green, 0x1A);
			Ui.round(c, x + 10, y + 11, 8, 8, 4, green);
			Ui.text(c, textRenderer, "Licencja aktywna", x + 26, y + 11, green, true);
			Ui.text(c, textRenderer, "Klucz:  " + LicenseManager.maskedKey(), x + 10, y + 29, 0xFFD7DEE9, false);
			Ui.text(c, textRenderer, "Wersja moda:  1.0.0", x + 10, y + 43, 0xFF8B95A7, false);
		}
	}

	/** Wybór koloru motywu jako kolorowe kafelki. */
	private class Swatches extends Control {
		private static final int SIZE = 30;
		private static final int PER_ROW = 6;
		private static final int ROW_H = SIZE + 16;

		Swatches() {
			int rows = (Ui.ACCENTS.length + PER_ROW - 1) / PER_ROW;
			this.h = rows * ROW_H - 4;
		}

		@Override
		boolean contains(double mx, double my) {
			return Ui.inside(mx, my, x, y, w, h);
		}

		private int gap() {
			return Math.max(4, (w - PER_ROW * SIZE) / (PER_ROW - 1));
		}

		private int cellX(int i) {
			return x + (i % PER_ROW) * (SIZE + gap());
		}

		private int cellY(int i) {
			return y + (i / PER_ROW) * ROW_H;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			PenguinConfig cfg = PenguinConfig.get();
			int selected = Math.floorMod(cfg.accentIndex, Ui.ACCENTS.length);
			for (int i = 0; i < Ui.ACCENTS.length; i++) {
				int sx = cellX(i);
				int sy = cellY(i);
				int color = 0xFF000000 | Ui.ACCENTS[i];
				boolean hov = Ui.inside(mx, my, sx, sy, SIZE, SIZE);
				if (i == selected) {
					Ui.glow(c, sx + SIZE / 2, sy + SIZE / 2, SIZE / 2 - 2, color, 0x16);
					Ui.round(c, sx - 2, sy - 2, SIZE + 4, SIZE + 4, 9, 0xFFFFFFFF);
					Ui.round(c, sx - 1, sy - 1, SIZE + 2, SIZE + 2, 8, 0xFF101722);
				}
				Ui.round(c, sx, sy, SIZE, SIZE, 7, hov ? Ui.shade(color, 1.15f) : color);
				Ui.gradient(c, sx + 2, sy + 1, SIZE - 4, SIZE / 2 - 2, 0x30FFFFFF, 0x00FFFFFF);
				Ui.textCentered(c, textRenderer, Ui.ACCENT_NAMES[i], sx + SIZE / 2, sy + SIZE + 5,
					i == selected ? 0xFFFFFFFF : 0xFF8B95A7, false);
			}
		}

		@Override
		boolean click(double mx, double my, int button) {
			if (button != 0) {
				return false;
			}
			for (int i = 0; i < Ui.ACCENTS.length; i++) {
				if (Ui.inside(mx, my, cellX(i), cellY(i), SIZE, SIZE)) {
					PenguinConfig.get().accentIndex = i;
					return true;
				}
			}
			return false;
		}
	}

	/** Pasek z podglądem koloru nieba. */
	private class SkyPreview extends Control {
		SkyPreview() {
			this.h = 28;
		}

		@Override
		boolean contains(double mx, double my) {
			return false;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			int rgb = SkyPreset.current().rgb();
			Ui.round(c, x, y, w, h, 6, 0xFF000000 | rgb);
			Ui.gradient(c, x + 2, y + h / 2, w - 4, h / 2 - 3, 0x00000000, 0x70000000);
			Ui.gradient(c, x + 2, y + 2, w - 4, h / 3, 0x30FFFFFF, 0x00FFFFFF);
			Ui.roundOutline(c, x, y, w, h, 6, 0x30FFFFFF);
			Ui.textCentered(c, textRenderer, "Podgląd nieba: " + SkyPreset.current().label, x + w / 2, y + 10, 0xFFFFFFFF, true);
		}
	}

	/** Podgląd animowanego obramowania z pingwinem. */
	private class OutlinePreview extends Control {
		OutlinePreview() {
			this.h = 70;
		}

		@Override
		boolean contains(double mx, double my) {
			return false;
		}

		@Override
		void render(DrawContext c, int mx, int my) {
			Ui.round(c, x, y, w, h, 6, 0xFF0D131C);
			Ui.roundOutline(c, x, y, w, h, 6, 0xFF1F2938);
			int cx = x + w / 2;
			Ui.drawPenguin(c, cx - 15, y + h / 2 - 16, 3);
			TargetOutline.draw(c, cx - 28, y + 6, cx + 28, y + h - 6, PenguinConfig.get());
		}
	}
}
