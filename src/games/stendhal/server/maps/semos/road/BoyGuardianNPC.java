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
package games.stendhal.server.maps.semos.road;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasShieldEquippedCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.player.Player;

public class BoyGuardianNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildMineArea(zone);
	}

	private void buildMineArea(final StendhalRPZone zone) {
			final SpeakerNPC npc = new SpeakerNPC("Will") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {

				String greetingBasis = "嗨, 你站住! 要小心了, 你已到了镇子外面! ";

				// When the players level is below 15 AND (he has a shield equipped OR he completed the "meet_hayunn" quest)
				add(
						ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(
								new LevelLessThanCondition(15),
								new OrCondition(
										new PlayerHasShieldEquippedCondition(),
										new QuestCompletedCondition("meet_hayunn")
								)
						),
						ConversationStates.ATTENDING,
						greetingBasis + "小心那些可能攻击你动物, 还有一些在附近活动的敌军. 路上记得带些吃的! ",
						null);
				// When the players level is below 15 AND he has NO shield AND he has NOT completed the "meet_hayunn" quest
				add(
						ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(
								new LevelLessThanCondition(15),
								new NotCondition(new PlayerHasShieldEquippedCondition()),
								new NotCondition(new QuestCompletedCondition("meet_hayunn"))
						),
						ConversationStates.ATTENDING,
						greetingBasis + "唉呀! 你还没有盾, 在你外出到野外之前, 现在最好回到塞门镇的旧房子里和 海云那冉 谈谈.",
						null);
				// When the player is above level 15
				add(
						ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new LevelGreaterThanCondition(15),
						ConversationStates.ATTENDING,
						greetingBasis + "Oh, 我看你已经够强大和勇敢了, 玩的开心 :)",
						null);

				addJob("我的工作是警戒外面的野兽！我父母交给我这个特殊 #使命 !");
				addReply("使命", "是的, 一个非常特殊和重要的使命!");
				addHelp("我爸爸总是跟我说不要去陌生的森林里 #闲逛 ...他还说我应该记得带着一些 #吃的 ,以防不测!");
				addReply("闲逛", "是的, 如果你想成为一个我所期望的勇士, 你必须跑快点!");
				addReply("吃的", "蓝德 是塞门镇的面包师, 他会制作的三明治非常可口, 妈妈总是在那儿买, 他的面包也很好吃！我不知道饮料在哪里买, 或许你可以问问 #卡蔓 或者 #玛格丽特 ?");
				addReply("卡蔓", "卡蔓 是塞门镇有名的医疗师, 可能你见过她, 她就在村庄到镇子上的路上 :)");
				addReply("玛格丽特", "玛格丽特 在酒馆工作, 但我父母不让我去那里..");
				addQuest("我正在执行任务 :) 我警戒坏坏的家伙或警告并 #帮助 过往的人们! 不过我还不需要你的帮助...");
				addGoodbye("Shhhhh, 不在大声说话！要小心！再见!");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("WillFirstChat")) {
					player.setQuest("WillFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setEntityClass("boyguardnpc");
		npc.setDescription("You 遇到了 Will. 他想长大后成为城镇警戒专家. ");
		npc.setPosition(6, 43);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		npc.setPerceptionRange(4);
		zone.add(npc);
	}
}
