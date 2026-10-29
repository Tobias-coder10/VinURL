package com.vinurl.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static com.vinurl.util.Constants.MOD_ID;

/**
 * All messages sent between server and client.
 * Replaces the owo-lib network channel with plain Fabric networking.
 */
public final class Payloads {
	private Payloads() {}

	public static void register() {
		PayloadTypeRegistry.clientboundPlay().register(PlaySound.TYPE, PlaySound.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(StopSound.TYPE, StopSound.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(OpenScreen.TYPE, OpenScreen.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SetUrl.TYPE, SetUrl.CODEC);
	}

	private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> typeOf(String path) {
		return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MOD_ID, path));
	}

	private static void writeNullablePos(FriendlyByteBuf buf, BlockPos pos) {
		buf.writeBoolean(pos != null);
		if (pos != null) {buf.writeBlockPos(pos);}
	}

	private static BlockPos readNullablePos(FriendlyByteBuf buf) {
		return buf.readBoolean() ? buf.readBlockPos() : null;
	}

	/** Server -> client: start playing a disc at a jukebox (pos) or on an entity (entityID). */
	public record PlaySound(BlockPos pos, int entityID, String url) implements CustomPacketPayload {
		public static final Type<PlaySound> TYPE = typeOf("play_sound");
		public static final StreamCodec<FriendlyByteBuf, PlaySound> CODEC = StreamCodec.of(
			(buf, msg) -> {
				writeNullablePos(buf, msg.pos());
				buf.writeVarInt(msg.entityID());
				buf.writeUtf(msg.url());
			},
			(buf) -> new PlaySound(readNullablePos(buf), buf.readVarInt(), buf.readUtf())
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** Server -> client: stop a disc, optionally cancelling its download. */
	public record StopSound(BlockPos pos, int entityID, String url, boolean cancel) implements CustomPacketPayload {
		public static final Type<StopSound> TYPE = typeOf("stop_sound");
		public static final StreamCodec<FriendlyByteBuf, StopSound> CODEC = StreamCodec.of(
			(buf, msg) -> {
				writeNullablePos(buf, msg.pos());
				buf.writeVarInt(msg.entityID());
				buf.writeUtf(msg.url());
				buf.writeBoolean(msg.cancel());
			},
			(buf) -> new StopSound(readNullablePos(buf), buf.readVarInt(), buf.readUtf(), buf.readBoolean())
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** Server -> client: open the screen to enter a URL. */
	public record OpenScreen(String url, int duration) implements CustomPacketPayload {
		public static final Type<OpenScreen> TYPE = typeOf("open_screen");
		public static final StreamCodec<FriendlyByteBuf, OpenScreen> CODEC = StreamCodec.of(
			(buf, msg) -> {
				buf.writeUtf(msg.url());
				buf.writeVarInt(msg.duration());
			},
			(buf) -> new OpenScreen(buf.readUtf(), buf.readVarInt())
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** Client -> server: save the URL, duration and lock state on the disc in hand. */
	public record SetUrl(String url, int duration, boolean lock) implements CustomPacketPayload {
		public static final Type<SetUrl> TYPE = typeOf("set_url");
		public static final StreamCodec<FriendlyByteBuf, SetUrl> CODEC = StreamCodec.of(
			(buf, msg) -> {
				buf.writeUtf(msg.url());
				buf.writeVarInt(msg.duration());
				buf.writeBoolean(msg.lock());
			},
			(buf) -> new SetUrl(buf.readUtf(), buf.readVarInt(), buf.readBoolean())
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}
