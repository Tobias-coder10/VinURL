package com.vinurl.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.vinurl.gui.ConfigScreen;

/** Adds a settings button for VinURL in Mod Menu (only used when Mod Menu is installed). */
public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ConfigScreen::create;
	}
}
