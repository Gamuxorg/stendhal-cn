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
package games.stendhal.server.maps.semos.tavern;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;

/*
 * Food and drink seller,  Inside Semos Tavern - Level 0 (ground floor)
 * Sells the flask required for 泰德's quest IntroducePlayers
 */
public class BarMaidNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildMargaret(zone);
	}

	private void buildMargaret(final StendhalRPZone zone) {
		final SpeakerNPC margaret = new SpeakerNPC("玛格丽特") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(11, 4));
				nodes.add(new Node(18, 4));
				nodes.add(new Node(18, 3));
				nodes.add(new Node(11, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addReply("瓶子", "如果你想买一个烧瓶, 请对我说: #buy #瓶子. 或者你还可以问我买其它的东西 #offer.");
				addQuest("Oh 很高兴为你服务, 但很不幸, 我帮不了你.");
				addJob("这是这个正规洒吧的坐台小姐姐, 你想要一些 #buy 国产或进口的啤酒, 还有好吃的小吃.");
				addHelp("本酒吧为客人提供住宿和餐饮服务！需要点什么酒水饮料请对我说 #offer .");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("food&drinks")));

				addGoodbye();
			}
		};

		//赠券 for free 啤酒

        margaret.add(ConversationStates.ATTENDING,
                (Arrays.asList("赠券", "赠券s", "啤酒券", "free 啤酒")),
                new PlayerHasItemWithHimCondition("赠券"),
                ConversationStates.ATTENDING,
                "Oh 你发现一个赠券, 这是我以前发放的, 喝得开心!",
                new MultipleActions(new DropItemAction("赠券"),
                					new EquipItemAction("啤酒"))
                );

        margaret.add(ConversationStates.ATTENDING,
        		(Arrays.asList("赠券", "赠券s", "啤酒券", "free 啤酒")),
                new NotCondition(new PlayerHasItemWithHimCondition("赠券")),
                ConversationStates.ATTENDING,
                "别撒谎！你并没有任何赠券. 时下酒吧生意不好, 请别骗我！!",
                null
                );

		margaret.setEntityClass("tavernbarmaidnpc");
		margaret.setDescription("玛格丽特 看着很火辣, 你虽不能帮她, 但总可以在那买点东西.");
		margaret.setPosition(11, 4);
		margaret.initHP(100);
		margaret.setSounds(Arrays.asList("hiccup-1", "hiccup-2", "hiccup-3"));
		zone.add(margaret);
	}
}
