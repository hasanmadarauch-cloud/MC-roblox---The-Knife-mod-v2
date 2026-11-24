package com.knifemod;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.joml.Vector3f;

/** Kan efektleri. Sadece görsel parçacıklar; dünyadaki bloklara dokunmaz. */
public final class Blood {
	private static final DustParticleEffect MIST =
			new DustParticleEffect(new Vector3f(0.55f, 0.0f, 0.0f), 1.3f);
	private static final BlockStateParticleEffect BONES =
			new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.getDefaultState());

	/** Her moba kendi "kanı": iskelet kemik, golem demir, slime/creeper yeşil, kardan adam kar. */
	private static BlockState materialFor(Entity e) {
		if (e instanceof AbstractSkeletonEntity) return Blocks.BONE_BLOCK.getDefaultState();
		if (e instanceof IronGolemEntity) return Blocks.IRON_BLOCK.getDefaultState();
		if (e instanceof SnowGolemEntity) return Blocks.SNOW_BLOCK.getDefaultState();
		if (e instanceof SlimeEntity || e instanceof CreeperEntity) return Blocks.SLIME_BLOCK.getDefaultState();
		return Blocks.REDSTONE_BLOCK.getDefaultState();
	}

	/** Kanamayan moblar (kanama efekti bunlara işlemez). */
	public static boolean bleeds(Entity e) {
		return !(e instanceof AbstractSkeletonEntity || e instanceof IronGolemEntity || e instanceof SnowGolemEntity);
	}

	private static boolean enabled(ServerWorld world) {
		return world.getGameRules().getBoolean(KnifeMod.BLOOD);
	}

	public static void splash(ServerWorld world, Entity target, int amount) {
		if (!enabled(world)) return;
		double x = target.getX(), y = target.getBodyY(0.6), z = target.getZ();
		world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, materialFor(target)),
				x, y, z, amount, 0.25, 0.3, 0.25, 0.15);
		if (bleeds(target)) {
			world.spawnParticles(MIST, x, y, z, Math.max(1, amount / 2), 0.3, 0.3, 0.3, 0.0);
		}
	}

	/** Aşağı süzülen kan damlaları (kanama / yakalanan hedef için). */
	public static void drip(ServerWorld world, Entity target, int amount) {
		if (!enabled(world) || !bleeds(target)) return;
		world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, materialFor(target)),
				target.getX(), target.getBodyY(0.5), target.getZ(), amount, 0.2, 0.25, 0.2, 0.0);
	}

	/** Büyük kan fışkırması (sırttan bıçaklama, infaz). */
	public static void spray(ServerWorld world, Entity target) {
		if (!enabled(world)) return;
		splash(world, target, 45);
		world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, materialFor(target)),
				target.getX(), target.getBodyY(0.8), target.getZ(), 30, 0.1, 0.1, 0.1, 0.45);
	}

	/** Ölüm patlaması: kan + kemik parçaları + ses. */
	public static void gib(ServerWorld world, Entity target) {
		if (!enabled(world)) return;
		double x = target.getX(), y = target.getBodyY(0.5), z = target.getZ();
		world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, materialFor(target)),
				x, y, z, 90, 0.35, 0.45, 0.35, 0.35);
		world.spawnParticles(BONES, x, y, z, 15, 0.3, 0.4, 0.3, 0.25);
		if (bleeds(target)) {
			world.spawnParticles(MIST, x, y, z, 50, 0.5, 0.6, 0.5, 0.0);
			world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, materialFor(target)),
					x, y + 0.5, z, 40, 0.6, 0.4, 0.6, 0.0);
		}
		world.playSound(null, x, y, z, SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.PLAYERS, 1.2f, 0.6f);
		world.playSound(null, x, y, z, SoundEvents.ENTITY_SKELETON_HURT, SoundCategory.PLAYERS, 0.6f, 0.5f);
	}

	private Blood() {}
}
