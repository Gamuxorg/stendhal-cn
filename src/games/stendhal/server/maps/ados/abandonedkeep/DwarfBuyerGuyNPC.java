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
package games.stendhal.server.maps.ados.abandonedkeep;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

/**
 * Inside Ados Abandoned Keep - level -3 .
 */
public class DwarfBuyerGuyNPC implements ZoneConfigurator  {

    private final ShopList shops = SingletonRepository.getShopList();

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Ritati Dragontracker") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(25,32));
				nodes.add(new Node(38,32));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {

				addGreeting("你想要什么? #job");
				addJob("我收购零星杂物, 总要有人做这件事. #help ");
				addHelp("看着我! 我收些小饰品! 你有吗? #offer");
				addOffer("如果没有小东西卖给我就别烦我! 你可以看看黑板上的收购价格.");
				addQuest("除非你想拥有 #own 这块地方, 要不然你不能对我怎么样.");
				addGoodbye("离我远点!");
			    addReply("own", "什么？那为什么刚开你不付钱就不能上来!");
			    // see games.stendhal.server.maps.quests.mithrilcloak.GettingTools for further behaviour
			    addReply("buy", "我不卖东西, 但你可以看看黑板上我想收购的东西. 或者问我特殊买卖 #specials.");
			    addReply("YOU", "对, 我在和你说话！我还能对认谁说!");

				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyoddsandends")), false);
			}};

			npc.setPosition(25, 32);
			npc.setCollisionAction(CollisionAction.STOP);
			npc.setEntityClass("olddwarfnpc");
			npc.setDescription("你看见 Ritati Dragontracker ,他收购零星杂物.");
			zone.add(npc);
	}
}
