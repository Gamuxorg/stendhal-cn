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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import games.stendhal.common.Direction;
import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.PlayerList;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.Task;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.IRPZone;
/**
 * Script to make all players stronger and immune to poison before randomly distributing them
 * over all zones of the running server
 *
 * @author madmetzger
 */
public class MoveAndStrengthenOnlinePlayers extends ScriptImpl {

	private List<StendhalRPZone> zones = new ArrayList<StendhalRPZone>();

	/**
	 * Create the script and initialize the list of zones
	 */
	public MoveAndStrengthenOnlinePlayers() {
		StendhalRPWorld rpWorld = SingletonRepository.getRPWorld();
		for (IRPZone irpZone : rpWorld) {
			StendhalRPZone irpZone2 = (StendhalRPZone) irpZone;
			if (!irpZone2.getName().startsWith("int")) {
				zones.add(irpZone2);
			}
		}
	}

	@Override
	public void execute(final Player admin, List<String> args) {
		Collection<Player> onlinePlayers = SingletonRepository.getRuleProcessor().getOnlinePlayers().getAllPlayers();
		PlayerList pl = new PlayerList();
		int packet = 1;
		for (Player p : onlinePlayers) {
			String zoneName = p.getZone().getName();
			if ((zoneName != null) && (zoneName.equals("int_afterlife") || zoneName.equals("int_塞门_守卫_室"))) {
				pl.add(p);
				if(pl.getAllPlayers().size() == 5) {
					SingletonRepository.getTurnNotifier().notifyInTurns(packet, new MoveAndStrengthenPlayersTurnListener(pl, admin));
					pl = new PlayerList();
					packet += 1;
				}
			}
		}

	}

	private class MoveAndStrengthenPlayersTurnListener implements TurnListener {

		private final PlayerList playersToDealWith;

		private final Player admin;

		MoveAndStrengthenPlayersTurnListener(PlayerList pl, Player executor) {
			playersToDealWith = pl;
			admin = executor;
		}

		@Override
		public void onTurnReached(int currentTurn) {
			playersToDealWith.forAllPlayersExecute(new Task<Player>() {

				@Override
				public void execute(Player player) {
					equipPlayer(player);
					fillBag(player);
					player.setDefXP(999999999);
					player.addXP(999999999);
					StendhalRPZone zone = zones.get(Rand.rand(zones.size()));
					int x = Rand.rand(zone.getWidth() - 4) + 2;
					int y = Rand.rand(zone.getHeight() - 5) + 2;
					player.teleport(zone, x, y, Direction.DOWN, admin);
				}

				private void fillBag(Player player) {
					String[] items = {"韭菜", "大脚菇", "治疗济", "抗毒药济", "啤酒", "小治疗剂", "home scroll", "阿多斯城回城卷", "空白卷轴"};
					for(String item : items) {
						StackableItem stackable = (StackableItem) SingletonRepository.getEntityManager().getItem(item);
						stackable.setQuantity(50);
						player.equipToInventoryOnly(stackable);
					}
				}

				private void equipPlayer(Player player) {
					StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
					money.setQuantity(5000);
					player.equipToInventoryOnly(money);
					StackableItem potions = (StackableItem) SingletonRepository.getEntityManager().getItem("大治疗剂");
					potions.setQuantity(5000);
					player.equipToInventoryOnly(potions);
					if(!player.isEquipped("混沌匕首")) {
						Item first = (Item) player.getSlot("rhand").getFirst();
						player.drop(first);
						Item dagger = SingletonRepository.getEntityManager().getItem("混沌匕首");
						player.equip("rhand", dagger);
					}
					if(!player.isEquipped("混乱之盾")) {
						Item first = (Item) player.getSlot("lhand").getFirst();
						player.drop(first);
						Item shield = SingletonRepository.getEntityManager().getItem("混乱之盾");
						player.equip("lhand", shield);
					}
					if(!player.isEquipped("黑头盔")) {
						Item first = (Item) player.getSlot("head").getFirst();
						player.drop(first);
						Item helmet = SingletonRepository.getEntityManager().getItem("黑头盔");
						player.equip("head", helmet);
					}
					if(!player.isEquipped("elvish legs")) {
						Item first = (Item) player.getSlot("legs").getFirst();
						player.drop(first);
						Item legs = SingletonRepository.getEntityManager().getItem("elvish legs");
						player.equip("legs", legs);
					}
					if(!player.isEquipped("杀手之靴")) {
						Item first = (Item) player.getSlot("feet").getFirst();
						player.drop(first);
						Item boots = SingletonRepository.getEntityManager().getItem("杀手之靴");
						player.equip("feet", boots);
					}
					if(!player.isEquipped("绿龙斗篷")) {
						Item first = (Item) player.getSlot("cloak").getFirst();
						player.drop(first);
						Item cloak = SingletonRepository.getEntityManager().getItem("绿龙斗篷");
						player.equip("cloak", cloak);
					}
				}
			});
		}

	}

}
