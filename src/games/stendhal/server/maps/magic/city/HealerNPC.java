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
package games.stendhal.server.maps.magic.city;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;

/**
 * Builds a Healer NPC for the magic city.
 *
 * @author kymara
 */
public class HealerNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("莎娃玛特丽") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				// walks along the aqueduct path, roughly
				nodes.add(new Node(5, 25));
				nodes.add(new Node(5, 51));
				nodes.add(new Node(18, 51));
				nodes.add(new Node(18, 78));
				nodes.add(new Node(20, 78));
				nodes.add(new Node(20, 109));
				// and back again
				nodes.add(new Node(20, 78));
				nodes.add(new Node(18, 78));
				nodes.add(new Node(18, 51));
				nodes.add(new Node(5, 51));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
			        //addGreeting("Greetings. Can I #help you?");
				addGreeting("你好， 我可以 #帮助 你吗?");
				//addJob("I practise alchemy and have the ability to #heal others.");
				addJob("我练习炼金术，有能力 #heal others.");
				new HealerAdder().addHealer(this, 500);
				//addReply("magical", "We're all capable of magic here. There are different kinds, of course. My favourite is the Sunlight Spell to keep grass and flowers growing underground.");
				addReply("magical", "我们都拥有魔力。 当然，他们有着不同的种类。 我最爱的是让花草能在地下生长的阳光法术。");
				//addHelp("I have #magical powers to #heal your ailments.");
				addHelp("我有 #magical 力量 来 #heal 你的疾病。");
				//addQuest("I need nothing, thank you.");
				addQuest("我什么都不需要，谢谢你。");
				//addGoodbye("Fare thee well.");
 				addGoodbye("祝你好运，");
			}
		};
		//npc.setDescription("You see a quiet woman with a benign face.");
		npc.setDescription("你看见了一个有着善良面庞的安静的女人");
		npc.setEntityClass("cloakedwomannpc");
		npc.setPosition(5, 25);
		npc.initHP(100);
		zone.add(npc);
	}
}
