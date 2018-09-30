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
 * Not safe for players below level 30
 */
public class FrogmanSheepmanRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("蛙人", 7);
		attackArmy.put("蛙人精英", 4);
		attackArmy.put("蛙人巫师", 2);
		attackArmy.put("羊人", 6);
		attackArmy.put("武装羊人", 3);
		attackArmy.put("老年羊人", 2);
		attackArmy.put("羊人精英", 1);
		return attackArmy;
	}

	@Override
	protected String getInfo() {
		return "30级以下不安全";
	}
}
