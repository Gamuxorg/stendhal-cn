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
 * Less safe for players below level 30
 */
public class DwarfRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("矮人", 7);
		attackArmy.put("矮人守卫", 6);
		attackArmy.put("矮人壮士", 6);
		attackArmy.put("矮人统领", 4);
		attackArmy.put("矮人英雄", 5);
		attackArmy.put("杜加矮人", 3);
		attackArmy.put("杜加壮士", 3);
		attackArmy.put("杜加斧手", 3);

		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "30级以下不安全.";
	}
}
