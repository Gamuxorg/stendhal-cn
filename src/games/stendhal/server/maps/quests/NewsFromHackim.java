/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: News from 黑姆伊索
 *
 * PARTICIPANTS:
 * <ul>
 * <li> 黑姆伊索 </li>
 * <li> 辛布兰卡 </li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> 黑姆伊索 asks you to give a message to 辛布兰卡. </li>
 * <li> 辛布兰卡 thanks you with a pair of 皮裤. </li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 10 XP </li>
 * <li> some karma (2) </li>
 * <li> a pair of 皮裤 </li>
 * </ul>
 *
 * REPETITIONS: - None
 */
public class NewsFromHackim extends AbstractQuest {
	private static final String QUEST_SLOT = "news_hackim";



	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("黑姆伊索 the blacksmith assistant wants me to bring a secret message to 辛布兰卡 in 塞门镇 tavern.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("That job was just too hot for me and I don't want to do anything illegal.");
			return res;
		}
		res.add("It can't be that bad to just collect a message to 辛布兰卡. What will happen? I'll do it.");
		if (isCompleted(player)) {
			res.add("I brought 辛布兰卡 the message by 黑姆伊索. That brought me some nice 皮裤.");
		}
		return res;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("黑姆伊索");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Pssst! C'mere... do me a favour and tell #Xin #Blanca that the new supply of weapons is ready, will you?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Thanks, but I don't have any messages to pass on to #Xin. I can't smuggle so often now... I think 艾克德罗斯 is beginning to suspect something. Anyway, let me know if there's anything else I can do.",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Thanks! I'm sure that #Xin will reward you generously. Let me know if you need anything else.",
			new SetQuestAction(QUEST_SLOT, "start"));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Yes, now that I think about it, it probably isn't wise to involve too many people in this... Just forget we spoke, okay? You never heard anything, if you know what I mean.",
			new SetQuestAction(QUEST_SLOT, "rejected"));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("Xin", "辛布兰卡", "Blanca"),
			null,
			ConversationStates.QUEST_OFFERED,
			"You don't know who Xin is? Everybody at the tavern knows Xin. He's the guy who owes 啤酒 money to most of the people in 塞门镇! So, will you do it?",
			null);

		npc.addReply(Arrays.asList("Xin", "辛布兰卡", "Blanca"), "Xin's so cool. I want to work in the tavern too like him but my dad says I have to learn a trade.");
	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("辛布兰卡");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					String answer;
					if (player.isEquipped("皮裤")) {
						answer = "Take this set of brand new... oh, you already have leather leg armor. Well, maybe you can sell them off or something.";
					} else {
						answer = "Take this set of brand new leather leg armor! Let me know if you want anything else.";
					}
					// player.say("Well, to make a long story short; I know
					// your business with 黑姆伊索 and I'm here to tell you
					// that the next shipment is ready.");
					raiser.say("Ah, it's ready at last! That is very good news indeed! Here, let me give you a little something for your help... "
									+ answer);
					player.setQuest(QUEST_SLOT, "done");

					final Item item = SingletonRepository.getEntityManager().getItem("皮裤");
					player.equipOrPutOnGround(item);
					player.addXP(10);
					player.addKarma(2);

					player.notifyWorldAboutChanges();
				}
			});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"News from 黑姆伊索",
				"黑姆伊索, the 塞门镇 blacksmith assistant, needs help to send a message to someone.",
				false);
		step_1();
		step_2();
	}

	@Override
	public String getName() {
		return "NewsFromHackim";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "黑姆伊索";
	}
}
