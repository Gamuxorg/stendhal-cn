/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item;

import java.util.Map;

import org.apache.log4j.Logger;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

/**
 * Represents a marked teleport scroll which also poisoned the player.
 *
 * @author kymara
 */
public class TwilightElixir extends Drink {

	private static final Logger logger = Logger.getLogger(TwilightElixir.class);

	/**
	 * Creates a new marked teleport scroll.
	 *
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public TwilightElixir(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public TwilightElixir(final TwilightElixir item) {
		super(item);
	}

	/*
	 * This is very nasty of us. We take away the message that HP = -1000 so player doesn't know.
	 */

	@Override
	public String describe() {
		String text = "你看到 " + getTitle() + ".";
		String stats = "";
		if (hasDescription()) {
			text = getDescription();
		}

		final String boundTo = getBoundTo();

		if (boundTo != null) {
			text = text + " 这个特别奖是因为 " + boundTo
					+ ", 所以不能用在别处.";
		}
		return (text + stats);
	}

	/**
	 * the overridden method verifies item is near to player. if so splits one single item of and
	 * calls consumeItem of the player - so they get poisoned, since that's what 莹光药剂 does
	 * @param user the eating player
	 * @return true if consumption can be started
	 *
	 * this one first teleports the player  (if they are in quest slot twilight zone, to prevent abuse) before it poisons them.
	 */
	@Override
		public boolean onUsed(final RPEntity user) {
		if (user instanceof Player) {
			String extra = " ";
				// then it's safe to cast user to player and use the player-only teleport and quest methods.
			if (((Player) user).isQuestInState("mithril_cloak", "twilight_zone")) {
				StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("hell");
				int x = 5;
				int y = 5;
				if (zone == null) {
					// invalid zone (shouldn't happen)
					user.sendPrivateText("Oh oh. 因为一些奇怪的原因, 卷轴没有把我传送到正确的地点.");
					logger.warn("莹光药剂 to unknown zone hell,"
								+ " teleported " + user.getName()
								+ " to 塞门镇 instead");
					zone = SingletonRepository.getRPWorld().getZone("0_塞门_镇");
				}
				((Player) user).teleport(zone, x, y, null, (Player) user);
				extra = " 现在你应该下地狱, 因为你不想别的, 只考虑你自己";
			}
 			user.sendPrivateText("你不清楚一个人的饮料可能是另一个人的毒药？这个长生药对于 Ida 意味着她被带进了迷魂境地." + extra);
			return super.onUsed(user);
		} else {
			// should never happen.
			logger.warn("some non player RPEntity just used a 莹光药剂, which shouldn't be possible.");
			return false;
		}

	}
}
