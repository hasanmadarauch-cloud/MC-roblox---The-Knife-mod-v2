package com.knifemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class KnifeMod implements ModInitializer {
	public static final String MOD_ID = "knifemod";

	/** /gamerule knifeBlood false  -> kan efektlerini kapatır */
	public static GameRules.Key<GameRules.BooleanRule> BLOOD;
	/** /gamerule knifeGrabPlayers false -> sadece moblar yakalanabilir */
	public static GameRules.Key<GameRules.BooleanRule> GRAB_PLAYERS;

	@Override
	public void onInitialize() {
		BLOOD = GameRuleRegistry.register("knifeBlood", GameRules.Category.MISC,
				GameRuleFactory.createBooleanRule(true));
		GRAB_PLAYERS = GameRuleRegistry.register("knifeGrabPlayers", GameRules.Category.PLAYER,
				GameRuleFactory.createBooleanRule(true));

		ModItems.init();
		ModEntities.init();
		ModEffects.init();

		ServerTickEvents.END_SERVER_TICK.register(GrabManager::tick);

		// Bıçakla (kesme, fırlatma, kanama) ölen her şey kan/kemik patlamasıyla ölür
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (!(entity.getWorld() instanceof ServerWorld sw)) return;
			boolean byKnife = source.getSource() instanceof ThrownKnifeEntity
					|| source.isOf(BleedingEffect.DAMAGE_TYPE)
					|| (source.getAttacker() instanceof LivingEntity attacker
						&& source.getSource() == attacker
						&& attacker.getMainHandStack().isOf(ModItems.KNIFE));
			if (byKnife) Blood.gib(sw, entity);
		});
	}
}
