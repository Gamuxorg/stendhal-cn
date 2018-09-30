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

import java.util.List;

import games.stendhal.common.MathHelper;
import games.stendhal.server.core.config.annotations.ServerModeUtil;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.RaidCreature;
import games.stendhal.server.entity.player.Player;

/**
 * @author hendrik
 */
public class Plague extends ScriptImpl {

	private static final int MAX_RING_COUNT = 2;

	@Override
	public void execute(final Player admin, final List<String> args) {

		// help
		if (args.size() == 0) {
			admin.sendPrivateText("/script Plague.class [ringcount] <creature>");
			return;
		}

		// extract position of admin

		final int x = admin.getX();
		final int y = admin.getY();
		sandbox.setZone(admin.getZone());

		int ringcount = MathHelper.parseIntDefault(args.get(0), -1);
		int startArgIndex = 1;
		if (ringcount == -1) {
			ringcount = 1;
			startArgIndex = 0;
		}

		// concatenate torn words into one

		StringBuilder sb = new StringBuilder();
		final List <String>  templist = args.subList(startArgIndex, args.size());
		for (final String part : templist) {
			sb.append(part).append(' ');
		}

		String creatureClass  = sb.toString().trim();

		final Creature tempCreature = sandbox.getCreature(creatureClass);

		if (tempCreature == null) {
			admin.sendPrivateText("无此生物");
		} else if (tempCreature.isRare() && !ServerModeUtil.isTestServer()) {
			// Rare creatures should not be summoned even in raids
			// Require parameter -Dstendhal.testserver=junk
			admin.sendPrivateText("带有稀有物品的生物只能在测试服务器上召唤 "
												+ "要使用此功能请在开启服务器使用参数: -Dstendhal.testserver=junk");
		} else {
			final Creature creature = new RaidCreature(tempCreature);

			final int k = MathHelper.parseIntDefault(args.get(0), 1);
			if (k <= MAX_RING_COUNT) {
				for (int dx = -k; dx <= k; dx++) {
					for (int dy = -k; dy <= k; dy++) {
						if ((dx != 0) || (dy != 0)) {
							sandbox.add(creature, x + dx, y + dy + 1);
						}
					}
				}
			} else {
				admin.sendPrivateText("太多了! 请不要大于 <ringcount> "
						+ MAX_RING_COUNT + ".");
			}
		}
	}
}
