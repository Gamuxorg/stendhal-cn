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
package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.common.Rand;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;

/**
 * a basket which can be unwrapped.
 *
 * @author kymara
 */
public class Basket extends Box {


	private static final String[] ITEMS = {"egg", "复活蛋", "小复活蛋", "斑点蛋", "传说之卵"};

	/**
	 * Creates a new Basket.
	 *
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public Basket(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public Basket(final Basket item) {
		super(item);
	}

	@Override
	protected boolean useMe(final Player player) {
		this.removeOne();
		String itemName;
		itemName = ITEMS[Rand.rand(ITEMS.length)];
		final Item item = SingletonRepository.getEntityManager().getItem(
				itemName);
		if ("复活蛋".equals(itemName)) {
			item.setBoundTo(player.getName());
		}
		player.sendPrivateText("祝贺你得到了 "
				+ itemName + "!");
		player.equipOrPutOnGround(item);
		player.notifyWorldAboutChanges();
		return true;
	}

}
