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
package games.stendhal.server.maps.ratcity.bakery;

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
 * Provides a Ratman chef running the Rat City bakery.
 *
 * @author omero
 */
public class RatChefNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Gaston") {

			@Override
			protected void createPath() {
                final List<Node> nodes = new LinkedList<Node>();
                nodes.add(new Node(12, 3));
                nodes.add(new Node(3, 3));
                nodes.add(new Node(7, 3));
                nodes.add(new Node(7, 6));
                nodes.add(new Node(9, 6));
                nodes.add(new Node(9, 12));
                nodes.add(new Node(16, 12));
                nodes.add(new Node(16, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("I'm the best #可丽饼 au chocolate chef in town. Ask me to #bake one for you!");
				addReply("crepes",
                    "Ah le dessert for a prince... A taste of which, I really believe, would reform a 食人族 into a civilized gentleman.");
                /**
                 * 巧克力棒 is the item, and the parser knows nothing about bar/bars.
                 * Handle all possibilities explicitly.
                 */
				addReply(Arrays.asList("chocolate", "巧克力棒", "巧克力棒s"),
                    "A rarity. It seems only very nasty and murderous folks carry some in their pockets.");
				addReply("面粉",
                    "I stea.. ahem.. get all my supplies of 面粉 from the nearby 塞门镇.");
				addReply("egg",
                    "I'd look for one where hens scratch about.");
				addReply("牛奶",
                    "A farm would be a good place where one could find that ingredient.");
				addReply("奶油",
                    "Where you find 牛奶 you will most likely find 奶油 as well!");
				addReply(Arrays.asList("沸水"),
                    "It gets distilled from 甘蔗s and you could try asking around in Ados market for that.");
                addReply("糖块",
                    "That comes from grinding 甘蔗s, which you could harvest yourself in #cane #fields."
                        + " You will need some #kitchen #tool for the grinding part.");
                addReply("kitchen tool",
                    "Yes, a 制糖机! Too bad I've lost mine which I borrowed from Erna some time ago... Oh, please avoid mentioning my name to her!");
                addReply("erna",
                    "Oh, she's 蓝德 assistant and you always find her in 塞门镇 bakery!");
                addReply(Arrays.asList("cane", "canes", "cane field", "cane fields", "甘蔗", "甘蔗s"),
                    "I have heard that 甘蔗s need a warm and humid climate to thrive. Maybe you might find some on 阿托尔岛");
				addOffer("I will serve you 可丽饼 au chocolate if you ask me to #bake one!");
				addHelp("Ask me to #bake you my special #chocolate #可丽饼, that's what I offer.");
				addGoodbye("Au revoir voyageur... And come back to visit me anytime you like!");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("面粉", 1);
				requiredResources.put("egg", 1);
				requiredResources.put("奶油", 1);
				requiredResources.put("牛奶", 1);
				requiredResources.put("糖块", 1);
				requiredResources.put("巧克力棒", 1);
				requiredResources.put("沸水", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour("gaston_bake_crepesuzette", "bake", "可丽饼",
				        requiredResources, 5 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				        "Hi there. I bet you've come to taste my #可丽饼 au chocolate! I can #bake some for you if you like.");
			}
		};

		npc.setEntityClass("ratchefnpc");
		npc.setDirection(Direction.DOWN);
		npc.setPosition(16, 3);
		npc.initHP(100);
		npc.setDescription("You see Gaston. He is the best 可丽饼 au chocolate chef in all Rat City.");
		zone.add(npc);
	}
}
