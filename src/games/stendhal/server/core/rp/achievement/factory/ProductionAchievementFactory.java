/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.behaviour.journal.ProducerRegister;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.PlayerProducedNumberOfItemsCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
/**
 * Factory for production achievements
 *
 * @author kymara
 */
public class ProductionAchievementFactory extends AbstractAchievementFactory {

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> achievements = new LinkedList<Achievement>();

		final ProducerRegister producerRegister = SingletonRepository.getProducerRegister();

	    final List<String> foodlist = producerRegister.getProducedItemNames("food");
		final String[] foods = foodlist.toArray(new String[foodlist.size()]);
		// may wish to remove 强治疗剂 by hand?

		// this includes a lot of foods! at time of writing, this is at least:
		// 馅饼, 起司香肠, 香肠, 鱼肉派, 苹果派, 樱桃派, 可丽饼, 三明治, 面包, 比萨
		// 烤排 is made using quest code and not production code so we add an extra condition, and it doesn't adhere to standard completion guidelines
		achievements.add(createAchievement("production.class.food", "Gourmet", "Order all food types available from Faiumoni's cooks",
				Achievement.MEDIUM_BASE_SCORE, true,
				new AndCondition(
						new PlayerProducedNumberOfItemsCondition(1, foods),
						new QuestStateStartsWithCondition("coal_for_haunchy","waiting;"))));


	    final List<String> drinklist = producerRegister.getProducedItemNames("drink");
		final String[] drinks = drinklist.toArray(new String[drinklist.size()]);

		// soups and koboldish torcibud are made using quest code so we add extra conditions for those
		// at time of writing, the other drinks are 沸水, 茶, 椰林飘香, and 强治疗剂 (which we may remove)
		achievements.add(createAchievement("production.class.drink", "Thirsty Worker", "Order all drink types available from Faiumoni's cooks",
				Achievement.MEDIUM_BASE_SCORE, true,
				new AndCondition(
						new PlayerProducedNumberOfItemsCondition(1, drinks),
						new QuestCompletedCondition("soup_maker"),
						new QuestCompletedCondition("fishsoup_maker"),
						new QuestCompletedCondition("koboldish_torcibud"))));


	    final List<String> resourcelist = producerRegister.getProducedItemNames("resource");
		final String[] resources = resourcelist.toArray(new String[resourcelist.size()]);
		// at time of writing: 金条, 密银锭, 面粉, 铁锭
		achievements.add(createAchievement("production.class.resource", "Alchemist", "Produce 5 of each kind of precious metal and resource",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerProducedNumberOfItemsCondition(5, resources)));

		achievements.add(createAchievement("production.flour.1000", "詹妮's Assistant", "Produce 1000 面粉",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerProducedNumberOfItemsCondition(1000, "面粉")));

		return achievements;
	}

	@Override
	protected Category getCategory() {
		return Category.PRODUCTION;
	}

}
