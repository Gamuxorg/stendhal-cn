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
 * Less safe for players below level 50
 */
public class ZombieRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("僵尸鼠", 4);
		attackArmy.put("血尸", 5);
		attackArmy.put("僵尸", 10);
		attackArmy.put("无头怪", 5);
		attackArmy.put("腐尸", 5);
		attackArmy.put("吸血女郎", 5);
		attackArmy.put("狼人", 3);
		attackArmy.put("死亡骑士", 3);
		attackArmy.put("吸血新娘", 1);
		attackArmy.put("吸血鬼王", 1);

		return attackArmy;
	}
}
