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
 * A raid safe for lowest level players
 */
public class FarmRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("小猪", 4);
		attackArmy.put("母牛", 3);
		attackArmy.put("老母鸡", 4);
		attackArmy.put("山羊", 3);
		attackArmy.put("马", 2);
		attackArmy.put("小鸡", 5);
		attackArmy.put("公牛", 2);
		attackArmy.put("公羊", 5);
		attackArmy.put("小老鼠", 5);
		attackArmy.put("白马", 2);
		return attackArmy;
	}

	@Override
	protected String getInfo() {
		return "对于新手比较安全.";
	}
}
