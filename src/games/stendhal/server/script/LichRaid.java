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
 * Not safe for players below level 80
 */
public class LichRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("小骷髅", 5);
		attackArmy.put("骷髅战士", 3);
		attackArmy.put("资深骷髅", 4);
		attackArmy.put("骷髅精", 3);
		attackArmy.put("骨龙", 3);
		attackArmy.put("堕落武士", 5);
		attackArmy.put("堕落僧侣", 3);
		attackArmy.put("堕落高僧", 2);
		attackArmy.put("巫妖", 8);
		attackArmy.put("巫妖王", 3);
		attackArmy.put("大巫妖", 3);

		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Not safe for players below level 80.";
	}
}
