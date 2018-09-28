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
package games.stendhal.server.maps.ados.barracks;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * Builds an NPC to buy previously unbought armor.
 *
 * @author kymara
 */
public class BuyerNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Mrotho") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(45, 49));
				nodes.add(new Node(29, 49));
				nodes.add(new Node(29, 57));
				nodes.add(new Node(45, 57));
				nodes.add(new Node(19, 57));
				nodes.add(new Node(19, 49));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("我在这里负责看管武器装. 除了盔甲不太够外, 其它装备都很充足. 如果你有盔甲可以 #销售 给我.");
				addHelp("如果你 #提供 盔甲, 我可以收购些. 如果你能低调点, 我还可以卖些军火给你.");
				addOffer("请自行看看那边架子上的黑板上写的字, 上面有我们急需的采购东西. 另外我也销售几种箭支.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyrare3")), false);
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellarrows")), false);
				addGoodbye("再见, 伙计.");
			}
		};

		npc.setDescription("你遇见了 Mrotho, 他在阿多斯兵营管理武器库.");
		npc.setEntityClass("barracksbuyernpc");
		npc.setPosition(45, 49);
		npc.initHP(500);
		zone.add(npc);
	}
}
