package pl.penguin.visuals.ui;

import net.minecraft.client.gui.DrawContext;
import pl.penguin.visuals.PenguinConfig;

import java.util.Random;

/** Efekty tła (menu i opcjonalnie ekran gry). Rodzaj efektu wybiera się w zakładce Motyw. */
public final class Fx {
	public static final String[] EFFECT_NAMES = {"Śnieg", "Deszcz", "Gwiazdy", "Iskry", "Bąbelki", "Matrix", "Brak"};
	private static final int NONE = 6;

	private Fx() {
	}

	public static final class Background {
		private final int count;
		private final float[] x;
		private final float[] y;
		private final float[] vx;
		private final float[] vy;
		private final float[] phase;
		private final int[] size;
		private final Random random = new Random();
		private int mode = -1;
		private long last = System.nanoTime();
		private float time = 0f;

		public Background(int count) {
			this.count = count;
			x = new float[count];
			y = new float[count];
			vx = new float[count];
			vy = new float[count];
			phase = new float[count];
			size = new int[count];
		}

		private void spawn(int i, int w, int h, boolean anywhere) {
			x[i] = random.nextFloat() * w;
			phase[i] = random.nextFloat() * 6.28f;
			vx[i] = 0f;
			size[i] = 1;
			switch (mode) {
				case 0 -> {
					y[i] = anywhere ? random.nextFloat() * h : -3f;
					vy[i] = 12f + random.nextFloat() * 26f;
					size[i] = random.nextInt(8) == 0 ? 2 : 1;
				}
				case 1 -> {
					y[i] = anywhere ? random.nextFloat() * h : -12f;
					vy[i] = 230f + random.nextFloat() * 170f;
					size[i] = random.nextInt(3);
				}
				case 2 -> {
					y[i] = random.nextFloat() * h;
					vy[i] = 0f;
					size[i] = random.nextInt(6) == 0 ? 2 : 1;
				}
				case 3 -> {
					y[i] = anywhere ? random.nextFloat() * h : h + 3f;
					vy[i] = -(18f + random.nextFloat() * 42f);
					vx[i] = (random.nextFloat() - 0.5f) * 14f;
					size[i] = random.nextInt(4) == 0 ? 2 : 1;
				}
				case 4 -> {
					y[i] = anywhere ? random.nextFloat() * h : h + 6f;
					vy[i] = -(10f + random.nextFloat() * 22f);
					size[i] = 3 + random.nextInt(3);
				}
				default -> {
					x[i] = (int) (random.nextFloat() * w / 6f) * 6f;
					y[i] = anywhere ? random.nextFloat() * h : -30f;
					vy[i] = 40f + random.nextFloat() * 80f;
					size[i] = 5 + random.nextInt(9);
				}
			}
		}

		public void render(DrawContext c, int w, int h) {
			int wanted = Math.floorMod(PenguinConfig.get().bgEffect, EFFECT_NAMES.length);
			long now = System.nanoTime();
			float dt = Math.min(0.05f, (now - last) / 1_000_000_000f);
			last = now;
			time += dt;

			if (wanted != mode) {
				mode = wanted;
				for (int i = 0; i < count; i++) {
					spawn(i, w, h, true);
				}
			}
			if (mode == NONE) {
				return;
			}

			for (int i = 0; i < count; i++) {
				switch (mode) {
					case 0 -> snow(c, i, dt, w, h);
					case 1 -> rain(c, i, dt, w, h);
					case 2 -> star(c, i);
					case 3 -> ember(c, i, dt, w, h);
					case 4 -> bubble(c, i, dt, w, h);
					default -> matrix(c, i, dt, w, h);
				}
			}
		}

		private void snow(DrawContext c, int i, float dt, int w, int h) {
			y[i] += vy[i] * dt;
			x[i] += (float) Math.sin(time * 0.8f + phase[i]) * 10f * dt;
			if (y[i] > h) {
				spawn(i, w, h, false);
			}
			if (x[i] < 0f) {
				x[i] += w;
			} else if (x[i] > w) {
				x[i] -= w;
			}
			int alpha = size[i] == 2 ? 0x8C : 0x4E;
			Ui.rect(c, (int) x[i], (int) y[i], size[i], size[i], (alpha << 24) | 0xFFFFFF);
		}

		private void rain(DrawContext c, int i, float dt, int w, int h) {
			y[i] += vy[i] * dt;
			if (y[i] > h) {
				spawn(i, w, h, false);
			}
			Ui.rect(c, (int) x[i], (int) y[i], 1, 6 + size[i] * 3, 0x668FB8FF);
		}

		private void star(DrawContext c, int i) {
			float twinkle = 0.5f + 0.5f * (float) Math.sin(time * 1.6f + phase[i] * 3f);
			int alpha = 0x28 + (int) (0x98 * twinkle);
			Ui.rect(c, (int) x[i], (int) y[i], size[i], size[i], (alpha << 24) | 0xDCE8FF);
		}

		private void ember(DrawContext c, int i, float dt, int w, int h) {
			y[i] += vy[i] * dt;
			x[i] += (vx[i] + (float) Math.sin(time * 2f + phase[i]) * 8f) * dt;
			if (y[i] < -3f) {
				spawn(i, w, h, false);
			}
			float life = Math.max(0f, Math.min(1f, y[i] / h));
			int alpha = (int) (0xC0 * life);
			int color = (i % 3 == 0) ? 0xFFD25A : 0xFF8A2E;
			Ui.rect(c, (int) x[i], (int) y[i], size[i], size[i], (alpha << 24) | color);
		}

		private void bubble(DrawContext c, int i, float dt, int w, int h) {
			y[i] += vy[i] * dt;
			x[i] += (float) Math.sin(time * 1.2f + phase[i]) * 8f * dt;
			if (y[i] < -8f) {
				spawn(i, w, h, false);
			}
			int s = size[i];
			int bx = (int) x[i];
			int by = (int) y[i];
			int color = 0x70A8E6FF;
			Ui.rect(c, bx + 1, by, s - 2, 1, color);
			Ui.rect(c, bx + 1, by + s - 1, s - 2, 1, color);
			Ui.rect(c, bx, by + 1, 1, s - 2, color);
			Ui.rect(c, bx + s - 1, by + 1, 1, s - 2, color);
			Ui.rect(c, bx + 1, by + 1, 1, 1, 0xB0FFFFFF);
		}

		private void matrix(DrawContext c, int i, float dt, int w, int h) {
			y[i] += vy[i] * dt;
			if (y[i] - size[i] * 3 > h) {
				spawn(i, w, h, false);
			}
			int bx = (int) x[i];
			for (int k = 0; k < size[i]; k++) {
				int ky = (int) y[i] - k * 3;
				if (ky < -2 || ky > h) {
					continue;
				}
				int alpha = (int) (0xB0 * (1f - (float) k / size[i]));
				int color = k == 0 ? 0xD8FFD8 : 0x2EE66B;
				Ui.rect(c, bx, ky, 2, 2, (alpha << 24) | color);
			}
		}
	}
}
