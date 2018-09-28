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
 * Less safe for players below level 40
 */
public class OrcRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("兽族战士", 7);
		attackArmy.put("兽族猎手", 5);
		attackArmy.put("兽族首领", 3);
		attackArmy.put("兽人", 6);
		attackArmy.put("山岭兽人", 3);
		attackArmy.put("巨魔", 4);
		attackArmy.put("红巨魔", 7);
		attackArmy.put("穴居巨魔", 2);
   		attackArmy.put("绿龙", 3);

		return attackArmy;
	}
}
