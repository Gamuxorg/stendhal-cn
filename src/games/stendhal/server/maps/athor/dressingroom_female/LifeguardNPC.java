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
package games.stendhal.server.maps.athor.dressingroom_female;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.OutfitChangerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.OutfitChangerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

/**
 * Dressing rooms at the 阿托尔岛 beach (Inside / Level 0).
 *
 * @author daniel
 */
public class LifeguardNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildFemaleDressingRoom(zone);
	}

	private void buildFemaleDressingRoom(final StendhalRPZone zone) {
		final SpeakerNPC pam = new SpeakerNPC("Pam") {

			@Override
			protected void createPath() {
				// doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addJob("I'm one of the lifeguards at this beach. And as you can see, I also take care of the women's dressing room.");
				addHelp("Just tell me if you want to #'borrow a swimsuit'!");
				addGoodbye("Have fun!");

				final Map<String, Integer> priceList = new HashMap<String, Integer>();
				priceList.put("swimsuit", 5);
				final OutfitChangerBehaviour behaviour = new OutfitChangerBehaviour(priceList);
				new OutfitChangerAdder().addOutfitChanger(this, behaviour, "borrow");

				// stuff needed for the SuntanCreamForZara quest
				// (uses sorted TreeMap instead of HashMap)
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
<<<<<<< HEAD
				requiredResources.put("arandula", 1);
				requiredResources.put("kokuda", 1);
				requiredResources.put("小治疗济", 1);
=======
				requiredResources.put("海芋", 1);
				requiredResources.put("科科达", 1);
				requiredResources.put("小治疗剂", 1);
>>>>>>> f76672e17df092a61ddb88a57859203a0a9ef0ae

				final ProducerBehaviour mixerBehaviour = new ProducerBehaviour("pamela_mix_cream",
						"mix", "suntan cream", requiredResources, 10 * 60);

				new ProducerAdder().addProducer(this, mixerBehaviour, "Hallo!");

				addReply(
				        Arrays.asList("suntan", "cream", "suntan cream"),
				        "David's and mine suntan cream is famous all over the island. But the way to the labyrinth entrance is blocked, so we can't get all the ingredients we need. If you bring me the things we need, I can #mix our special suntan cream for you.");

				addReply("海芋", "海芋 is a herb which is growing around 塞门镇.");

				addReply(
				        "科科达",
				        "We can't find the 科科达 herb which is growing on this island, because the entrance of the labyrinth, where you can find this herb, is blocked.");

<<<<<<< HEAD
				addReply("小治疗济", "It's a small bottle full of potion. You can buy it at several places.");
=======
				addReply("小治疗剂", "It's a small bottle full of 治疗济. You can buy it at several places.");
>>>>>>> f76672e17df092a61ddb88a57859203a0a9ef0ae
			}
		};

		pam.setEntityClass("lifeguardfemalenpc");
		pam.setDirection(Direction.LEFT);
		pam.setPosition(12, 11);
		pam.initHP(100);
		pam.setDescription("You see Pam. She waits for models who can wear her newest swimsuit collection.");
		zone.add(pam);
	}
}
