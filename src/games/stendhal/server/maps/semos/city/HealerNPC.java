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
package games.stendhal.server.maps.semos.city;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * A young lady (original name: 卡蔓) who heals players without charge.
 */
public class HealerNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("卡蔓") {
			@Override
			public void createDialog() {
				addGreeting("Hi, 如果需要 #帮助 , 只要跟我说一声.");
				addJob("我的特殊能力可以让我给受伤的人们治疗伤口. 我也销售药济和解毒济.");
				addHelp("我可以免费 #治疗 你的伤(#救命 #奶我 #医疗 #医治). 你也可以在外出探险前向我买点药, 当然要 #支付 一点钱.");
				addEmotionReply("hugs", "hugs");
				addGoodbye();
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(5, 46));
				nodes.add(new Node(18, 46));
				setPath(new FixedPath(nodes, true));
			}
		};
		new SellerAdder().addSeller(npc, new SellerBehaviour(SingletonRepository.getShopList().get("healing")));
		new HealerAdder().addHealer(npc, 0);
		npc.setPosition(5, 46);
		npc.setCollisionAction(CollisionAction.STOP);
		npc.setDescription("你看到了好心的卡蔓. 喜欢帮助人. 你有需要时可以找她.");
		npc.setEntityClass("welcomernpc");
		npc.setSounds(Arrays.asList("giggle-1", "giggle-2"));
		zone.add(npc);
	}

}
