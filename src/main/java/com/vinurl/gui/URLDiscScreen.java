package com.vinurl.gui;

import com.vinurl.client.SoundManager;
import com.vinurl.client.VinURLClient;
import com.vinurl.exe.Executable;
import com.vinurl.net.Payloads;
import com.vinurl.net.ServerEvent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import static com.vinurl.client.VinURLClient.CLIENT;
import static com.vinurl.util.Constants.*;

/**
 * Screen to enter the URL of a disc, with automatic duration, lock and a duration slider.
 * Enter or Escape saves and closes. Built with vanilla widgets instead of owo-lib.
 */
public class URLDiscScreen extends Screen {
	private static final int PADDING = 10;
	private static final int GAP = 4;
	private static final int BUTTON_SIZE = 20;
	private static final int TEXT_WIDTH = 120;
	private static final int PANEL_WIDTH = TEXT_WIDTH + 2 * (GAP + BUTTON_SIZE);
	private static final int PANEL_HEIGHT = 20 + GAP + 20;

	private String url;
	private boolean lock;
	private boolean simulate;
	private int duration;

	private EditBox urlTextbox;
	private DurationSlider durationSlider;
	private IconButton simulateButton;

	public URLDiscScreen(String defaultURL, int defaultDuration) {
		super(Component.translatable("item.vinurl.custom_record"));
		this.url = defaultURL;
		this.duration = Math.clamp(defaultDuration, ServerEvent.MIN_DURATION, ServerEvent.MAX_DURATION);
	}

	@Override
	protected void init() {
		int left = (this.width - PANEL_WIDTH) / 2;
		int top = (this.height - PANEL_HEIGHT) / 2;

		urlTextbox = new EditBox(this.font, left, top, TEXT_WIDTH, 20, Component.translatable("gui.vinurl.textfield.placeholder"));
		urlTextbox.setMaxLength(ServerEvent.MAX_URL_LENGTH);
		urlTextbox.setHint(Component.translatable("gui.vinurl.textfield.placeholder"));
		urlTextbox.setValue(url);
		urlTextbox.setResponder(text -> url = text);
		this.addRenderableWidget(urlTextbox);

		simulateButton = new IconButton(
			left + TEXT_WIDTH + GAP, top, BUTTON_SIZE,
			Component.translatable("gui.vinurl.button.duration.tooltip"),
			button -> simulate ? SIMULATE_BUTTON_DISABLED_ID : (button.isHovered() ? SIMULATE_BUTTON_HOVER_ID : SIMULATE_BUTTON_ID),
			button -> calculateDuration()
		);
		simulateButton.setTooltip(Tooltip.create(Component.translatable("gui.vinurl.button.duration.tooltip")));
		this.addRenderableWidget(simulateButton);

		IconButton lockButton = new IconButton(
			left + TEXT_WIDTH + GAP + BUTTON_SIZE + GAP, top, BUTTON_SIZE,
			Component.translatable("gui.vinurl.button.lock"),
			button -> lock ? LOCK_BUTTON_ID : LOCK_BUTTON_DISABLED_ID,
			button -> lock = !lock
		);
		lockButton.setTooltip(Tooltip.create(Component.translatable("gui.vinurl.button.lock")));
		this.addRenderableWidget(lockButton);

		durationSlider = new DurationSlider(left, top + 20 + GAP, PANEL_WIDTH, 20);
		this.addRenderableWidget(durationSlider);

		this.setInitialFocus(urlTextbox);
	}

	private void calculateDuration() {
		if (simulate || url.isBlank()) {return;}
		simulate = true;
		simulateButton.setTooltip(Tooltip.create(Component.translatable("gui.vinurl.button.duration.tooltip.calculating")));

		Executable.executeCommand(
			SoundManager.getFileName(url) + "/duration", Executable.YT_DLP.getCommandLine().addArguments(new String[] {
				url, "--print", "DURATION: %(duration)d", "--no-playlist",
				"--js-runtimes", "deno:" + Executable.DENO.FILE_PATH
			}, false).addArguments(VinURLClient.CONFIG.parameters())
		).subscribe("duration")
			.onOutput((output) -> {
				String type = output.substring(0, output.indexOf(':') + 1);
				String message = output.substring(type.length()).trim();

				switch (type) {
					case "DURATION:" -> CLIENT.execute(() -> durationSlider.setDuration(Integer.parseInt(message)));
					case "WARNING:" -> LOGGER.warn(message);
					case "ERROR:" -> LOGGER.error(message);
					default -> LOGGER.info(output);
				}
			})
			.onError((error) -> CLIENT.execute(this::finishCalculating))
			.onComplete(() -> CLIENT.execute(this::finishCalculating))
		.start();
	}

	private void finishCalculating() {
		simulate = false;
		simulateButton.setTooltip(Tooltip.create(Component.translatable("gui.vinurl.button.duration.tooltip")));
	}

	@Override
	public boolean keyPressed(KeyEvent input) {
		if (input.isEscape() || input.isConfirmation()) {
			ClientPlayNetworking.send(new Payloads.SetUrl(url, duration, lock));
			this.onClose();
			return true;
		}
		return super.keyPressed(input);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int left = (this.width - PANEL_WIDTH) / 2;
		int top = (this.height - PANEL_HEIGHT) / 2;
		// simple dark panel behind the widgets
		graphics.fill(left - PADDING, top - PADDING, left + PANEL_WIDTH + PADDING, top + PANEL_HEIGHT + PADDING, 0xC0101010);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static String formatDuration(int seconds) {
		return "%02d:%02d".formatted(seconds / 60, seconds % 60);
	}

	/** Slider from 0 to 3600 seconds that shows the duration as mm:ss. */
	private class DurationSlider extends AbstractSliderButton {
		private static final int MAX = ServerEvent.MAX_DURATION;

		DurationSlider(int x, int y, int width, int height) {
			super(x, y, width, height, Component.empty(), (double) duration / MAX);
			updateMessage();
		}

		void setDuration(int seconds) {
			duration = Math.clamp(seconds, 0, MAX);
			this.value = (double) duration / MAX;
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.literal(formatDuration(duration)));
		}

		@Override
		protected void applyValue() {
			duration = (int) Math.round(this.value * MAX);
		}
	}
}
