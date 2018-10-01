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
package games.stendhal.server.maps.wofol.bakery;

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
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

/**
 * Builds the wofol baker NPC.
 *
 * @author kymara
 */
public class BakerNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

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

	//
	// BakerNPC
	//

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC baker = new SpeakerNPC("可洛普") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(15, 3));
				nodes.add(new Node(15, 8));
				nodes.add(new Node(13, 8));
				nodes.add(new Node(13, 10));
				nodes.add(new Node(10, 10));
				nodes.add(new Node(10, 12));
				nodes.add(new Node(7, 12));
				nodes.add(new Node(7, 6));
				nodes.add(new Node(2, 6));
				nodes.add(new Node(2, 3));
				nodes.add(new Node(9, 3));
				nodes.add(new Node(2, 3));
				nodes.add(new Node(2, 6));
				nodes.add(new Node(15, 6));
				nodes.add(new Node(15, 5));
				nodes.add(new Node(27, 5));
				nodes.add(new Node(27, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				// This isn't bad grammar. It's his tyle of speech! Do't correct pls.
				addJob("我 #制作 #比萨. 我曾经在烘焙大师 #蓝德 处学习.");
				addReply(Arrays.asList("小圆菇", "大脚菇"),
				        "#蓝德 教会我在森林中采蘑菇. 人们喜欢吃蘑菇, 我就多加一点.");
				addReply("面粉", "塞门镇附近的磨坊可以用小麦生产.");
				addReply("干酪", "干酪? 我不知道.");
				addReply("西红柿", "生长在玻璃温室中.");
				addReply("火腿", "大型动物身上才有火腿.");
				addHelp("我和 #蓝德 一起工作过, 我会 #制作 #比萨.");
				addReply("蓝德", "我向塞门镇的人类, 蓝德大师学习 #制作 #比萨.");
				addQuest("#蓝德 需要 比萨外卖员. 我 #制作 #比萨, 你得有材料.");
				addGoodbye("你没有带着蜡烛!");

				// makes a 比萨 if you bring 面粉 cheese mushroom 大脚菇 and ham
				// (uses sorted TreeMap instead of HashMap)
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("面粉", 2);
				requiredResources.put("干酪", 1);
				requiredResources.put("西红柿", 1);
				requiredResources.put("小圆菇", 2);
				requiredResources.put("大脚菇", 1);
				requiredResources.put("火腿", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour("kroip_make_pizza", "制作", "比萨",
				        requiredResources, 5 * 60, true);

				new ProducerAdder().addProducer(this, behaviour,
				        "欢迎!");
			}
		};

		baker.setEntityClass("koboldchefnpc");
		baker.setPosition(15, 3);
		baker.initHP(1000);
		baker.setDescription("你遇见了可洛普. 他以前是蓝德的实习生, 现在是Wofol有名的比萨师.");
		zone.add(baker);
	}
}
