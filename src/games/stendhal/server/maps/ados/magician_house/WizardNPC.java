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
package games.stendhal.server.maps.ados.magician_house;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

public class WizardNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildMagicianHouseArea(zone);
	}

	private void buildMagicianHouseArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("海震") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(7, 2));
				nodes.add(new Node(7, 4));
				nodes.add(new Node(13, 4));
				nodes.add(new Node(13, 9));
				nodes.add(new Node(9, 9));
				nodes.add(new Node(9, 8));
				nodes.add(new Node(9, 9));
				nodes.add(new Node(2, 9));
				nodes.add(new Node(2, 3));
				nodes.add(new Node(7, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("我是个巫师, 也卖点 #魔法卷轴 .具体 #商品 可以问我!");
				addHelp("旅行时你最好带着我的 #魔法卷轴 ,可以用来救急!");

				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("scrolls")));

				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("魔法卷轴", "卷轴"),
				        null,
				        ConversationStates.ATTENDING,
				        "我 #提供 帮你快速到达特定地点的: #'回城卷轴' 和针对高级顾客使用的可以标记的 #'空白卷轴s'. 另外我还销售 #'召唤卷轴'!",
				        null);
				add(ConversationStates.ATTENDING, Arrays.asList("回城卷轴", "回城卷"), null,
				        ConversationStates.ATTENDING,
				        "回城卷可以立即回城, 是脱困的好方法!", null);
				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("空卷轴", "空白卷轴"),
				        null,
				        ConversationStates.ATTENDING,
				        "空白卷轴可以自已标记位置. 然后就可以回到标记处. 但就是有点小贵.",
				        null);
				add(
				        ConversationStates.ATTENDING,
				        "召唤卷轴",
				        null,
				        ConversationStates.ATTENDING,
				        "用召唤卷轴可发召唤怪兽, 越高等级的魔法师可以召唤更高等级的怪兽. 当然, 这些滥用卷轴可能会有危险.",
				        null);

				addGoodbye();
			}
		};

		npc.setEntityClass("wisemannpc");
		npc.setPosition(7, 2);
		npc.initHP(100);
		npc.setDescription("你遇见了魔法大师 海震. 用他的卷轴可以让人们任意传送.");
		zone.add(npc);
	}
}
