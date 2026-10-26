package pl.penguin.visuals.ui;

import net.minecraft.client.gui.DrawContext;

import java.util.Random;

/** Efekty tła menu. */
public final class Fx {
	private Fx() {
	}

	/** Padający śnieg za oknem menu. */
	public static final class Snow {
		private final int count;
		private final float[] x;
		private final float[] y;
		private final float[] speed;
		private final float[] drift;
		private final int[] size;
		private final Random random = new Random();
		private boolean ready = false;
		private long last = System.nanoTime();
		private float time = 0f;

		public Snow(int count) {
			this.count = count;
			x = new float[count];
			y = new float[count];
			speed = new float[count];
			drift = new float[count];
			size = new int[count];
		}

		public void render(DrawContext c, int w, int h) {
			long now = System.nanoTime();
			float dt = Math.min(0.05f, (now - last) / 1_000_000_000f);
			last = now;
			time += dt;

			if (!ready) {
				for (int i = 0; i < count; i++) {
					x[i] = random.nextFloat() * w;
					y[i] = random.nextFloat() * h;
					speed[i] = 12f + random.nextFloat() * 26f;
					drift[i] = random.nextFloat() * 6.28f;
					size[i] = random.nextInt(8) == 0 ? 2 : 1;
				}
				ready = true;
			}

			for (int i = 0; i < count; i++) {
				y[i] += speed[i] * dt;
				x[i] += (float) Math.sin(time * 0.8f + drift[i]) * 10f * dt;
				if (y[i] > h) {
					y[i] = -3f;
					x[i] = random.nextFloat() * w;
				}
				if (x[i] < 0f) {
					x[i] += w;
				} else if (x[i] > w) {
					x[i] -= w;
				}
				int alpha = size[i] == 2 ? 0x8C : 0x4E;
				Ui.rect(c, (int) x[i], (int) y[i], size[i], size[i], (alpha << 24) | 0xFFFFFF);
			}
		}
	}
}
