package com.vinurl.util;

import net.minecraft.nbt.CompoundTag;

/**
 * Reads and writes the VinURL values stored in a disc's custom data.
 * The keys are the same ones owo-lib used, so existing discs keep working.
 */
public final class DiscData {
	public static final String URL_KEY = "music_url";
	public static final String LOCK_KEY = "lock";
	public static final String DURATION_KEY = "duration";

	private DiscData() {}

	public static String url(CompoundTag tag) {
		return tag.getStringOr(URL_KEY, "");
	}

	public static boolean locked(CompoundTag tag) {
		return tag.getBooleanOr(LOCK_KEY, false);
	}

	public static int duration(CompoundTag tag) {
		return tag.getIntOr(DURATION_KEY, 0);
	}

	public static boolean hasDuration(CompoundTag tag) {
		return tag.contains(DURATION_KEY);
	}

	public static void write(CompoundTag tag, String url, int duration, boolean lock) {
		tag.putString(URL_KEY, url);
		tag.putInt(DURATION_KEY, duration);
		tag.putBoolean(LOCK_KEY, lock);
	}
}
