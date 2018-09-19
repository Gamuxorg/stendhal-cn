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
package games.stendhal.server.script;

import java.util.HashMap;
import java.util.Map;

/**
 * @author miguel
 *
 * Not safe for players below level 150
 */
public class DrowRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("黑暗精灵", 10);
		attackArmy.put("黑暗精灵弓箭手", 2);
		attackArmy.put("黑暗精灵精英弓箭手", 2);
		attackArmy.put("黑暗精灵队长", 5);
		attackArmy.put("黑暗精灵骑士", 3);
		attackArmy.put("黑暗精灵将军", 1);
		attackArmy.put("黑暗精灵巫师", 2);
		attackArmy.put("黑暗精灵总督", 1);
		attackArmy.put("黑暗精灵僧侣", 3);
		attackArmy.put("黑暗精灵妈妈", 1);
		attackArmy.put("黑暗精灵大师", 1);
		attackArmy.put("黑暗精灵游侠", 3);
		attackArmy.put("黑暗精灵海军上将", 3);
		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "不适合150级以下的玩家.";
	}
}
