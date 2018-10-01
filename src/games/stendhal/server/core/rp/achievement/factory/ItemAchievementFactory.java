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

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.PlayerLootedNumberOfItemsCondition;

/**
 * Factory for item related achievements.
 *
 * @author madmetzger
 */
public class ItemAchievementFactory extends AbstractAchievementFactory {

	@Override
	protected Category getCategory() {
		return Category.ITEM;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> itemAchievements = new LinkedList<Achievement>();

		itemAchievements.add(createAchievement("item.money.100", "First pocket money", "Loot 100 money from creatures",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(100, "money")));

		itemAchievements.add(createAchievement("item.money.1000000", "You don't need it anymore", "Loot 1000000 money from creatures",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1000000, "money")));

		itemAchievements.add(createAchievement("item.set.red", "亚马逊之威", "查看全部红色套装",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "红铠甲", "红头盔", "红斗篷", "红裤子", "红靴子",
						"红盾")));

		itemAchievements.add(createAchievement("item.set.blue", "深蓝境界", "查看全部蓝色套装",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "蓝色盔甲", "蓝头盔", "蓝色条纹斗篷", "蓝裤子",
						"蓝靴子", "蓝盾")));

		itemAchievements.add(createAchievement("item.set.elvish", "纳尔沃之毒", "查看全部精灵套装",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "精灵护甲", "精灵帽", "精灵披风", "精灵护腿",
						"精灵靴", "精灵之盾")));

		itemAchievements.add(createAchievement("item.set.shadow", "阴影居民", "查看全部阴影套装",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "影子铠甲", "影子头盔", "影子斗篷", "影子护腿",
						"影子靴子", "影盾")));

		itemAchievements.add(createAchievement("item.set.chaos", "混沌掠夺者", "Loot a complete chaos equipment set",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "混沌护甲", "混沌头盔", "混沌斗篷", "混沌护腿",
						"混沌靴", "混乱之盾")));

		itemAchievements.add(createAchievement("item.set.golden", "黄金男孩", "全部黄金装备",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "黄金铠甲", "黄金头盔", "金斗篷", "黄金护腿",
						"金靴子", "金盾")));

		itemAchievements.add(createAchievement("item.set.black", "黑暗界", "查看全部黑暗套装",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "黑色盔甲", "黑头盔", "黑斗篷", "黑护腿",
						"黑靴子", "黑盾")));

		itemAchievements.add(createAchievement("item.set.mainio", "奇妙套装", "查看全套奇妙装备",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "奇妙甲", "奇妙头盔", "奇妙斗篷", "奇妙裤子",
						"奇妙靴子", "奇妙之盾")));

		itemAchievements.add(createAchievement("item.set.xeno", "异界套装", "查看异界类全套装备",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "异界甲", "异界头盔", "异界斗篷", "异界护腿",
						"异界靴子", "异界盾")));

		itemAchievements.add(createAchievement("item.cloak.dragon", "屠龙者", "查看全部龙类斗篷",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "黑龙斗篷", "蓝龙斗篷", "骨龙斗篷",
						"绿龙斗篷", "红龙斗篷")));

		return itemAchievements;
	}

}
