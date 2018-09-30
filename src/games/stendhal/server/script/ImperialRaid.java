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
public class ImperialRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("帝国首领", 2);
		attackArmy.put("帝国骑士", 2);
		attackArmy.put("帝国将军", 1);
		attackArmy.put("帝国司令", 2);
		attackArmy.put("帝国科学家", 3);
		attackArmy.put("帝国牧师", 1);
		attackArmy.put("帝国守卫", 5);
		attackArmy.put("帝国试验体", 2);
		attackArmy.put("帝国生化人", 2);
		attackArmy.put("帝国巨人将军", 2);
		return attackArmy;
	}

	@Override
	protected String getInfo() {
		return "150级以下不安全";
	}
}
