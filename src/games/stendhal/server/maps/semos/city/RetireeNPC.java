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

import games.stendhal.common.Rand;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.actions.admin.AdministrationAction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.StoreMessageAction;
import games.stendhal.server.entity.player.Player;

/**
 * A crazy old man (original name: 迪金斯) who walks around the city.
 */
public class RetireeNPC implements ZoneConfigurator {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("迪金斯") {

			@Override
			public void createDialog() {
				addGreeting();
				addJob("哈哈! 工作? 十年前就从我的 #信使 工作退休了！哈哈！");
				addHelp("我帮不了你, 但你可以帮助 Stendhal; 告诉你所有的朋友, 访问https://stendhalgame.org and see how you can help!还可以帮助做开发！");
				addGoodbye();
				addReply("信使", "我以前邮递信件. 但现在都是用短信了. 我现在也用短信发消息了. 下面给你发一条.", new StoreMessageAction("迪金斯", "在 塞门镇, 这是很好的聊天方式. 如果你想使用信使给其他不在线的人发消息. 只需用 /msg postman"));
				addOffer("好吧... 虽然我仍然可以为你稍信, 但我已退休, 其他人接替了我的工作. 你可以问问那边的人, 塞门镇北边的新 #信使 .");
				add(ConversationStates.ATTENDING,
						ConversationPhrases.QUEST_MESSAGES,
						null,
				        ConversationStates.ATTENDING,
				        null,
				        new ChatAction() {
					        @Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						        if (Rand.throwCoin() == 1) {
							        npc.say("Ah, 任务... 就像以前我年轻时! 我记得有个请求是关于... 噢看那, 一只鸟! Hmm, 什么? 啊, 任务... 就像以前我年轻的时候!");
						        } else {
						        	npc.say("你知道那边的 赛特 收购羊? 好吧, 传说地牢的深处有一个怪物, 它也买羊...并且收购价要比 赛特 高的多!");
						        }
					        }
				        });

				// A convenience function to make it easier for admins to test quests.
				add(ConversationStates.ATTENDING, "cleanme!", null, ConversationStates.ATTENDING, "What?",
				        new ChatAction() {
					        @Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						        if (AdministrationAction.isPlayerAllowedToExecuteAdminCommand(player, "alter", false)) {
							        for (final String quest : player.getQuests()) {
								        player.removeQuest(quest);
							        }
						        } else {
							        npc.say("什么? 不; 你要给我清洗! 请从我的背开始, 谢谢.");
							        player.damage(5, npc.getEntity());
							        player.notifyWorldAboutChanges();
						        }
					        }
				        });
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(22, 43));
				nodes.add(new Node(25, 43));
				nodes.add(new Node(25, 45));
				nodes.add(new Node(31, 45));
				nodes.add(new Node(31, 43));
				nodes.add(new Node(35, 43));
				nodes.add(new Node(35, 29));
				nodes.add(new Node(22, 29));
				setPath(new FixedPath(nodes, true));
			}

		};
		npc.setPosition(24, 43);
		npc.setCollisionAction(CollisionAction.STOP);
		npc.setEntityClass("beggarnpc");
		npc.setDescription("迪金斯 是一个上了年纪的老人, 但人却很活泼, 待人友善且乐于助人");
		npc.setSounds(Arrays.asList("laugh-1", "laugh-2"));
		zone.add(npc);
	}

}
