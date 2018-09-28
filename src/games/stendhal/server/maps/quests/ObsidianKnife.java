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
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.Rand;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.KilledCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: The 黑曜石刀.
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Alrak, a dwarf who abandoned the mountain dwarves to live in 狗镇</li>
 * <li>Ceryl, the librarian in 塞门镇</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Alrak is hungry and asks for 100 pieces of ham, cheese or meat</li>
 * <li>Then, Alrak is bored and asks for a book</li>
 * <li>Get the book from Ceryl, and remember the name of who it is for</li>
 * <li>Bring the book to Alrak - he reads it for 3 days</li>
 * <li>After 3 days Alrak has learned how to make a knife from 黑曜石</li>
 * <li>Provided you have high enough level, you can continue</li>
 * <li>Get 黑曜石 for the blade and a fish for the fish bone handle</li>
 * <li>Alrak makes the knife for you</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>黑曜石刀</li>
 * <li>11500 XP</li>
 * <li> lots of karma (total 85 + (5 | -5))
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class ObsidianKnife extends AbstractQuest {

	// Please note, if you want to code a quest where you're asked to collect a number of some randomly picked item, like alrak asks you to initially,
	// please use StartRecordingRandomItemCollectionAction, SayRequiredItemAction, PlayerHasRecordedItemWithHimCondition and DropRecordedItemAction
    // This quest was written before they were available and you should not use it as a template.

	private static final int MINUTES_IN_DAYS = 24 * 60;

	private static final int REQUIRED_FOOD = 100;

	// Required level to move from the finished reading stage to
	// the offering knife stage
	private static final int REQUIRED_LEVEL = 50;

	private static final List<String> FOOD_LIST = Arrays.asList("火腿", "肉", "干酪");

	private static final int REQUIRED_DAYS = 3;

	private static final int REQUIRED_MINUTES = 10;

	private static final String QUEST_SLOT = "obsidian_knife";

	private static final String FISH = "cod";

	private static final String NAME = "Alrak";

	private static Logger logger = Logger.getLogger(ObsidianKnife.class);

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
		res.add("我在 Wofol 的铁匠铺会见了 Alrak .");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("我不想帮助 Alrak.");
			return res;
		}
		res.add("Alrak 让我带给他一些吃的.");
		if (player.isQuestInState(QUEST_SLOT, "火腿", "肉", "干酪")) {
			res.add("我必须拿到 " + REQUIRED_FOOD + ", 并且返回后说 " + questState + " .");
			return res;
		}
		res.add("我带给 Alrak 食物!");
		if (questState.equals("food_brought")) {
			return res;
		}
		res.add("我需要到图书管中帮助 Alrak 问出关于宝石书. ");
		if (questState.equals("seeking_book")) {
			return res;
		}
		res.add("我拿到了Alrak 想要的 宝石书. ");
		if (questState.equals("got_book")) {
			return res;
		}
		res.add("Alrak 正在读这带来的宝石书.");
		if (questState.startsWith("reading")) {
			return res;
		}
		res.add("Alrak 说这本书已教会他如何制作一把刀, 听起来很不错. ");
		if (questState.equals("book_read")) {
			return res;
		}
		res.add("Alrak 说如果我杀了一条黑龙, 并找到一个 cod 和一个 黑曜石 , 他会给我制作一把刀. ");
		if (questState.equals("knife_offered")
		&& !player.hasKilled("黑龙")) {
			return res;
		}
		res.add("我杀掉了一条黑龙. ");
		if (questState.equals("knife_offered")
				&& player.hasKilled("黑龙")) {
			return res;
		}
		res.add("我拿到了 cod 和 黑曜石.");
		if (questState.equals("knife_offered")
				&& player.isEquipped("黑曜石")
				&& player.isEquipped(FISH))  {
			return res;
		}
		res.add("我把 cod 和 黑曜石 带给 Alrak. 他现在正在给我打造一把刀. ");
		if (questState.startsWith("forging")) {
			return res;
		}
		res.add("我有了自已的黑曜石刀! 这太帅了!");
		if (questState.equals("done")) {
			return res;
		}
		// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("Quest state is: " + questState);
		logger.error("History doesn't have a matching quest state for " + questState);
		return debug;
	}

	private void prepareQuestOfferingStep() {
		final SpeakerNPC npc = npcs.get("Alrak");


		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_ITEM_QUESTION,
				"你知道, 附近很难找到吃的, 过了年我可能就没有任何吃的了",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"我又有工作的激情了！我现在为 Wrvil 制作东西. 谢谢你鼓励我重新对锻造产生了兴趣. ",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "food_brought"),
				ConversationStates.QUEST_ITEM_BROUGHT,
				"我现在不再担心食物, 我觉得自已太无聊. 这是一本我爱读的书 #book . ",
				null);

		// any other state than above
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "food_brought")),
				ConversationStates.ATTENDING,
				"我相信我想让你为我做点事. ",
				null);


		/*
		 * Player agrees to collect the food asked for. his quest slot gets set
		 * with it so that later when he returns and says the food name, Alrak
		 * can check if that was the food type he was asked to bring.
		 */
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String food = player.getQuest(QUEST_SLOT);
					npc.say("谢谢你！我希望不用花太长时间收集. 当你凑够了吃的, 不要忘了对我说 '"
						+ food + ". ");
					// player.setQuest(QUEST_SLOT, food);
					// set food to null?
				}
			});

		// Player says no. they might get asked to bring a different food next
		// time but they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"我不确定我明年还能活着. 再见, 见死不救的家伙!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// Player asks what supplies he needs, and a random choice of what he
		// wants is made.
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				"supplies",
				null,
				ConversationStates.QUEST_OFFERED,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final String food = Rand.rand(FOOD_LIST);
						player.setQuest(QUEST_SLOT, food);
						npc.say("如旭你能让我 " + REQUIRED_FOOD
								+ " 一些 " + food
								+ ", 我可以为你做任何事. 你可以帮我吗?");
					}
				});
	}

	private void bringFoodStep() {
		final SpeakerNPC npc = npcs.get("Alrak");

		/** If player has quest and has brought the food, and says so, take it */
		for(final String itemName : FOOD_LIST) {
			final List<ChatAction> reward = new LinkedList<ChatAction>();
			reward.add(new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						if (player.drop(itemName, REQUIRED_FOOD)) {
							npc.say("很好! 你买入了 " + itemName + "!");
						}
					} });
			reward.add(new IncreaseXPAction(1000));
			reward.add(new IncreaseKarmaAction(35.0));
			reward.add(new SetQuestAction(QUEST_SLOT, "food_brought"));

			npc.add(ConversationStates.ATTENDING,
				itemName,
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
						return player.hasQuest(QUEST_SLOT)
								&& player.getQuest(QUEST_SLOT).equals(itemName)
								&& player.isEquipped(itemName, REQUIRED_FOOD);
					}
				},
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(reward));
		}
	}

	private void requestBookStep() {
		final SpeakerNPC npc = npcs.get("Alrak");

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				"book",
				null,
				ConversationStates.QUEST_ITEM_BROUGHT,
				"它是关于宝石 gems 和 minerals的一本书. 我敢说你一定感兴趣. . . 但你觉得你怎么能得到它？",
				null);

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"你这是侮辱, 我真的喜欢学习关于那些特殊的石头, 好吧, 再见!",
				null);

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"谢谢, 可以试着去某个图书管问问 'gem book'.",
				new SetQuestAction(QUEST_SLOT, "seeking_book"));
	}

	private void getBookStep() {
		final SpeakerNPC npc = npcs.get("Ceryl");

		npc.add(ConversationStates.ATTENDING,
				"book",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "seeking_book"), new QuestCompletedCondition("ceryl_book")),
				ConversationStates.ATTENDING,
				"现在宝石书 #gem #book 十分流行...",
				null);

		npc.add(ConversationStates.ATTENDING,
				"gem book",
				new QuestInStateCondition(QUEST_SLOT, "seeking_book"),
				ConversationStates.QUESTION_1,
				"你太走运了！上周 Ognir 刚把这本书还了回来, 现在它是给谁的？?",
				null);

		npc.add(ConversationStates.QUESTION_1,
				NAME,
				null,
				ConversationStates.ATTENDING,
				"啊, 这个 山地矮人! 希望他喜欢这本宝石书 gem book.",
				new MultipleActions(new EquipItemAction("蓝色的书", 1, true),
				new SetQuestAction(QUEST_SLOT, "got_book")));

		// allow to say goodbye while Ceryl is listening for the dwarf's name
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.GOODBYE_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Bye.", null);

		// player says something which isn't the dwarf's name.
		npc.add(ConversationStates.QUESTION_1,
				"",
				new NotCondition(new TriggerInListCondition(NAME.toLowerCase())),
				ConversationStates.QUESTION_1,
				"Hm, 你最好确认下这本书给谁. ",
				null);
	}

	private void bringBookStep() {
		final SpeakerNPC npc = npcs.get("Alrak");
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "got_book"),
						new PlayerHasItemWithHimCondition("蓝色的书")),
				ConversationStates.IDLE,
				"Great! I think I'll read this for a while. Bye!",
				new MultipleActions(
						new DropItemAction("蓝色的书"),
						new IncreaseXPAction(500),
						new SetQuestAction(QUEST_SLOT, "reading;"),
						new SetQuestToTimeStampAction(QUEST_SLOT, 1)));

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new OrCondition(new QuestInStateCondition(QUEST_SLOT,"seeking_book"), new QuestInStateCondition(QUEST_SLOT, "got_book")),
						new NotCondition(new PlayerHasItemWithHimCondition("蓝色的书"))),
				ConversationStates.ATTENDING,
				"Hello again. I hope you haven't forgotten about the gem book I wanted.",
				null);
	}

	private void offerKnifeStep() {
		final SpeakerNPC npc = npcs.get("Alrak");
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "reading;"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_DAYS * MINUTES_IN_DAYS))),
				ConversationStates.IDLE,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_DAYS * MINUTES_IN_DAYS, "I haven't finished reading that book. Maybe I'll be done in"));

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "reading;"),
						new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_DAYS * MINUTES_IN_DAYS)),
				ConversationStates.QUEST_2_OFFERED,
				"I've finished reading! That was really interesting. I learned how to make a special #小刀 from #黑曜石.",
				new SetQuestAction(QUEST_SLOT, "book_read"));


		npc.add(ConversationStates.QUEST_2_OFFERED,
				"黑曜石",
				new LevelGreaterThanCondition(REQUIRED_LEVEL),
				ConversationStates.QUEST_2_OFFERED,
				"That book says that the black gem, 黑曜石, can be used to make a very sharp cutting edge. Fascinating! If you slay a 黑龙 to bring it, I'll make a #小刀 for you.",
				new SetQuestAction(QUEST_SLOT, "knife_offered"));

		npc.add(ConversationStates.QUEST_2_OFFERED,
				"小刀",
				new LevelGreaterThanCondition(REQUIRED_LEVEL),
				ConversationStates.QUEST_2_OFFERED,
				"I'll make an 黑曜石刀 if you can slay a 黑龙 and get the gem which makes the blade. Bring a "
						+ FISH
						+ " so that I can make the bone handle, too.",
				new SetQuestAction(QUEST_SLOT, "knife_offered"));

		npc.add(ConversationStates.QUEST_2_OFFERED,
				Arrays.asList("黑曜石", "小刀"),
				new NotCondition(new LevelGreaterThanCondition(REQUIRED_LEVEL)),
				ConversationStates.ATTENDING,
				"Well, I don't think you're quite ready for such a dangerous weapon yet. How about you come back when you're above level " + Integer.toString(REQUIRED_LEVEL) + "?",
				null);

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "book_read")),
				ConversationStates.QUEST_2_OFFERED,
				"Hi! Perhaps you have come to ask about that #小刀 again ... ",
				null);

		// player says hi to NPC when equipped with the fish and the gem and
		// he's killed a 黑龙
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "knife_offered"),
						new KilledCondition("黑龙"),
						new PlayerHasItemWithHimCondition("黑曜石"),
						new PlayerHasItemWithHimCondition(FISH)),
				ConversationStates.IDLE,
				"You found the gem for the blade and the fish bone to make the handle! I'll start work right away. Come back in "
				+ REQUIRED_MINUTES + " minutes.",
				new MultipleActions(
				new DropItemAction("黑曜石"),
				new DropItemAction(FISH),
				new SetQuestAction(QUEST_SLOT, "forging;"),
				new SetQuestToTimeStampAction(QUEST_SLOT, 1)));

		// player says hi to NPC when equipped with the fish and the gem and
		// he's not killed a 黑龙
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "knife_offered"),
						new NotCondition(new KilledCondition("黑龙")),
						new PlayerHasItemWithHimCondition("黑曜石"),
						new PlayerHasItemWithHimCondition(FISH)),
				ConversationStates.ATTENDING,
				"Didn't you hear me properly? I told you to go slay a 黑龙 for the 黑曜石, not buy it! How do I know this isn't a fake gem? *grumble* I'm not making a special knife for someone who is scared to face a dragon.",
				null);

		// player says hi to NPC when not equipped with the fish and the gem
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "knife_offered"),
						new NotCondition(
								new AndCondition(
										new PlayerHasItemWithHimCondition("黑曜石"),
										new PlayerHasItemWithHimCondition(FISH)))),
				ConversationStates.ATTENDING,
				"Hello again. Don't forget I offered to make that 黑曜石刀, if you bring me a "
					+ FISH
					+ " and a piece of 黑曜石 from a 黑龙 you killed. In the meantime if I can #help you, just say the word.",
				null);

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
				ConversationStates.IDLE,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "I haven't finished making the 小刀. Please check back in"));

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new IncreaseXPAction(10000));
		reward.add(new IncreaseKarmaAction(40));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new EquipItemAction("黑曜石刀", 1, true));

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
						new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
				ConversationStates.IDLE,
				"The knife is ready! You know, that was enjoyable. I think I'll start making things again. Thanks!",
				new MultipleActions(reward));
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"黑曜石刀",
				"A skilled dwarf blacksmith in Wo'fol is getting hungry, and bored...",
				false);
		prepareQuestOfferingStep();
		bringFoodStep();
		requestBookStep();
		getBookStep();
		bringBookStep();
		offerKnifeStep();
	}

	@Override
	public String getName() {
		return "ObsidianKnife";
	}

	@Override
	public int getMinLevel() {
		return REQUIRED_LEVEL;
	}

	@Override
	public String getNPCName() {
		return "Alrak";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_MINES;
	}

}
