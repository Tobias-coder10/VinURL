package com.vinurl.net;

import com.vinurl.util.DiscData;
import com.vinurl.util.Url;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.stream.Stream;

import static com.vinurl.VinURL.CUSTOM_RECORD;


public class ServerEvent {
	public static final int MAX_URL_LENGTH = 400;
	public static final int MIN_DURATION = 0;
	public static final int MAX_DURATION = 3600;

	public static void register() {
		// Server event handler for setting the URL on the custom record
		ServerPlayNetworking.registerGlobalReceiver(Payloads.SetUrl.TYPE, (message, context) -> {
			Player player = context.player();
			ItemStack stack = Stream.of(InteractionHand.values())
				.map(player::getItemInHand)
				.filter((stackInHand) -> stackInHand.is(CUSTOM_RECORD))
				.findFirst()
				.orElse(ItemStack.EMPTY);

			if (stack.isEmpty()) {
				player.sendOverlayMessage(Component.translatable("message.vinurl.custom_record.missing"));
				return;
			}

			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (DiscData.locked(tag)) {
				player.sendOverlayMessage(Component.translatable("item.vinurl.custom_record.message.locked"));
				return;
			}

			Url url = Url.parse(message.url());
			if (url == null) {
				player.sendOverlayMessage(Component.translatable("message.vinurl.custom_record.url.invalid"));
				return;
			}

			if (url.length() > MAX_URL_LENGTH) {
				player.sendOverlayMessage(Component.translatable("message.vinurl.custom_record.url.long"));
				return;
			}

			if (message.duration() < MIN_DURATION || message.duration() > MAX_DURATION) {
				player.sendOverlayMessage(Component.translatable("message.vinurl.custom_record.duration.invalid"));
				return;
			}

			DiscData.write(tag, url.toString(), message.duration(), message.lock());
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

			player.level().playSound(null, player, SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.MASTER, 1.0f, 1.0f);
		});
	}
}
