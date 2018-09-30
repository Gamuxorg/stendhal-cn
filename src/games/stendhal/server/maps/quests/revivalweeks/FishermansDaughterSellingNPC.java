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
package games.stendhal.server.maps.quests.revivalweeks;

import java.util.Arrays;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.maps.ados.market.FishermansDaughterNPC;

/**
 * 卡若琳 during the 矿镇复兴展会周
 */
public class FishermansDaughterSellingNPC implements LoadableContent {
	private final ShopList shops = SingletonRepository.getShopList();
	private void createFishermansDaughterSellingNPC() {
		final StendhalRPZone zone2 = SingletonRepository.getRPWorld().getZone("0_塞门_山_北2");
		final SpeakerNPC npc2 = new SpeakerNPC("卡若琳") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Hello, nice to meet you! Welcome at my small Mine Town market stand!");
				addJob("I have been asked to work here and sell some tasty snacks and drinks during the 矿镇复兴展会周. An awesome chance to gain some cooking skills for my business which I'm working on at the moment!");
				addHelp("You should go and enjoy the games and party around the Mine Town :) Hope you tested my favourite, the #outfit #colouring already, it's perfect for Halloween days and selfmade costumes :)");
				addReply(Arrays.asList("outfit", "colouring", "outfit colouring"),
		        "It's just pefect for 塞门镇 矿镇复兴展会周 and Halloween! You can recolour your outfit by right clicking on yourself. Choose Set Outfit and then pick your favourite colour for hair and dress! It's awesome!");
				addReply("susi", "Oh she is a lovely girl! I met her around here, she is so happy that she can celebrate with all of us again!");
				addOffer("I sell some snacks and drinks during the Mine Town Weeks. Please take a look at the list close to the stand.");
				addQuest("I heard that #苏茜 would love to make friends, she is inside of the house. Or you can ask 费多拉 next to me about a little run.");
				addGoodbye("Bye, hope you'll enjoy the days here!");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellrevivalweeks")), false);
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc2.setPosition(62, 106);
		npc2.setEntityClass("fishermansdaughternpc");
		npc2.setDirection(Direction.DOWN);
		npc2.initHP(100);
		zone2.add(npc2);
	}


	/**
	 * removes an NPC from the world and NPC list
	 *
	 * @param name name of NPC
	 */
	private void removeNPC(String name) {
		SpeakerNPC npc = NPCList.get().get(name);
		if (npc == null) {
			return;
		}
		npc.getZone().remove(npc);
	}

	@Override
	public void addToWorld() {
		removeNPC("卡若琳");
		createFishermansDaughterSellingNPC();
	}


	/**
	 * removes 卡若琳 from the Mine Town and places her back into her home in Ados.
	 *
	 * @return <code>true</code>, if the content was removed, <code>false</code> otherwise
	 */
	@Override
	public boolean removeFromWorld() {
		removeNPC("卡若琳");

		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_阿多斯_卡若琳_小屋_0");
		new FishermansDaughterNPC().createFishermansDaughterSellingNPC(zone);

		return true;
	}
}
