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
		final SpeakerNPC baker = new SpeakerNPC("Kroip") {

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
				addJob("I #make #比萨. I have learn from the great baker #Leander.");
				addReply(Arrays.asList("小圆菇", "大脚菇"),
				        "#Leander taught me mushroom grow in wood area. People like mushroom, I add more.");
				addReply("面粉", "Mill near 塞门镇 produce from 小麦.");
				addReply("干酪", "Cheese? I know not.");
				addReply("西红柿", "This grow in glass houses.");
				addReply("火腿", "The pig animal have ham.");
				addHelp("I have work with #Leander, I #make #比萨.");
				addReply("Leander", "I was with human, in 塞门镇. The great Leander taught to #make #比萨.");
				addQuest("#Leander need 比萨 send. I #make #比萨, you have ingredients.");
				addGoodbye("You no take 蜡烛!");

				// makes a 比萨 if you bring 面粉 cheese mushroom 大脚菇 and ham
				// (uses sorted TreeMap instead of HashMap)
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("面粉", 2);
				requiredResources.put("干酪", 1);
				requiredResources.put("西红柿", 1);
				requiredResources.put("小圆菇", 2);
				requiredResources.put("大脚菇", 1);
				requiredResources.put("火腿", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour("kroip_make_pizza", "make", "比萨",
				        requiredResources, 5 * 60, true);

				new ProducerAdder().addProducer(this, behaviour,
				        "Welkom!");
			}
		};

		baker.setEntityClass("koboldchefnpc");
		baker.setPosition(15, 3);
		baker.initHP(1000);
		baker.setDescription("You see Kroip. He was a trainee of Leander once and is now a famous pizza baker in Wofol.");
		zone.add(baker);
	}
}
