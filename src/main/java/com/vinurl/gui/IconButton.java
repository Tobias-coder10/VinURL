package com.vinurl.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A small button that draws one of the mod's own icons.
 * The icon can change with the button's state (for example locked / unlocked).
 */
public class IconButton extends AbstractButton {
	private final Function<IconButton, Identifier> sprite;
	private final Consumer<IconButton> onPress;

	public IconButton(int x, int y, int size, Component message, Function<IconButton, Identifier> sprite, Consumer<IconButton> onPress) {
		super(x, y, size, size, message);
		this.sprite = sprite;
		this.onPress = onPress;
	}

	@Override
	public void onPress(InputWithModifiers input) {
		onPress.accept(this);
	}

	@Override
	public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite.apply(this), getX(), getY(), getWidth(), getHeight());
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput output) {
		this.defaultButtonNarrationText(output);
	}
}
