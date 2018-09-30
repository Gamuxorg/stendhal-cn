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
 * Less safe for players below level 10
 */
public class KoboldRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("狗头人", 7);
		attackArmy.put("狗头人弓箭手", 3);
		attackArmy.put("狗头人队长", 7);
		attackArmy.put("狗头人士兵", 7);
		attackArmy.put("巨型狗头人", 2);
		attackArmy.put("狗头人老兵", 7);
		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "10级以下不安全.";
	}
}