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
package games.stendhal.server.maps.fado.forest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import games.stendhal.server.entity.npc.behaviour.adder.MultiProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.MultiProducerBehaviour;

/**
 * Provides a Meat and Fish professional smoker in Fado forest.
 *
 * @author omero
 */
public class MeatAndFishSmokerNPC implements ZoneConfigurator {
    /**
     * Configure a zone.
     *
     * @param   zone        The zone to be configured.
     * @param   attributes  Configuration attributes.
     */
    @Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
        buildNPC(zone);
    }

    private void buildNPC(final StendhalRPZone zone) {
        final SpeakerNPC npc = new SpeakerNPC("Olmo") {

            @Override
            protected void createPath() {
                final List<Node> nodes = new LinkedList<Node>();
                nodes.add(new Node(26, 53));
                nodes.add(new Node(26, 58));
                nodes.add(new Node(22, 58));
                nodes.add(new Node(29, 58));
                nodes.add(new Node(29, 52));
                nodes.add(new Node(31, 52));
                nodes.add(new Node(27, 52));
                nodes.add(new Node(27, 53));
                nodes.add(new Node(25, 53));
                nodes.add(new Node(25, 52));
                nodes.add(new Node(22, 52));
                nodes.add(new Node(22, 50));
                nodes.add(new Node(22, 53));

                setPath(new FixedPath(nodes, true));
            }

            @Override
            protected void createDialog() {
                addJob("I can #smoke you #smoked #meat, #smoked #ham, #smoked #trout or #smoked #cod. Just ask me to!");
                addOffer("I will #smoke for you #smoked #meat, #smoked #ham, #smoked #trout or #薰鳕鱼, but you'll have to bring me what is needed.");
                addHelp("Ask me to #smoke for you #smoked #meat, #smoked #ham, #smoked #trout or #smoked #cod, that's what I'm good at when you bring me what is needed.");

                addReply(Arrays.asList("smoked", "薰肉", "薰火腿", "薰鲑鱼", "薰鳕鱼"),
                    "The true secret is which herbs and which wood will make the perfect #smoke.");
                addReply(Arrays.asList("鼠尾草", "百里香"),
                    "It grows in many places, at the edges or well in the depths of a forest.");
                addReply(Arrays.asList("trout", "cod"),
                    "I wouldn't reveal you where my favorite fishing spots are but I would suggest you go find some books on the subject in one of those scholarly places.");
                addReply(Arrays.asList("肉","火腿"),
                    "I don't care if it comes from lion or 大象... I can #smoke that for you!");

                addGoodbye("S' veg!");

                final HashSet<String> productsNames = new HashSet<String>();
                productsNames.add("薰肉");
                productsNames.add("薰火腿");
                productsNames.add("薰鲑鱼");
                productsNames.add("薰鳕鱼");

                final Map<String, Integer> reqRes_smokedMeat = new TreeMap<String, Integer>();
                reqRes_smokedMeat.put("木头", 2);
                reqRes_smokedMeat.put("肉", 1);
                reqRes_smokedMeat.put("百里香", 1);

                final Map<String, Integer> reqRes_smokedHam = new TreeMap<String, Integer>();
                reqRes_smokedHam.put("木头", 3);
                reqRes_smokedHam.put("火腿", 1);
                reqRes_smokedHam.put("百里香", 2);

                final Map<String, Integer> reqRes_smokedTrout = new TreeMap<String, Integer>();
                reqRes_smokedTrout.put("木头", 1);
                reqRes_smokedTrout.put("trout", 1);
                reqRes_smokedTrout.put("鼠尾草", 1);

                final Map<String, Integer> reqRes_smokedCod = new TreeMap<String, Integer>();
                reqRes_smokedCod.put("木头", 1);
                reqRes_smokedCod.put("cod", 1);
                reqRes_smokedCod.put("鼠尾草", 2);


                final HashMap<String, Map<String, Integer>> requiredResourcesPerProduct = new HashMap<String, Map<String, Integer>>();
                requiredResourcesPerProduct.put("薰肉", reqRes_smokedMeat);
                requiredResourcesPerProduct.put("薰火腿", reqRes_smokedHam);
                requiredResourcesPerProduct.put("薰鲑鱼", reqRes_smokedTrout);
                requiredResourcesPerProduct.put("薰鳕鱼", reqRes_smokedCod);

                final HashMap<String, Integer> productionTimesPerProduct = new HashMap<String, Integer>();
                productionTimesPerProduct.put("薰肉", 5 * 60);
                productionTimesPerProduct.put("薰火腿", 8 * 60);
                productionTimesPerProduct.put("薰鲑鱼", 4 * 60);
                productionTimesPerProduct.put("薰鳕鱼", 6 * 60);

                final HashMap<String, Boolean> productsBound = new HashMap<String, Boolean>();
                productsBound.put("薰肉", false);
                productsBound.put("薰火腿", true);
                productsBound.put("薰鲑鱼", true);
                productsBound.put("薰鳕鱼", false);

                final MultiProducerBehaviour behaviour = new MultiProducerBehaviour(
                    "olmo_smoke_meatandfish",
                    "smoke",
                    productsNames,
                    requiredResourcesPerProduct,
                    productionTimesPerProduct,
                    productsBound);

                new MultiProducerAdder().addMultiProducer(this, behaviour,
                        "Hi there! Sure you smelled the aroma coming from  my #smoked products!");
            }
        };

        npc.setEntityClass("meatandfishsmokernpc");
        npc.setDirection(Direction.DOWN);
        npc.setPosition(26, 53);
        npc.initHP(100);
        npc.setDescription("You see Olmo. He seems busy smoking meat and fish.");
        zone.add(npc);
    }
}
