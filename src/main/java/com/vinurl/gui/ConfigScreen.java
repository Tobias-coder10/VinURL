package com.vinurl.gui;

import com.vinurl.client.ClientConfig.AudioQuality;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import java.util.Arrays;
import java.util.Locale;

import static com.vinurl.client.VinURLClient.CONFIG;

/**
 * Settings screen, opened with /vinurl config or through Mod Menu.
 * Replaces the screen owo-lib generated automatically.
 */
public class ConfigScreen extends Screen {
	private static final String KEY = "text.config.VinURLConfig.";
	private static final int COLUMN_WIDTH = 220;
	private static final int ROW = 24;
	private static final int LABEL_COLOR = 0xFFA0A0A0;

	private final Screen parent;
	private String whitelistText;
	private int parametersLabelY;
	private int whitelistLabelY;

	public ConfigScreen(Screen parent) {
		super(Component.translatable(KEY + "title"));
		this.parent = parent;
		this.whitelistText = String.join("\n", CONFIG.urlWhitelist());
	}

	@Override
	protected void init() {
		int x = (this.width - COLUMN_WIDTH) / 2;
		int y = 28;

		addToggle(x, y, "downloadEnabled", CONFIG.downloadEnabled, value -> CONFIG.downloadEnabled = value);
		y += ROW;
		addToggle(x, y, "updatesOnStartup", CONFIG.updatesOnStartup, value -> CONFIG.updatesOnStartup = value);
		y += ROW;
		addToggle(x, y, "showDescription", CONFIG.showDescription, value -> CONFIG.showDescription = value);
		y += ROW;

		CycleButton<AudioQuality> quality = CycleButton
			.builder((AudioQuality value) -> Component.translatable(KEY + "option.audioBitrate.value." + value.name().toLowerCase(Locale.ROOT)), CONFIG.audioBitrate)
			.withValues(Arrays.asList(AudioQuality.values()))
			.create(x, y, COLUMN_WIDTH, 20, Component.translatable(KEY + "option.audioBitrate"), (button, value) -> CONFIG.audioBitrate = value);
		quality.setTooltip(Tooltip.create(Component.translatable(KEY + "option.audioBitrate.tooltip")));
		this.addRenderableWidget(quality);
		y += ROW;

		parametersLabelY = y + 2;
		y += 12;
		EditBox parameters = new EditBox(this.font, x, y, COLUMN_WIDTH, 20, Component.translatable(KEY + "option.parameters"));
		parameters.setMaxLength(1000);
		parameters.setValue(CONFIG.parameters);
		parameters.setResponder(value -> CONFIG.parameters = value);
		parameters.setTooltip(Tooltip.create(Component.translatable(KEY + "option.parameters.tooltip")));
		this.addRenderableWidget(parameters);
		y += ROW;

		whitelistLabelY = y + 2;
		y += 12;
		int whitelistHeight = Math.max(36, this.height - y - 34);
		MultiLineEditBox whitelist = MultiLineEditBox.builder()
			.setX(x)
			.setY(y)
			.setPlaceholder(Component.literal("https://www.youtube.com"))
			.build(this.font, COLUMN_WIDTH, whitelistHeight, Component.translatable(KEY + "option.urlWhitelist"));
		whitelist.setCharacterLimit(10000);
		whitelist.setValue(whitelistText);
		whitelist.setValueListener(value -> whitelistText = value);
		this.addRenderableWidget(whitelist);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
			.bounds(x, this.height - 26, COLUMN_WIDTH, 20)
			.build());
	}

	private void addToggle(int x, int y, String option, boolean value, java.util.function.Consumer<Boolean> setter) {
		CycleButton<Boolean> toggle = CycleButton.onOffBuilder(value)
			.create(x, y, COLUMN_WIDTH, 20, Component.translatable(KEY + "option." + option), (button, newValue) -> setter.accept(newValue));
		toggle.setTooltip(Tooltip.create(Component.translatable(KEY + "option." + option + ".tooltip")));
		this.addRenderableWidget(toggle);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		int x = (this.width - COLUMN_WIDTH) / 2;
		graphics.centeredText(this.font, this.title, this.width / 2, 12, CommonColors.WHITE);
		graphics.text(this.font, Component.translatable(KEY + "option.parameters"), x, parametersLabelY, LABEL_COLOR);
		graphics.text(this.font, Component.translatable(KEY + "option.urlWhitelist"), x, whitelistLabelY, LABEL_COLOR);
	}

	@Override
	public void onClose() {
		CONFIG.urlWhitelist = new java.util.ArrayList<>(Arrays.asList(whitelistText.split("\\R")));
		CONFIG.sanitize();
		CONFIG.save();
		this.minecraft.gui.setScreen(parent);
	}

	public static Screen create(Screen parent) {
		return new ConfigScreen(parent);
	}
}
