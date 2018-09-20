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
package games.stendhal.server.maps.ados.bakery;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

/**
 * Ados Bakery (Inside / Level 0).
 *
 * @author hendrik
 */
public class BakerNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildBakery(zone);
	}

	private void buildBakery(final StendhalRPZone zone) {
		final SpeakerNPC baker = new SpeakerNPC("Arlindo") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				// to the well
				nodes.add(new Node(15, 3));
				// to a barrel
				nodes.add(new Node(15, 8));
				// to the baguette on the table
				nodes.add(new Node(13, 9));
				// around the table
				nodes.add(new Node(13, 10));
				nodes.add(new Node(10, 10));
				// to the sink
				nodes.add(new Node(10, 12));
				// to the 比萨/cake/whatever
				nodes.add(new Node(7, 12));
				nodes.add(new Node(7, 10));
				// to the pot
				nodes.add(new Node(3, 10));
				// towards the oven
				nodes.add(new Node(3, 4));
				nodes.add(new Node(5, 4));
				// to the oven
				nodes.add(new Node(5, 3));
				// one step back
				nodes.add(new Node(5, 4));
				// towards the well
				nodes.add(new Node(15, 4));

				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				// addGreeting("Hi, most of the people are out of town at the moment.");
				addJob("我是本地的面包师. 尽管我们得到 塞门镇 的很多支援, 但还是有很多的工作要做. ");
				addReply(Arrays.asList("面粉", "肉", "胡萝卜"),
				        "Ados 供应短缺, 我们要从西边的 塞门镇 取得大多数的食物.");
				addReply(Arrays.asList("mushroom", "小圆菇"),
					    "我们抱怨我们的 pies 不够吃, 所以我们现在只能吃点 mushroom 蘑菇充饥. 你可以在森林中找到一些.");
				addHelp("如果你有足够多的肉或干酪, 可以云 Ados 酒吧找 Siandra 卖掉一些换钱.");
				addGoodbye();

				// Arlindo makes pies if you bring him 面粉, meat, 胡萝卜 and a mushroom
				// (uses sorted TreeMap instead of HashMap)
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("面粉", Integer.valueOf(2));
				requiredResources.put("肉", Integer.valueOf(2));
				requiredResources.put("胡萝卜", Integer.valueOf(1));
				requiredResources.put("小圆菇", Integer.valueOf(2));

				final ProducerBehaviour behaviour = new ProducerBehaviour("arlindo_make_pie", "make", "馅饼",
				        requiredResources, 7 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				        "Hi! 我敢说你一定听过本店有名的馅饼  #馅饼  , 想要我给你做一个吗？ #make ");
			}
		};

		baker.setEntityClass("bakernpc");
		baker.setDirection(Direction.DOWN);
		baker.setPosition(15, 3);
		baker.initHP(100);
		baker.setDescription("Arlindo 是 Ados 官方 面包师, 他制作的馅饼 pies 远近闻名.");
		zone.add(baker);
	}

}
