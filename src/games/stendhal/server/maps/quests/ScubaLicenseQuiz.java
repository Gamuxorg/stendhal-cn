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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Rand;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.JokerExprMatcher;
import games.stendhal.common.parser.Sentence;
import games.stendhal.common.parser.SimilarExprMatcher;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Quest to get the 潜水装.
 * <p>
 *
 * PARTICIPANTS: <ul><li> Edward the diving instructor</ul>
 *
 *
 * STEPS: <ul><li> This quest is about players getting the ability to dive and earn the necessary equipment.
 *  The instructor will as a question and once the player answers correctly will reward them with 潜水装.</ul>
 *
 *
 * REWARD:
 * <ul>
 * <li> 100 XP
 * <li> some karma (5)
 * <li> The 潜水装
 * </ul>
 *
 * REPETITIONS: <ul><li> no repetitions</ul>
 *
 * @author soniccuz based on (LookUpQuote by dine)
 */

public class ScubaLicenseQuiz extends AbstractQuest {
	private static final String QUEST_SLOT = "get_diving_license";

	private static Map<String, String> anwsers = new HashMap<String, String>();
	static {
		anwsers.put("潜水结束后, 氮气泡泡封住了你身体的血, 这种感觉叫?",
				"减压症");
		anwsers.put("空气中氧气占比多少? 只写数字.",
						"21");
		anwsers.put("起浪是由于 ...",
						"风");
		anwsers.put("大多数潜水装的损坏是因鱼和水生动物引起, 原因是它们____你.",
						"害怕");
		anwsers.put("当你____时, 不应考虑潜水",
						"感冒");
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
		res.add("我遇见了潜水员 Edward , 他教人们如何潜水. 如果我能通过他的考试, 我就可以得到一张潜水执照.");
		if (!player.isQuestCompleted(QUEST_SLOT)) {
			res.add("我必须回答的问题是 " + player.getQuest(QUEST_SLOT) + ".");
		} else {
			res.add("我通过 Edward 的考试, 并且拿到了潜水执照.");
		}
		return res;
	}

	private void createLicense() {
		final SpeakerNPC instructor = npcs.get("Edward");

		instructor.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new GreetingMatchesNameCondition(instructor.getName()), true,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					if (!player.hasQuest(QUEST_SLOT)) {
						npc.say("Hi 我是 Faiumoni 唯一的潜水教练. 如果你想畅游美妙的海底世界, 还需要一张潜水 #执照 和 #潜水装.");
					} else if (!player.isQuestCompleted(QUEST_SLOT)) {
						final String name = player.getQuest(QUEST_SLOT);
						npc.say("你回来了! 我想信你完成学业并能正确回答这些问题. " + name);
						npc.setCurrentState(ConversationStates.QUESTION_1);
					} else {
						npc.say("欢迎上船!");
					}
				}
			});

		instructor.add(ConversationStates.ATTENDING,
				ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, Arrays.asList("考试", "测验")),
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"确定要开始答题?",
				null);

		// TODO: point to diving location
		instructor.add(ConversationStates.ATTENDING,
				ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, Arrays.asList("考试", "测验")),
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"你通过了考试! 现在可以去找个好的潜水地点了.",
				null);

		instructor.add(ConversationStates.ATTENDING,
				ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, Arrays.asList("考试", "测验")),
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final String name = player.getQuest(QUEST_SLOT);
						npc.say("我想信你完成学业并能正确回答这些问题. " + name);
					}
				});

		instructor.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Okay, 潜水不是适合所有人, 但如果你改变主意不要犹豫马上回来. 同时感受自由的 #学习.", null);

		instructor.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.QUESTION_1, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String name = Rand.rand(anwsers.keySet());
					npc.say("很好. 下面是你的问题. " + name);
					player.setQuest(QUEST_SLOT, name);
				}
			});

		/*
		instructor.add(ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Too bad. You're not qualified to dive until you know the answer. You should #study.", null);
		*/

		// TODO: rewrite this to use standard conditions and actions
		instructor.addMatching(ConversationStates.QUESTION_1, Expression.JOKER, new JokerExprMatcher(), null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String name = player.getQuest(QUEST_SLOT);
					final String quote = anwsers.get(name);

					final Sentence answer = sentence.parseAsMatchingSource();
					final Sentence expected = ConversationParser.parse(quote, new SimilarExprMatcher());

					if (answer.matchesFull(expected)) {
						npc.say("正确, 不错! 现在你有潜水资格了! 但首先你还要 #买 #潜水装 . 恐怕我不能再给你免费使用了.");
						//Free samples are over.
						player.addXP(100);
						player.addKarma(5);
						player.setQuest(QUEST_SLOT, "done");
						player.notifyWorldAboutChanges();
					} else if (ConversationPhrases.GOODBYE_MESSAGES.contains(sentence.getTriggerExpression().getNormalized())) {
						npc.say("再见 - 下次再来!");
						npc.setCurrentState(ConversationStates.IDLE);
					} else {
						npc.setCurrentState(ConversationStates.ATTENDING);
						npc.say("不对. #学习 完成后再来吧.");
					}
				}
			});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"潜水资格考试",
				"Edward 掌管潜水执照的发放. 但要考试合格",
				false);
		createLicense();
	}
	@Override
	public String getName() {
		return "DivingLicenseQuiz";
	}

	@Override
	public String getRegion() {
		return Region.ATHOR_ISLAND;
	}
	@Override
	public String getNPCName() {
		return "Edward";
	}

	/**
	 * is scuba diving possible?
	 */
	public static class ScubaCondition implements ChatCondition {

        @Override
        public boolean fire(Player player, Sentence sentence, Entity npc) {
            return player.isEquippedItemInSlot("armor", "潜水装") && player.isQuestCompleted("get_diving_license");
        }

        @Override
        public int hashCode() {
            return -13527181;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ScubaCondition;
        }

        @Override
        public String toString() {
            return "scuba?";
        }
	}
}
