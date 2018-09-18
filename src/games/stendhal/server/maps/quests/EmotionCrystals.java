/***************************************************************************
 *                   (C) Copyright 2003-2013 - Stendhal                    *
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

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Emotion Crystals
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Julius (the Soldier who guards the entrance to 阿多斯城)</li>
 * <li>Crystal NPCs around Faiumoni</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Julius wants some precious stones for his wife.</li>
 * <li>Find the 5 crystals and solve their riddles.</li>
 * <li>Bring the crystals to Julius.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>2000 XP</li>
 * <li>stone legs</li>
 * <li>Karma: 15</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 *
 * @author AntumDeluge
 */
public class EmotionCrystals extends AbstractQuest {
	private static final String QUEST_SLOT = "emotion_crystals";

	private static final String[] crystalColors = { "red", "purple", "yellow", "pink", "blue" };

	// Amount of time, in minutes, player must wait before retrying the riddle (24 hours)
	private static final int WAIT_TIME_WRONG = 24 * 60;
	private static final int WAIT_TIME_RETRY = 7 * 24 * 60;

	private static final int OFFSET_TIMESTAMPS = 1;
	private static final int OFFSET_SUCCESS_MARKER = 6;

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}

		// Only include Julius in the quest log if player has spoken to him
		if (player.isQuestInState(QUEST_SLOT, 0, "start") || player.isQuestInState(QUEST_SLOT, 0,  "rejected")) {
			res.add("我向 Julius 聊天, 这个士兵守卫着 Ados 的大门.");
			if (player.isQuestInState(QUEST_SLOT, 0, "rejected")) {
				res.add("我心情不好, 也管不了, 因此拒绝了他的请求.");
			}
			else {
				res.add("我答应走遍 Faiumoni 收集5种水晶给他.");
			}
		}

		List<String> gatheredCrystals = new ArrayList<String>();
		boolean hasAllCrystals = true;

		for (String color : crystalColors) {
			if (player.isEquipped(color + " emotion crystal")) {
				gatheredCrystals.add(color + " emotion crystal");
			} else {
				hasAllCrystals = false;
			}
		}
		if (!gatheredCrystals.isEmpty()) {
			String tell = "我已找到的水晶有：";
			tell += gatheredCrystals;
			res.add(tell);
		}

		if (hasAllCrystals) {
			res.add("我已取得全部的情感水晶, 接下来要把它们带给 Ados城的 Julius .");
		}

		if (player.isQuestInState(QUEST_SLOT, 0, "done")) {
			res.add("我把水晶递给 Julius 的妻子. 我得到一些经验值、运气值和有用的石腿 stone legs.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Julius");


		// Player asks for quest
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, 0, "start"),
					new QuestNotCompletedCondition(QUEST_SLOT)),
			ConversationStates.QUEST_OFFERED,
			"我不能常常见到我的妻子, 因为我守卫这个关口很忙. 我想为妻子做一事情, 你能帮我吗？",
			null);

		// Player accepts quest
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"谢谢你, 我想收集5种心情水果 #emotion #crystals ,作为送给妻子的礼物, 请找这5种水晶, 并带回给我.",
			new MultipleActions(
					new SetQuestAction(QUEST_SLOT, 0, "start"),
					new IncreaseKarmaAction(5)));

		// Player rejects quest
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			// Julius walks away
			ConversationStates.IDLE,
			"Hmph! 我应该问问某些人.",
			new MultipleActions(
					new SetQuestAction(QUEST_SLOT, 0, "rejected"),
					new DecreaseKarmaAction(5)));

		// Player tries to leave without accepting/rejecting the quest
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.GOODBYE_MESSAGES,
			null,
			ConversationStates.QUEST_OFFERED,
			"这不是 \"yes\" 或 \"no\" 的问题. 我说, 你能做点我喜欢的事吗？",
			null);

		// Player asks about crystals
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("crystal", "crystals", "emotion", "emotions", "emotion crystal", "emotion crystals", "emotions crystal", "emotions crystals"),
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"你不知道这是什么心情？当然, 你知道快乐和悲伤. 我听说水晶分散在 Faiumoni 的各处, 这种特殊的水果能释放各种心情. 总共有5种, 藏在 地牢、山上和森林中. 我还听说有一些在房子附近可以找到. ",
			null);

		// Player asks for quest after completed
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT, 0, "done"),
			ConversationStates.ATTENDING,
			"我妻子一定很喜欢这些心情水晶.",
			null);

		// Player asks for quest after already started
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"我确信我向你要过水晶 #crystals.",
				null);
	}


	private void prepareRiddlesStep() {
		// List of NPCs
		final List<SpeakerNPC> npcList = new ArrayList<SpeakerNPC>();

		// Add the crystals to the NPC list with their riddle
		for (String color : crystalColors) {
			npcList.add(npcs.get(Character.toUpperCase(color.charAt(0)) + color.substring(1) + " Crystal"));
		}

		// Riddles
		final List<String> riddles = new ArrayList<String>();
		// Answers to riddles
		final List<List<String>> answers = new ArrayList<List<String>>();

		// Red Crystal (crystal of anger)
		riddles.add("我像火一样燃烧, 我的生命不是享受, 那些试探我的人都能感到我的愤怒, 那我是什么？");
		answers.add(Arrays.asList("anger", "angry", "mad", "offended", "hostility", "hostile", "hate", "hatred", "animosity"));
		// Purple Crystal (crystal of fear)
		riddles.add("我不敢露面害怕担责, 他们劝我, 但我还是不去. 发抖才是我的最爱的动作. 那我是什么? ");
		answers.add(Arrays.asList("fear", "fearful", "fearfullness", "fright", "frightened", "afraid", "scared"));
		// Yellow Crystal (crystal of joy)
		riddles.add("我不能停下, 我的心中只有对没有错, 如果你释放我的生命世界将撒满阳光. 那我是什么？");
		answers.add(Arrays.asList("joy", "joyful", "joyfulness", "happy", "happiness", "happyness", "cheer", "cheery",
						"cheerful", "cheerfulness"));
		// Pink Crystal (crystal of love)
		riddles.add("我对所有的事都在意, 我最纯净, 如果你为我分享, 我肯定会报答你, 那我是什么？");
		answers.add(Arrays.asList("love", "amor", "amour", "amity", "compassion"));
		// Blue Crystal (crystal of peace)
		riddles.add("我不让一些事烦扰我. 我从不亢奋. 沉思才是我的优点. 那我是什么?");
		answers.add(Arrays.asList("peace", "peaceful", "peacefullness", "serenity", "serene", "calmness", "calm"));

		// Add conversation states
		for (int n = 0; n < npcList.size(); n++)
		{
			SpeakerNPC crystalNPC = npcList.get(n);
			String rewardItem = crystalColors[n] + " emotion crystal";
			String crystalRiddle = riddles.get(n);
			List<String> crystalAnswers = answers.get(n);

			// In place of QUEST_SLOT
			//String RIDDLER_SLOT = crystalColors.get(n) + "_crystal_riddle";

			final List<ChatAction> rewardAction = new LinkedList<ChatAction>();
			rewardAction.add(new EquipItemAction(rewardItem,1,true));
			rewardAction.add(new IncreaseKarmaAction(5));
			rewardAction.add(new SetQuestToTimeStampAction(QUEST_SLOT, OFFSET_TIMESTAMPS + n));
			rewardAction.add(new SetQuestAction(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle_solved"));

			final List<ChatAction> wrongGuessAction = new LinkedList<ChatAction>();
			wrongGuessAction.add(new SetQuestToTimeStampAction(QUEST_SLOT, OFFSET_TIMESTAMPS + n));
			wrongGuessAction.add(new SetQuestAction(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "wrong"));

			// Player asks about riddle
			crystalNPC.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "start"),
					new OrCondition(
						new QuestNotInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle_solved"),
						new AndCondition(
							new QuestInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle_solved"),
							new NotCondition(new PlayerHasItemWithHimCondition(rewardItem)),
							new TimePassedCondition(QUEST_SLOT, OFFSET_TIMESTAMPS + n, WAIT_TIME_RETRY)
						)
					)
				),
				ConversationStates.ATTENDING,
				"请回答我问你的迷语 #riddle ..",
				null);

			// Player asks about riddle
			crystalNPC.add(ConversationStates.ATTENDING,
					Arrays.asList("riddle", "question", "query", "puzzle"),
					new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new OrCondition(
							new QuestNotInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle_solved"),
							new AndCondition(
								new QuestInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle_solved"),
								new NotCondition(new PlayerHasItemWithHimCondition(rewardItem)),
								new TimePassedCondition(QUEST_SLOT, OFFSET_TIMESTAMPS + n, WAIT_TIME_RETRY)
							)
						)
					),
					ConversationStates.ATTENDING,
					crystalRiddle,
					new SetQuestAction(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle"));

			// Player gets the riddle right
			crystalNPC.add(ConversationStates.ATTENDING,
					crystalAnswers,
					new QuestInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle"),
					ConversationStates.IDLE,
					"回答正确, 这个水晶就是奖励.",
					new MultipleActions(rewardAction));


			// Player gets the riddle wrong
			crystalNPC.add(ConversationStates.ATTENDING,
					"",
					new QuestInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle"),
					ConversationStates.IDLE,
					"对不起, 回答错误.",
					new MultipleActions(wrongGuessAction));

			// Player returns before time is up, to get another chance
			crystalNPC.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "wrong"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, OFFSET_TIMESTAMPS + n, WAIT_TIME_WRONG))
					),
					ConversationStates.IDLE,
					null,
					new SayTimeRemainingAction(QUEST_SLOT, OFFSET_TIMESTAMPS + n, WAIT_TIME_WRONG, "你回答的难了, 重来一次吧"));

			// Player returns before time is up, to get another crystal
			crystalNPC.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle_solved"),
						new NotCondition(new PlayerHasItemWithHimCondition(rewardItem)),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, OFFSET_TIMESTAMPS + n, WAIT_TIME_RETRY))
					),
					ConversationStates.IDLE,
					null,
					new SayTimeRemainingAction(QUEST_SLOT, OFFSET_TIMESTAMPS + n, WAIT_TIME_RETRY, "Oh, 你弄丢了水晶？我再给你一个新的"));

			// Player can't do riddle twice while they still have the reward
			crystalNPC.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new PlayerHasItemWithHimCondition(rewardItem)
					),
					ConversationStates.ATTENDING,
					"我希望你拿着水晶让其他人开心",
					null);


			// Player asks for quest without talking to Julius first
			crystalNPC.add(ConversationStates.ATTENDING,
					ConversationPhrases.QUEST_MESSAGES,
					new QuestNotStartedCondition(QUEST_SLOT),
					ConversationStates.ATTENDING,
					"抱歉, 我不能给你什么. 也许还有人需要这个亮晶晶的水晶 #crystal ...",
					null);

			crystalNPC.add(ConversationStates.ATTENDING,
					Arrays.asList("crystal", "sparkling crystal"),
					new QuestNotStartedCondition(QUEST_SLOT),
					ConversationStates.ATTENDING,
					"Ados 城里有个士兵, 他为妻子做了一个充满漂亮水晶的宝箱...",
					null);

		}
	}


	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Julius");

		// Reward
		final List<ChatAction> rewardAction = new LinkedList<ChatAction>();
		for (String color : crystalColors) {
			rewardAction.add(new DropItemAction(color + " emotion crystal"));
		}
		rewardAction.add(new EquipItemAction("stone legs", 1, true));
		rewardAction.add(new IncreaseXPAction(2000));
		rewardAction.add(new IncreaseKarmaAction(15));
		rewardAction.add(new SetQuestAction(QUEST_SLOT, 0, "done"));

		// Player has all crystals
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestActiveCondition(QUEST_SLOT),
						new PlayerHasItemWithHimCondition("红色心情水晶"),
						new PlayerHasItemWithHimCondition("紫色心情水晶"),
						new PlayerHasItemWithHimCondition("黄色心情水晶"),
						new PlayerHasItemWithHimCondition("粉色心情水晶"),
						new PlayerHasItemWithHimCondition("蓝色心情水晶")),
				ConversationStates.QUEST_ITEM_BROUGHT,
				"Did you bring the crystals?",
				null);

		// Player is not carrying all the crystals
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new OrCondition(
								new NotCondition(new PlayerHasItemWithHimCondition("红色心情水晶")),
								new NotCondition(new PlayerHasItemWithHimCondition("紫色心情水晶")),
								new NotCondition(new PlayerHasItemWithHimCondition("黄色心情水晶")),
								new NotCondition(new PlayerHasItemWithHimCondition("粉色心情水晶")),
								new NotCondition(new PlayerHasItemWithHimCondition("蓝色心情水晶")))),
			ConversationStates.ATTENDING,
			"请带给我所有你找到的心情水晶.",
			null);

		// Player says "yes" (has brought crystals)
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				"非常感谢！我想信这些水晶能让我妻子感到很开心, 请收到这些石腿, 这是你的奖励",
				new MultipleActions(rewardAction));

		// Player says "no" (has not brought crystals)
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"在此期间请保留意寻找, 我能帮你什么忙吗?",
				null);

	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Emotion Crystals",
				"Julius 需要得到一些水晶送给他妻子. 水晶在 Faiumoni 城的各处.",
				false);
		prepareRequestingStep();
		prepareRiddlesStep();
		prepareBringingStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "EmotionCrystals";
	}

	public String getTitle() {

		return "Emotion Crystals";
	}

	@Override
	public int getMinLevel() {
		return 70;
	}

	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Julius";
	}
}
