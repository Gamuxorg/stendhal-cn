/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
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
import games.stendhal.server.core.rp.achievement.condition.KilledRareCreatureCondition;
import games.stendhal.server.core.rp.achievement.condition.KilledSharedAllCreaturesCondition;
import games.stendhal.server.core.rp.achievement.condition.KilledSoloAllCreaturesCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasKilledNumberOfCreaturesCondition;
/**
 * Factory for fighting achievements
 *
 * @author madmetzger
 */
public class FightingAchievementFactory extends AbstractAchievementFactory {

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> fightingAchievements = new LinkedList<Achievement>();
		fightingAchievements.add(createAchievement("fight.general.rats", "Rat Hunter", "Kill 15 rats", Achievement.EASY_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition("老鼠", 15)));
		fightingAchievements.add(createAchievement("fight.general.exterminator", "Exterminator", "Kill 10 rats of each kind", Achievement.MEDIUM_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition(10, "老鼠", "洞穴老鼠", "利齿鼠", "毒液鼠", "僵尸鼠", "巨型鼠", "鼠人", "女鼠人", "鼠人弓箭手")));
		fightingAchievements.add(createAchievement("fight.general.deer", "Deer Hunter", "Kill 25 鹿", Achievement.EASY_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition("鹿", 25)));
		fightingAchievements.add(createAchievement("fight.general.boars", "Boar Hunter", "Kill 20 boar", Achievement.EASY_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition("野猪", 20)));
		fightingAchievements.add(createAchievement("fight.general.bears", "Bear Hunter", "Kill 10 black bears, 10 bears and 10 幼熊s", Achievement.EASY_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition(10, "熊", "黑熊", "幼熊")));
		fightingAchievements.add(createAchievement("fight.general.狐狸es", "Fox Hunter", "Kill 20 狐狸es", Achievement.EASY_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition("狐狸", 20)));
		fightingAchievements.add(createAchievement("fight.general.safari", "Safari", "Kill 30 tigers, 30 lions and 50 elephants", Achievement.EASY_BASE_SCORE, true,
													new AndCondition(
															new PlayerHasKilledNumberOfCreaturesCondition("老虎", 30),
															new PlayerHasKilledNumberOfCreaturesCondition("狮子", 30),
															new PlayerHasKilledNumberOfCreaturesCondition("大象", 50)
															)));
		fightingAchievements.add(createAchievement("fight.general.ents", "Wood Cutter", "Kill 10 ents, 10 树精妻子s and 10 old ents", Achievement.MEDIUM_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition(10, "ent", "树精妻子", "老树精")));
		fightingAchievements.add(createAchievement("fight.special.rare", "Poacher", "Kill any rare creature", Achievement.HARD_BASE_SCORE, true,
				new KilledRareCreatureCondition()));

		fightingAchievements.add(createAchievement("fight.special.all", "Legend", "Kill all creatures solo", Achievement.HARD_BASE_SCORE, true,
				new KilledSoloAllCreaturesCondition()));
		fightingAchievements.add(createAchievement("fight.special.allshared", "Team Player", "Kill all creatures in a team", Achievement.HARD_BASE_SCORE, true,
				new KilledSharedAllCreaturesCondition()));
		return fightingAchievements;
	}

	@Override
	protected Category getCategory() {
		return Category.FIGHTING;
	}

}
