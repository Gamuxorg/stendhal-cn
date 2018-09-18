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
package games.stendhal.server.maps.semos.bank;

import java.awt.geom.Rectangle2D;
import java.util.Set;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.ItemLogger;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.Spot;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.GuaranteedDelayedPlayerTextSender;
import games.stendhal.server.core.events.MovementListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.ActiveEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.area.WalkBlocker;
import games.stendhal.server.entity.mapstuff.chest.PersonalChest;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.mapstuff.portal.Teleporter;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.player.Player;

public class Vault extends StendhalRPZone {

	private PersonalChest chest;

	public Vault(final String name, final StendhalRPZone zone,
			final Player player) {
		super(name, zone);

		init(player);

	}

	private void init(final Player player) {
		Portal portal = new Teleporter(new Spot(player.getZone(),
				player.getX(), player.getY()));
		portal.setPosition(4, 8);
		add(portal);

		chest = new PersonalChest();
		chest.setPosition(4, 2);
		add(chest);

		WalkBlocker walkblocker = new WalkBlocker();
		walkblocker.setPosition(2, 5);
		walkblocker
				.setDescription("你看到一个垃圾桶. 请把不用的垃圾扔到里面.");
		add(walkblocker);
		// Add a sign explaining about equipped items
		final Sign book = new Sign();
		book.setPosition(2, 2);
		book
				.setText("就像物品被装备错误了一样, 物品扔到地面后会在你离开仓库时返回到你的背包. 另外, 垃圾箱提供了你想扔掉的存放地, 它会在你离开仓库时被自动清空.");
		book.setEntityClass("book_blue");
		book.setResistance(0);
		add(book);
		disallowIn();
		this.addMovementListener(new VaultMovementListener());
	}

	private static final class VaultMovementListener implements MovementListener {
		private static final Rectangle2D area = new Rectangle2D.Double(0, 0, 100, 100);

		@Override
		public Rectangle2D getArea() {
			return area;
		}

		@Override
		public void onEntered(final ActiveEntity entity,
				final StendhalRPZone zone, final int newX, final int newY) {
			// ignore
		}

		@Override
		public void onExited(final ActiveEntity entity,
				final StendhalRPZone zone, final int oldX, final int oldY) {
			if (!(entity instanceof Player)) {
				return;
			}
			if (zone.getPlayers().size() == 1) {
				Set<Item> itemsOnGround = zone.getItemsOnGround();
				for (Item item : itemsOnGround) {
					// ignore items which are in the wastebin
					if (!(item.getX() == 2 && item.getY() == 5)) {
						Player player = (Player) entity;
						String message;
						boolean equippedToBag = player.equip("背包", item);
						if (equippedToBag) {

							message = "你披在地面上的物品 "+item.getName() +  "已自动反回到你的背包.";

							new GameEvent(player.getName(), "equip", item.getName(), "vault", "背包", Integer.toString(item.getQuantity())).raise();
							// Make it look like a normal equip
							new ItemLogger().equipAction(player, item, new String[] {"ground", zone.getName(), item.getX() + " " + item.getY()}, new String[] {"slot", player.getName(), "背包"});
						} else {
							boolean equippedToBank = player.equip("bank", item);
							if (equippedToBank) {
								message = "你在扔在仓库地面上的物品 " + item.getName() + "已自动回到你的银行箱子.";

								new GameEvent(player.getName(), "equip", item.getName(), "vault", "bank", Integer.toString(item.getQuantity())).raise();
								// Make it look like the player put it in the chest
								new ItemLogger().equipAction(player, item, new String[] {"ground", zone.getName(), item.getX() + " " + item.getY()}, new String[] {"slot", "a bank chest", "content"});
							} else {
								// the player lost their items
								message = "你在扔在仓库地面上的物品 " + item.getName() + " 已被清空, 因为你的背包和银行箱子里没有空间装下它们. ";

								// the timeout method enters the zone and coords of item, this is useful we will know it was in vault
								new ItemLogger().timeout(item);
							}
						}

						// tell the player the message
						notifyPlayer(player.getName(), message);
					} else {
						// the timeout method enters the zone and coords of item, this is useful, this is useful we will know it was in wastebin
						new ItemLogger().timeout(item);
					}

				}
				// since we are about to destroy the vault, change the player
				// zoneid to semos bank so that if they are relogging,
				// they can enter back to the bank (not the default zone of
				// PlayerRPClass).
				// If they are scrolling out or walking out the portal it works
				// as before.
				entity.put("zoneid", "int_塞门_银库");
				entity.put("x", "9");
				entity.put("y", "27");

				TurnNotifier.get().notifyInTurns(2, new VaultRemover(zone));
			}
		}

		@Override
		public void onMoved(final ActiveEntity entity,
				final StendhalRPZone zone, final int oldX, final int oldY,
				final int newX, final int newY) {
			// ignore
		}

		@Override
		public void beforeMove(ActiveEntity entity, StendhalRPZone zone,
				int oldX, int oldY, int newX, int newY) {
			// does nothing, but is specified in the implemented interface
		}
	}

	/**
	 * Notifies the user of the vault in the name of Dagobert.
	 *
	 * @param target the player to be notified
	 * @param message the delivered message
	 */
	private static void notifyPlayer(final String target, final String message)  {
		// only uses postman if they logged out. Otherwise, just send the private message.

		final Player player = SingletonRepository.getRuleProcessor().getPlayer(target);

		new GuaranteedDelayedPlayerTextSender("Dagobert", player, message, 2);

	}

	@Override
	public void onFinish() throws Exception {
		this.remove(chest);

	}
}
