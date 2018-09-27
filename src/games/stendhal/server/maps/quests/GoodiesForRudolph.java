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
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Christmas Goodies For Rudolph
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Rudolph (the Red-Nosed Reindeer) - walking around 塞门镇 during Christmas season.</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Rudolph asks you for some 石蕊, carrots and 苹果s.</li>
 * <li>You get his goodies by collecting them from around 塞门镇..</li>
 * <li>Rudolph sees you have collected goodies and asks for them and then thanks you.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>100 XP</li>
 * <li>50 gold</li>
 * <li>Karma: 60</li>
 * <li>雪珠</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Once every 11 months.</li>
 * </ul>
 */
public class GoodiesForRudolph extends AbstractQuest {

	private static final String QUEST_SLOT = "goodies_rudolph";

	private static final int REQUIRED_MONTHS = 11;
	private static final int REQUIRED_MINUTES = 60 * 24 * 30 * REQUIRED_MONTHS;

	private static final String RUDOLPH_TALK_QUEST_ACCEPT = "I heard about the wonderful #goodies you have here in 塞门镇. If you get 5 石蕊, 10 苹果s and 10 胡萝卜s, I'll give you a reward.";
	private static final String RUDOLPH_TALK_QUEST_OFFER = "I want some delicious goodies only you can help me get. Do you think you can help me?";

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("I have met Rudolph. He is the Red-Nosed Reindeer running around in 塞门镇.");
		final String questStateFull = player.getQuest(QUEST_SLOT);
		final String[] parts = questStateFull.split(";");
		final String questState = parts[0];

		if ("rejected".equals(questState)) {
			res.add("He asked me to find goodies for him but I rejected his request.");
		}
		if (player.isQuestInState(QUEST_SLOT, 0, "start", "done")) {
			res.add("I promised to find goodies for him because he is a nice reindeer.");
		}
		if ("start".equals(questState) && player.isEquipped("石蕊", 5)  && player.isEquipped("胡萝卜", 10) && player.isEquipped("苹果", 10) || "done".equals(questState)) {
			res.add("I got all the goodies and will take them to Rudolph.");
		}
		if (isCompleted(player)) {
			if (isRepeatable(player)) {
				res.add("It's been a year since I helped Rudolph. I should ask him if he needs help again.");
			} else {
				res.add("I took the goodies to Rudolph. As a little thank you, he gave ME some goodies. :)");
			}
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Rudolph");

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			RUDOLPH_TALK_QUEST_OFFER,
			null);

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
			ConversationStates.ATTENDING,
			"Thank you very much for the goodies, but I don't have any other task for you this year. Have a wonderful holiday season.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"), new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
			ConversationStates.QUEST_OFFERED,
			RUDOLPH_TALK_QUEST_OFFER,
			null);

		// player is willing to help
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			RUDOLPH_TALK_QUEST_ACCEPT,
			new SetQuestAction(QUEST_SLOT, "start"));

		// player is not willing to help
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Well, then I guess I'll just ask someone else for them. Woe is me.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// player wants to know what goodies he is referring to
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("goodies"),
			null,
			ConversationStates.ATTENDING,
			"石蕊 is a pale green patch of wonderfulness which grows all around this city. 苹果s are found at the farm to the east of the city, and 胡萝卜s are to the 东北： of the city.",
			null);
	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Rudolph");

		// player returns while quest is still active
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				new AndCondition(
					new PlayerHasItemWithHimCondition("石蕊", 5),
					new PlayerHasItemWithHimCondition("苹果", 10),
					new PlayerHasItemWithHimCondition("胡萝卜", 10))),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Excuse me, please! I see you have delicious goodies. Are they for me?",
			null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				new NotCondition(new AndCondition(
					new PlayerHasItemWithHimCondition("石蕊", 5),
					new PlayerHasItemWithHimCondition("苹果", 10),
					new PlayerHasItemWithHimCondition("胡萝卜", 10)))),
			ConversationStates.ATTENDING,
			"Oh my. I am so in anticipation of those goodies which I have asked you for. Hopefully it will not be much longer before you can bring them to me.",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
        reward.add(new DropItemAction("石蕊", 5));
        reward.add(new DropItemAction("胡萝卜", 10));
        reward.add(new DropItemAction("苹果", 10));
		reward.add(new EquipItemAction("money", 50));
		reward.add(new EquipItemAction("雪珠"));
		reward.add(new IncreaseXPAction(100));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(60));
		reward.add(new InflictStatusOnNPCAction("苹果"));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		reward.add(new SetQuestAction(QUEST_SLOT, 0, "done"));

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			// make sure the player isn't cheating by putting the goodies
			// away and then saying "yes"

			new AndCondition(
					new PlayerHasItemWithHimCondition("石蕊", 5),
					new PlayerHasItemWithHimCondition("苹果", 10),
					new PlayerHasItemWithHimCondition("胡萝卜", 10)),

			ConversationStates.ATTENDING, "Oh, I am so excited! I have wanted to eat these things for so long. Thanks so much. And to borrow a phrase, Ho Ho Ho, Merry Christmas.",
			new MultipleActions(reward));


		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Well then, I certainly hope you find those goodies before I pass out from hunger.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Goodies for Rudolph",
				"Rudolph, Santa's favorite reindeer, desperately wants some goodies.",
				false);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "GoodiesForRudolph";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"),
				 new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player, null, null);
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Rudolph";
	}
}
