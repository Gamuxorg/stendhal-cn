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
package games.stendhal.server.maps.semos.blacksmith;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * The blacksmith (original name: 艾克德罗斯). Brother of the goldsmith in Ados.
 * He refuses to sell weapons, but he casts iron for the player, and he sells
 * tools.
 *
 * @author daniel
 *
 * @see games.stendhal.server.maps.quests.HungryJoshua
 */
public class BlacksmithNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("艾克德罗斯") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
                nodes.add(new Node(23,12));
                nodes.add(new Node(29,12));
                nodes.add(new Node(29,9));
                nodes.add(new Node(17,9));
                nodes.add(new Node(17,5));
                nodes.add(new Node(16,5));
                nodes.add(new Node(16,3));
                nodes.add(new Node(28,3));
                nodes.add(new Node(28,5));
                nodes.add(new Node(23,5));
                nodes.add(new Node(23,9));
                nodes.add(new Node(28,9));
                nodes.add(new Node(28,13));
                nodes.add(new Node(21,12));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addReply("木头",
						"我需要一些木头保持炉子的火力. 你可以去森林里找到一些.");

				addReply(Arrays.asList("铁矿", "铁矿石"),
				"你能到 Or'ril 西面的山上, 矮人矿附近的地方找到铁矿石, 但去那要小心!");

				addReply("淘金盘",
				"带着这个工具, 你才能够淘金. 顺着 Or'ril 河, 城堡的南边, 瀑布的旁边是一个湖. 我以前在那里发现过 #'金砂' . 也许你也可以.");

				addReply("金砂",
				"我兄弟住在阿多斯. 他能把金块重铸成纯金条.");

				addReply("线轴", "虽然我做工具 #生意 ,但我不做 ＃线轴 , 抱歉. 造出这东西对我来说太麻烦了. 你可以找一个 矮人试试.");
				addReply(Arrays.asList("oil", "can of oil"), "Oh, 这些是由渔夫 fishermen 给我们供应.");

				addHelp("如果你给我带来 #木头 和 #'铁矿石', 我能为你 #鋳铁 . 然后把它卖给矮人, 就可以为自已赚些钱.");
				addJob("我是一个铁匠, 工作是 #鋳铁 , 也 #销售 点工具.");
				addGoodbye();
				new SellerAdder().addSeller(this, new SellerBehaviour(SingletonRepository.getShopList().get("selltools")));

				// 艾克德罗斯 casts iron if you bring him wood and 铁矿石.
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("木头", 1);
				requiredResources.put("铁矿石", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour("xoderos_cast_iron",
						"鋳铁", "铁锭", requiredResources, 5 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				"欢迎, 很抱歉, 由于战争不允许我卖给你任何武器, 不过我可以给你 #铸铁 , 也 #销售 一些工具.");
			}};
			npc.setPosition(23, 12);
			npc.setEntityClass("blacksmithnpc");
			npc.setDescription("你见到了 艾克德罗斯, 是 塞门镇的强壮的铁匠.");
			zone.add(npc);
	}
}
