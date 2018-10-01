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
 * @author gummipferd
 *
 * Less safe for players below level 30
 */
public class ElfRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("精灵", 7);
		attackArmy.put("民兵精灵", 4);
		attackArmy.put("士兵精灵", 3);
		attackArmy.put("精灵指挥官", 4);
		attackArmy.put("精灵大法师", 3);
		attackArmy.put("精灵法师", 6);
		attackArmy.put("精灵弓箭手", 6);
		attackArmy.put("泽地仙女", 5);
		attackArmy.put("树精", 3);

		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "对于30级以下的玩家不安全. ";
	}
}
