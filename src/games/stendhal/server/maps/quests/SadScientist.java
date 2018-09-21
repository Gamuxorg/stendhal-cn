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
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;


/**
 * QUEST: The Sad Scientist.
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Vasi Elos, a scientist in Kalavan</li>
 * <li>Mayor Sakhs, the mayor of semos</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * 		<li>Talk to Vasi Elos, a lonely scientist.</li>
 * 		<li>Give him all stuff he needs for a present for his 蜂蜜.</li>
 * 		<li>Talk to semos mayor.</li>
 * 		<li>Bring Elos mayor's letter.</li>
 * 		<li>Kill the Imperial Scientist.</li>
 *		<li>Give him the flask with his brother's blood.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * 		<li>a pair of black legs</li>
 * 		<li>20 Karma</li>
 * 		<li>10000 XP</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * 		<li>None</li>
 * </ul>
 */
public class SadScientist extends AbstractQuest {

	private static Logger logger = Logger.getLogger(SadScientist.class);

	private static final String LETTER_DESCRIPTION = "这是一封寄给 Vasi Elos 的信.";
	private static final String QUEST_SLOT = "sad_scientist";
	private static final int REQUIRED_MINUTES = 20;
	private static final String NEEDED_ITEMS = "翡翠=1;黑曜石=1;蓝宝石=1;红宝石=2;gold bar=20;mithril bar=1";

	@Override
	public String getName() {
		return "TheSadScientist";
	}

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
		final String questState = player.getQuest(QUEST_SLOT);
		// it might have been rejected before Vasi even explained what he wanted.
		if ("rejected".equals(questState)) {
			res.add("Vasi Elos 向你求助, 但我没有兴趣帮助这个科学字");
			return res;
		}
		res.add("Vasi Elos 让我带些宝石、黄金和密银, 要为他的情人 Vera 制造一只宝石腿. ");
		if (getConditionForBeingInCollectionPhase().fire(player,null,null)) {
			final ItemCollection missingItems = new ItemCollection();
			missingItems.addFromQuestStateString(questState);
			res.add("宝石腿还需要 " + missingItems.toStringList() + ".");
			return res;
		}
		res.add("Vasi Elos 需要一个基座 影子腿 shadow legs, 好把这些珠宝镶到上面");
		if ("legs".equals(questState)) {
			return res;
		}
		res.add("Vasi Elos 正在用这些宝石制作宝石腿.");
		if (questState.startsWith("making")) {
			return res;
		}
		res.add("Vasi Elos 让我找 Mayor Sakhs 寻问找出 Vera 在哪.");
		if ("find_vera".equals(questState) && !player.isEquipped("笔记")) {
			return res;
		}
		res.add("我有一张信条, 记录着一些可怕的事, 要把它送给 Vasi.");
		if ("find_vera".equals(questState) && player.isEquipped("笔记")) {
			return res;
		}
		res.add("Vera的离开让Vasi Elos 非常伤心愤怒, 我必须新旧他的亲哥哥, 并把一杯鲜血带给他.");
		if (questState.startsWith("kill_scientist") && !new KilledForQuestCondition(QUEST_SLOT, 1).fire(player, null, null)) {
			return res;
		}
		res.add("我杀了帝国科学家 Imperial Scientist Sergej Elos ,并且取了一杯鲜血作为证据");
		if (questState.startsWith("kill_scientist") && new KilledForQuestCondition(QUEST_SLOT, 1).fire(player, null, null)) {
			return res;
		}
		res.add("Vasi Elos 非常伤心, 他用鲜血倒在宝石腿上.");
		if (questState.startsWith("decorating")) {
			return res;
		}
		res.add("那个腿, 现在变成黑色的腿, 带着血和诅咒的乌黑的腿, 现属于我. " +
				"但要不惜一切代价?");
        if ("done".equals(questState)){
        	return res;
		}
        // if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("Quest state is: " + questState);
		logger.error("History doesn't have a matching quest state for " + questState);
		return debug;
	}

	/* (non-Javadoc)
	 * @see games.stendhal.server.maps.quests.AbstractQuest#addToWorld()
	 */
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"悲伤的科学家",
				"Vasi Elos, 一个孤独的科学家, 想给他的情人送一件特别的礼物.",
				false);
		prepareQuestSteps();
	}

	private void prepareQuestSteps() {
		prepareScientist();
	}

	private void prepareScientist() {
		final SpeakerNPC scientistNpc = npcs.get("Vasi Elos");
		final SpeakerNPC mayorNpc = npcs.get("Mayor Sakhs");
		startOfQuest(scientistNpc);
		bringItemsPhase(scientistNpc);
		playerReturnsAfterRequestForLegs(scientistNpc);
		playerReturnsAfterGivingTooEarly(scientistNpc);
		playerReturnsAfterGivingWhenFinished(scientistNpc);
		playerReturnsWithoutLetter(scientistNpc);
		playerVisitsMayorSakhs(mayorNpc);
		playerReturnsWithLetter(scientistNpc);
		playerReturnsWithoutKillingTheImperialScientistOrWithoutGoblet(scientistNpc);
		playerReturnsAfterKillingTheImperialScientist(scientistNpc);
		playerReturnsToFetchReward(scientistNpc);
		playerReturnsAfterCompletingQuest(scientistNpc);
	}

	private void playerReturnsToFetchReward(SpeakerNPC npc) {
		// time has passed
		final ChatCondition condition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT,"decorating"),
						new TimePassedCondition(QUEST_SLOT, 1, 5)
					);
		final ChatAction action = new MultipleActions(
											new SetQuestAction(QUEST_SLOT,"done"),
											new IncreaseKarmaAction(20),
											new IncreaseXPAction(10000),
											// here, true = bind them to player
											new EquipItemAction("black legs", 1, true)
										);
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				condition,
				ConversationStates.IDLE,
				"这是黑色之腿. 现在我求你穿上它,它象征着我的痛苦, 价钱好说",
				action);

		// time has not yet passed
		final ChatCondition notCondition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT,"decorating"),
				new NotCondition( new TimePassedCondition(QUEST_SLOT, 1, 5))
			);
		ChatAction reply = new SayTimeRemainingAction(QUEST_SLOT, 1, 5, "我还没装饰完这个腿 " +
						"请回头再来");
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				notCondition,
				ConversationStates.IDLE,
				null,
				reply);
	}

	private void playerReturnsAfterKillingTheImperialScientist(SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(
				new QuestStateStartsWithCondition(QUEST_SLOT, "kill_scientist"),
				new KilledForQuestCondition(QUEST_SLOT, 1),
				new PlayerHasItemWithHimCondition("盛血高脚杯")
			);
		ChatAction action = new MultipleActions(
										new SetQuestAction(QUEST_SLOT, "decorating;"),
										new SetQuestToTimeStampAction(QUEST_SLOT, 1),
										new DropItemAction("盛血高脚杯",1)
										);
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()), condition),
				ConversationStates.ATTENDING,
				"哈, 哈, 哈！我会把这些珠宝镶在这个腿上, 然后用这些鲜血, 它们就会转化为 " +
				"痛苦的符号 #symbol ",
				null);

		npc.add(ConversationStates.ATTENDING, "symbol",
				condition, ConversationStates.IDLE,
				"我正去创造一双黑色之腿. 请在5分钟后再来",
				action);
	}


	private void playerReturnsWithoutKillingTheImperialScientistOrWithoutGoblet(
			SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "kill_scientist"),
				new NotCondition(
						new AndCondition(
										new KilledForQuestCondition(QUEST_SLOT, 1),
										new PlayerHasItemWithHimCondition("盛血高脚杯")))
			);
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				condition, ConversationStates.IDLE,
				"我现在只有痛苦, 杀了我哥哥, 把他的血带过来, 我现在只想要这些!",
				null);
	}

	private void playerReturnsWithLetter(final SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(
				new QuestStateStartsWithCondition(QUEST_SLOT, "find_vera"),
				new PlayerHasItemWithHimCondition("笔记")
			);

		final ChatAction action = new MultipleActions(
					new SetQuestAction(QUEST_SLOT, 0, "kill_scientist"),
					new StartRecordingKillsAction(QUEST_SLOT, 1, "Sergej Elos", 0, 1),
					new DropItemAction("笔记")
				);
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()), condition),
				ConversationStates.INFORMATION_2,
				"Hello! 你给我带了什么东西吗?",
				null);

		npc.add(ConversationStates.INFORMATION_2, Arrays.asList("letter", "yes", "笔记"),
				condition,
				ConversationStates.ATTENDING,
				"Oh 不要! 我感到很痛苦, 我不再需要创造这些美丽的宝石腿了. " +
				"我想转变它, 我要把它做成痛苦的标志. 你！去杀了我哥哥 " +
				"帝国科学家 Sergej Elos. 把他的血带给我.",
				action);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.GOODBYE_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, 0, "kill_scientist"),
				ConversationStates.INFORMATION_2,
				"Do it!",
				null);
	}

	private void playerReturnsWithoutLetter(final SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "find_vera"),
				new NotCondition(new PlayerHasItemWithHimCondition("笔记"))
			);
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				condition,
				ConversationStates.IDLE,
				"问问 Mayor Sakhs 我的妻子 Vera 的近况.",
				null);
	}

	private void playerVisitsMayorSakhs(final SpeakerNPC npc) {
		final ChatAction action = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final Item item = SingletonRepository.getEntityManager().getItem("笔记");
				item.setInfoString(player.getName());
				item.setDescription(LETTER_DESCRIPTION);
				item.setBoundTo(player.getName());
				player.equipOrPutOnGround(item);
			}
		};
		npc.add(ConversationStates.ATTENDING, "Vera",
				new QuestStateStartsWithCondition(QUEST_SLOT, "find_vera"),
				ConversationStates.ATTENDING,
				"什么? 你怎么知道她? 好吧, 这是个悲伤的故事." +
				" 她正给她的朋友伊丽莎挑选海芋,她看见一个地窖的入口, " +
				" 3个月后, 一个年轻的英雄看到她成了吸血鬼. " +
				" 多么悲伤的故事. " +
				" 我把这封信留给她丈夫, " +
				" 我想他还呆在 Kalavan." ,
				action);
	}

	private void playerReturnsAfterGivingWhenFinished(final SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(
				new QuestStateStartsWithCondition(QUEST_SLOT, "making;"),
				new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)
			);
		final ChatAction action = new SetQuestAction(QUEST_SLOT,"find_vera");
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()), condition),
				ConversationStates.INFORMATION_1,
				"我完成了腿的工作. 但我不想信你, 在我把珠宝腿给你之前" +
				" 我需要给我的爱人送封信. " +
				" 问问 Mayor Sakhs , Vera 怎么样了. 你能做到吗?",
				null);

		npc.add(ConversationStates.INFORMATION_1, ConversationPhrases.YES_MESSAGES,
				condition,
				ConversationStates.IDLE,
				"Oh, 谢谢, 我等着你.",
				action);

		npc.add(ConversationStates.INFORMATION_1, ConversationPhrases.NO_MESSAGES,
				condition,
				ConversationStates.IDLE,
				"Pah! Bye!",
				null);
	}

	private void playerReturnsAfterGivingTooEarly(final SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "making;"),
				new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))
			);
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				condition,
				ConversationStates.IDLE,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "你觉得我的工作很轻松? 出去. " +
								"一会再回来"));
	}

	private void bringItemsPhase(final SpeakerNPC npc) {
		//condition for quest being active and in item collection phase
		ChatCondition itemPhaseCondition = getConditionForBeingInCollectionPhase();

		//player returns during item collection phase
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						itemPhaseCondition),
				ConversationStates.QUESTION_1,
				"Hello. 你有我制作宝石腿需要的东西 #items 吗？",
				null);

		//player asks for items
		npc.add(ConversationStates.QUESTION_1, Arrays.asList("items","item"),
				itemPhaseCondition,
				ConversationStates.QUESTION_1,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "当你有了我制作宝石腿所需要的材料时再来. 我需要以下材料 [items]."));

		//player says no
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES,
				itemPhaseCondition,
				ConversationStates.IDLE,
				"真是个败家子.",
				null);

		//player says yes
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.YES_MESSAGES,
				itemPhaseCondition,
				ConversationStates.QUESTION_1,
				"不错! 你带了些什么过来?",
				null);

		//add transition for each item
		final ItemCollection items = new ItemCollection();
		items.addFromQuestStateString(NEEDED_ITEMS);
		for (final Map.Entry<String, Integer> item : items.entrySet()) {
			npc.add(ConversationStates.QUESTION_1, item.getKey(), null,
					ConversationStates.QUESTION_1, null,
					new CollectRequestedItemsAction(
							item.getKey(), QUEST_SLOT,
							"很好, 你还有其它什么东西?",
							"你已经给过这些东西了!",
							new MultipleActions(
									new SetQuestAction(QUEST_SLOT,"legs"),
									new SayTextAction("我是一个十足的大傻瓜, 我要我的妻子记住我的爱. 当然这些腿也需要些珠宝镶上 " +
											". 请下次带来一对影之腿 shadow legs. 再见.")), ConversationStates.IDLE
							));
		}
	}


//																new SetQuestAction(QUEST_SLOT,"making;"), new SetQuestToTimeStampAction(QUEST_SLOT, 1),
	/**
	 * Creates a condition for quest being active and in item collection phase
	 * @return the condition
	 */
	private AndCondition getConditionForBeingInCollectionPhase() {
		return new AndCondition(
													new QuestActiveCondition(QUEST_SLOT),
													new NotCondition(
															new QuestStateStartsWithCondition(QUEST_SLOT,"making;")
																	),
													new NotCondition(
															new QuestStateStartsWithCondition(QUEST_SLOT,"decorating;")
																	),
													new NotCondition(
															new QuestStateStartsWithCondition(QUEST_SLOT,"find_vera")
																	),
													new NotCondition(
															new QuestStateStartsWithCondition(QUEST_SLOT,"kill_scientist")
																	),
													new NotCondition(
															new QuestStateStartsWithCondition(QUEST_SLOT,"legs")
																					)
															);
	}

	private void playerReturnsAfterRequestForLegs(final SpeakerNPC npc) {
	//player returns without legs
	final AndCondition nolegscondition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
									new QuestInStateCondition(QUEST_SLOT, "legs"),
									new NotCondition(new PlayerHasItemWithHimCondition("shadow legs"))
									);
	npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			nolegscondition,
			ConversationStates.IDLE,
			"又见面了. 请你有了 shadow legs 影之腿后再来吧, 我要为 vera 把珠宝镶到这个基座上. ",
			null);

	//player returns with legs
	final AndCondition legscondition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
								new QuestInStateCondition(QUEST_SLOT, "legs"),
								new PlayerHasItemWithHimCondition("shadow legs")
								);
	final ChatAction action = new MultipleActions(
	new SetQuestAction(QUEST_SLOT,"making;"),
	new SetQuestToTimeStampAction(QUEST_SLOT, 1),
	new DropItemAction("shadow legs"));
	npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			legscondition,
			ConversationStates.IDLE,
			"影之腿! 太好了！我马上开始工作. 我用科技的帮助做这些只用花很少时间. ! " +
			"请在 20 分钟后回来.",
			action);
	}

	private void startOfQuest(final SpeakerNPC npc) {
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"出去!",null);

		//offer the quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"所以...看起来你想帮助我?",null);

		//accept the quest
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.QUEST_STARTED,
				"我妻子住在 塞门镇 城. 她喜爱宝石. 你能给我带些宝石 #gems 吗?" +
				",我要做一件特别的腿 #legs?" ,
				null);

		// #gems
		npc.add(ConversationStates.QUEST_STARTED,
				Arrays.asList("gem","gems"),
				null,
				ConversationStates.QUEST_STARTED,
				"我需要一个 翡翠, 一个 黑曜石, 一个 蓝宝石, 两个 红宝石s, 20 枚金条 和一个  mithril 条." +
				" 你能为了我妻子弄到这些吗?",
				null);

		// #legs
		npc.add(ConversationStates.QUEST_STARTED,
				Arrays.asList("leg","legs"),
				null,
				ConversationStates.QUEST_STARTED,
				"宝石腿, 我需要一个 翡翠, 一个 黑曜石, 一个 蓝宝石, 两个 红宝石s, 20 枚金条 和一个  mithril 条." +
				" 你能为了我妻子弄到这些吗?",
				null);

		//yes, no after start of quest
		npc.add(ConversationStates.QUEST_STARTED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				"我等着你, 塞门镇 来的." ,
				new SetQuestAction(QUEST_SLOT, NEEDED_ITEMS));

		npc.add(ConversationStates.QUEST_STARTED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.QUEST_STARTED,
				"在我杀掉你之前快滚开!" ,
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

		//reject the quest
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"如果你改变主意, 再来问我..." ,
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	private void playerReturnsAfterCompletingQuest(final SpeakerNPC npc) {
		// after finishing the quest, just tell them to go away, and mean it.
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.IDLE,
				"Go away!",null);
	}

	// The items and surviving in the basement mean we shouldn't direct them till level 100 or so
	@Override
	public int getMinLevel() {
		return 100;
	}

	@Override
	public String getRegion() {
		return Region.KALAVAN;
	}

	@Override
	public String getNPCName() {
		return "Vasi Elos";
	}
}
