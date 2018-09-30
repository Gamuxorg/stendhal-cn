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
 * Less safe for players below level 50
 */
public class ElementalRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("火元素", 7);
		attackArmy.put("水元素", 7);
		attackArmy.put("冰元素", 7);
		attackArmy.put("土元素", 7);
		attackArmy.put("神灯", 5);
		attackArmy.put("气元素", 7);
		return attackArmy;
	}

	@Override
	protected String getInfo() {
		return "50以下不安全";
	}
}
