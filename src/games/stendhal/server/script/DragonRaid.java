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
public class DragonRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("红龙", 2);
		attackArmy.put("绿龙", 2);
		attackArmy.put("骨龙", 3);
		attackArmy.put("双头飞龙", 2);
		attackArmy.put("blue dragon", 3);
		attackArmy.put("混沌红龙骑士", 2);
		attackArmy.put("混沌绿龙骑士", 2);
		attackArmy.put("金龙", 2);
		attackArmy.put("黑龙", 1);
		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Not safe for players below level 150.";
	}
}
