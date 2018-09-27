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

public class BarbarianRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("蛮人", 30);
		attackArmy.put("蛮族人狼", 15);
		attackArmy.put("蛮族精英", 12);
		attackArmy.put("蛮族牧师", 7);
		attackArmy.put("野蛮夏曼人", 5);
		attackArmy.put("蛮族首领", 3);
		attackArmy.put("蛮人王", 1);

		return attackArmy;
	}

}
