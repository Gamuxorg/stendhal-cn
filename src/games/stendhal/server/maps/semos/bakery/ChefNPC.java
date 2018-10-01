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
package games.stendhal.server.maps.semos.bakery;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

/**
 * The bakery chef. Father of the camping girl.
 * He makes 三明治s for players.
 * He buys cheese.
 *
 * @author daniel
 * @see games.stendhal.server.maps.orril.river.CampingGirlNPC
 * @see games.stendhal.server.maps.quests.PizzaDelivery
 */
public class ChefNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("蓝德") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				// to the well
				nodes.add(new Node(15,3));
				// to a barrel
				nodes.add(new Node(15,8));
				// to the baguette on the table
				nodes.add(new Node(13,8));
				// around the table
				nodes.add(new Node(13,10));
				nodes.add(new Node(10,10));
				// to the sink
				nodes.add(new Node(10,12));
				// to the 比萨/cake/whatever
				nodes.add(new Node(7,12));
				nodes.add(new Node(7,10));
				// to the pot
				nodes.add(new Node(3,10));
				// towards the oven
				nodes.add(new Node(3,4));
				nodes.add(new Node(5,4));
				// to the oven
				nodes.add(new Node(5,3));
				// one step back
				nodes.add(new Node(5,4));
				// towards the well
				nodes.add(new Node(15,4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("我是这里的面包师, 也做 #比萨 外卖服务. 在战争破坏和路被封锁之前, 有很多来自 阿多斯 城的订单, 但也给了我更多时间给重要的客户 #制作 三明治 .他们都说很好吃!");
				addHelp("如果你想挣钱, 可以帮我送 #比萨 外卖. 本来以前由我女儿 #莎丽 送, 但她现在外出渡假了.");
				addReply("面包", "Oh, Erna 管理对外的商务工作. 你可以到外面房间跟她谈.");
				addReply("干酪",
				"现在干酪相当难找, 我们这最近闹鼠灾. 我很奇怪这些老鼠把干酪藏哪了? 如果你 #'卖 干酪' , 我很乐意买一些!");
				addReply("火腿",
				"很好, 你看起来像是一个职业猎人, 为什么不去森林猎取一些上等肉呢? 不要给我这些小片的肉, 虽然. . . 我只用上等火腿做三明治!");
				addReply("莎丽",
				"我女儿莎丽可能会帮你弄到火腿. 她是一个侦察员. 你明白, 我想她现在可能在 Or'ril 城堡的南面露营.");
				addReply("比萨", "我需要一个能帮我送比萨外卖的人. 你原意接受这个 #任务 吗?");
				addReply(Arrays.asList("三明治"),
				"我的三明治营养且可口, 如果你想要, 就对我说 #'制作 1 三明治'.");
				addOffer("我的 #比萨 需要干酪 ,我们也不供应, 如果你带有干酪, 可以 #卖 给我.");
				final Map<String, Integer> offers = new TreeMap<String, Integer>();
				offers.put("干酪", 5);
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(offers), false);

				addGoodbye();

				// 蓝德 makes 三明治es if you bring him 面包, cheese, and ham.
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("面包", 1);
				requiredResources.put("干酪", 2);
				requiredResources.put("火腿", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour(
						"leander_make_sandwiches", "制作", "三明治",
						requiredResources, 3 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				"你好! 很高兴在我制做 #比萨 和 #三明治 的烹饪间见到你.");


			}};
			npc.setPosition(15, 3);
			npc.setEntityClass("chefnpc");
			npc.setDescription("你见到了蓝德. 他的工作是做一些好吃的东西.");
			zone.add(npc);
	}
}


