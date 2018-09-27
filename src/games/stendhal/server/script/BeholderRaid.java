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
 * Not safe for players below level 10
 */
public class BeholderRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("小邪眼怪", 7);
		attackArmy.put("绿色史莱姆", 4);
		attackArmy.put("邪眼怪", 5);
		attackArmy.put("大邪眼怪", 1);
		attackArmy.put("蛇", 3);
		attackArmy.put("草蛇", 4);
		return attackArmy;
	}

	@Override
	protected String getInfo() {
		return "不适合12级以下的玩家";
	}
}
