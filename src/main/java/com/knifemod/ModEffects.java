package com.knifemod;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEffects {
	public static final StatusEffect BLEEDING = Registry.register(Registries.STATUS_EFFECT,
			new Identifier(KnifeMod.MOD_ID, "bleeding"), new BleedingEffect());

	public static void init() {}

	private ModEffects() {}
}
