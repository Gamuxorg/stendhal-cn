/* $Id: CaptainNPC.java,v 1.23 2013/06/10 22:13:14 bluelads99 Exp $ */
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
package games.stendhal.server.maps.athor.ship;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.QuestCompletedSellerBehaviour;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.athor.ship.AthorFerry.Status;

/** Factory for the Scuba Diver on Athor Ferry. */

public class ScubaNPC implements ZoneConfigurator  {

	private Status ferrystate;
	private final ShopList shops = SingletonRepository.getShopList();

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Edward") {

			@Override
			public void createDialog() {
				addGoodbye("等太久了...");
				addHelp("Hm, 可能你喜欢冒险?");
				addOffer("#学习 过 #执照 我才卖 #潜水装.");
				new SellerAdder().addSeller(this, new QuestCompletedSellerBehaviour("get_diving_license", "我不能卖 #潜水装 给任何人!", shops.get("sellScubaStuff")), false);
				addJob("我是这艘船上的助理.");

				//scuba gear phrases
				addReply("潜水装","你需要 潜水装 周游美丽的海底世界.");
				//addReply("scuba","你需要 潜水装 周游美丽的海底世界.");
				//addReply("gear","You need 潜水装 to explore the beautiful world below the sea.");
				//clue for the player.
				addReply("学习","去图书馆学习操作员手册.");

				//quest phrases;
				addReply("执照","在我给你潜水装之前潜水会很危险, 你需要通过一场 #考试.");
				addReply("迷之诺","你知道他? 嗯.. 为何是他! 想起来了, 在我们在陆地上休息期间, 时常看到叫这个名字的人在 #沼泽 地区游荡.");
				addReply("沼泽","就是码头北面, 但是自从 #Blordrough 之日后, 那个沼泽开始闹鬼, 要小心.");
				addReply("Blordrough","几年前魔王 Blordrough 在这些岛上发动战争, 直到森林精灵和 Deniran 势力联合抵抗后, 才把他的军队阻挡住. 三方军队拼命撕杀, 但最后魔王被赶到了湖和海的另一侧.");
				add(ConversationStates.ATTENDING,
						"status",
						null,
						ConversationStates.ATTENDING,
						null,
						new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						npc.say(ferrystate.toString());
					}
				});

			}

			@Override
			protected void onGoodbye(final RPEntity player) {
				// Turn back to the sea.
				setDirection(Direction.LEFT);
			}
		};

		new AthorFerry.FerryListener() {

			@Override
			public void onNewFerryState(final Status status) {
				ferrystate = status;
				switch (status) {
				case ANCHORED_AT_MAINLAND:
				case ANCHORED_AT_ISLAND:
					// capital letters symbolize shouting
					npc.say("启航!");
					break;

				default:
					npc.say("起锚! 扬帆!");
					break;
				}
				// Turn back to the wheel
				npc.setDirection(Direction.DOWN);

			}
		};

		npc.setPosition(17, 40);
		npc.setEntityClass("pirate_sailornpc");
		npc.setDescription ("你遇见一个经验丰富的老船员, 但他好像若有所思.");
		npc.setDirection(Direction.LEFT);
		zone.add(npc);
	}
}
