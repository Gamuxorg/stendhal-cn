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
 * @author kymara
 *
 * Not safe for players below level 150
 */
public class ChaosRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("混沌勇士", 4);
		attackArmy.put("混沌士兵", 3);
		attackArmy.put("混沌司令", 4);
		attackArmy.put("混沌领主", 3);
		attackArmy.put("混沌巫师", 3);
		attackArmy.put("混沌君主", 3);
		attackArmy.put("混沌龙骑士", 3);
		attackArmy.put("混沌红龙骑士", 2);
		attackArmy.put("混沌绿龙骑士", 2);
		attackArmy.put("黑巨人", 1);
		attackArmy.put("黑龙", 1);
		return attackArmy;
	}

	@Override
	protected String getInfo() {
		return "不适合150级以下玩家";
	}
}
