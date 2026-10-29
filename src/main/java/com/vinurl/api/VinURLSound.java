package com.vinurl.api;

import com.vinurl.net.Payloads;
import com.vinurl.util.DiscData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.vinurl.VinURL.CUSTOM_RECORD;

@SuppressWarnings("unused")
public class VinURLSound {
	private static final double JUKEBOX_RANGE = 64;
	private static final double INFINITE_RANGE = Double.POSITIVE_INFINITY;

	public static void playAt(ServerLevel level, ItemStack stack, BlockPos pos) {
		send(stack, () -> playersInRange(level, pos, JUKEBOX_RANGE), (tag) ->
			new Payloads.PlaySound(pos, -1, DiscData.url(tag))
		);
	}

	public static void playFor(ServerLevel level, ItemStack stack, int entityID) {
		send(stack, () -> playersInRange(level, entityID, JUKEBOX_RANGE), (tag) ->
			new Payloads.PlaySound(null, entityID, DiscData.url(tag))
		);
	}

	public static void stopAt(ServerLevel level, ItemStack stack, BlockPos pos, boolean cancelable) {
		send(stack, () -> playersInRange(level, pos, INFINITE_RANGE), (tag) ->
			new Payloads.StopSound(pos, -1, DiscData.url(tag), cancelable)
		);
	}

	public static void stopFor(ServerLevel level, ItemStack stack, int entityID, boolean cancelable) {
		send(stack, () -> playersInRange(level, entityID, INFINITE_RANGE), (tag) ->
			new Payloads.StopSound(null, entityID, DiscData.url(tag), cancelable)
		);
	}

	private static void send(ItemStack stack, Supplier<List<ServerPlayer>> players, Function<CompoundTag, CustomPacketPayload> factory) {
		if (!stack.is(CUSTOM_RECORD)) {return;}

		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		CustomPacketPayload payload = factory.apply(tag);
		for (ServerPlayer player : players.get()) {
			// Only send to players who have VinURL installed
			if (ServerPlayNetworking.canSend(player, payload.type())) {
				ServerPlayNetworking.send(player, payload);
			}
		}
	}

	private static List<ServerPlayer> playersInRange(ServerLevel level, BlockPos pos, double range) {
		return level.getPlayers((player) -> player.position().distanceTo(Vec3.atCenterOf(pos)) <= range);
	}

	private static List<ServerPlayer> playersInRange(ServerLevel level, int entityID, double range) {
		return Optional.ofNullable(level.getEntity(entityID))
			.map(entity -> playersInRange(level, entity.blockPosition(), range))
			.orElse(List.of());
	}
}