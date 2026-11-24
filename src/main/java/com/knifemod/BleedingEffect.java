package com.knifemod;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/** Kanama: seviye arttıkça daha sık hasar verir. Zırhı deler ("X kan kaybından öldü"). */
public class BleedingEffect extends StatusEffect {
	public static final RegistryKey<net.minecraft.entity.damage.DamageType> DAMAGE_TYPE =
			RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(KnifeMod.MOD_ID, "bleeding"));
	public static final int MAX_LEVEL = 3; // 0..3 -> Kanama I..IV

	public BleedingEffect() {
		super(StatusEffectCategory.HARMFUL, 0x8A0000);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		int interval = switch (amplifier) {
			case 0 -> 30;
			case 1 -> 22;
			case 2 -> 16;
			default -> 11;
		};
		return duration % interval == 0;
	}

	@Override
	public void applyUpdateEffect(LivingEntity entity, int amplifier) {
		if (!(entity.getWorld() instanceof ServerWorld sw)) return;
		DamageSource src = new DamageSource(
				sw.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(DAMAGE_TYPE));
		entity.damage(src, 1.0f);
		Blood.drip(sw, entity, 6 + amplifier * 4);
	}

	/** Kanama uygula; zaten kanıyorsa bir seviye ağırlaştır. */
	public static void apply(LivingEntity target, int seconds) {
		if (!Blood.bleeds(target) || !target.isAlive()) return;
		StatusEffectInstance cur = target.getStatusEffect(ModEffects.BLEEDING);
		int amp = cur == null ? 0 : Math.min(cur.getAmplifier() + 1, MAX_LEVEL);
		int dur = Math.max(seconds * 20, cur == null ? 0 : cur.getDuration());
		target.addStatusEffect(new StatusEffectInstance(ModEffects.BLEEDING, dur, amp, false, true, true));
	}
}
