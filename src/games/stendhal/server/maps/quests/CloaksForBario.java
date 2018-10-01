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
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.MathHelper;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;

/**
 * QUEST: Cloaks for Bario
 *
 * PARTICIPANTS:
 * <ul>
 * <li> Bario, a guy living in an underground house deep under the Ados Wildlife Refuge</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Bario asks you for a number of 蓝灵斗篷s.</li>
 * <li> You get some of the cloaks somehow, e.g. by killing elves.</li>
 * <li> You bring the cloaks to Bario and give them to him.</li>
 * <li> Repeat until Bario received enough cloaks. (Of course you can bring up
 * all requi红斗篷s at the same time.)</li>
 * <li> Bario gives you a 金盾 in exchange.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 金盾</li>
 * <li> 15000 XP</li>
 * <li> Karma: 25</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> None.</li>
 * </ul>
 */
public class CloaksForBario extends AbstractQuest {

	private static final int REQUIRED_CLOAKS = 10;

	private static final String QUEST_SLOT = "cloaks_for_bario";


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Bario");

		// player says hi before starting the quest
		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Hey! 你是怎么下来的? 你做了什么? Huh. 好吧, 我是Bario. 不敢想像你能为我做 #任务.",
				null);

		// player is willing to help
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"我再不敢上楼, 因为我从矮人那儿偷了一个啤酒桶. 但它在那里太凉... 你能帮忙吗?",
				null);

		// player should already be getting cloaks
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"你答应给我带10个蓝灵斗篷. 记得吗?",
				null);

		// player has already finished the quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"我不需要你做什么, 真的.", null);

		// player is willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"如果我想活过冬天, 就需要一些蓝灵斗篷. 请带10个来, 我会给你报酬.",
				new SetQuestAction(QUEST_SLOT, Integer.toString(REQUIRED_CLOAKS)));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Oh 天啊... 我该怎么办...",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void step_2() {
		// Just find some of the cloaks somewhere and bring them to Bario.
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Bario");

		// player returns while quest is still active
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUESTION_1, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("又见面了! 我还需要 "
							+ player.getQuest(QUEST_SLOT)
							+ " 蓝色精灵 "
							+ MathHelper.parseInt(player.getQuest(QUEST_SLOT))
								+	"斗篷" + ". 你带的有吗?");
					}
				});

		// player returns after finishing the quest
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"欢迎! 再次谢谢你的斗篷.", null);

		// player says he doesn't have any 蓝灵斗篷s with him
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "真失望.", null);

		// player says he has a 蓝灵斗篷 with him but he needs to bring more than one still
		// could also have used GreaterThanCondition for Quest State but this is okay, note we can only get to question 1 if we were active
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, "1"), new PlayerHasItemWithHimCondition("蓝灵斗篷")),
				ConversationStates.QUESTION_1, null,
				new MultipleActions(
						new DropItemAction("蓝灵斗篷"),
						new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
								// find out how many cloaks the player still has to
								// bring. incase something has gone wrong and we can't parse the slot, assume it was just started
								final int toBring = MathHelper.parseIntDefault(player.getQuest(QUEST_SLOT),  REQUIRED_CLOAKS) -1;

								player.setQuest(QUEST_SLOT,
										Integer.toString(toBring));
								raiser.say("非常感谢! 你还带的有吗? 我还需要 "
										+ toBring +
												"斗篷"+ "one" + ".");

							}
						}));

		// player says he has a 蓝灵斗篷 with him and it's the last one
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("蓝灵斗篷"));
		reward.add(new EquipItemAction("金盾", 1, true));
		reward.add(new IncreaseXPAction(15000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(25));
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "1"), new PlayerHasItemWithHimCondition("蓝灵斗篷")),
				ConversationStates.ATTENDING,
				"太谢谢了! 现在我有足够的斗篷过冬了. 给你, 这面金盾是送你的.",
				new MultipleActions(reward));

		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(new PlayerHasItemWithHimCondition("蓝灵斗篷")),
				ConversationStates.ATTENDING,
				"真的? 没看到啊...",
				null);
	}

	@Override
	public void addToWorld() {
		step_1();
		step_2();
		step_3();
		fillQuestInfo(
				"Bario的斗篷",
				"Bario, 一个冻僵的矮人, 需要用斗篷保暖.",
				false);
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("我在阿多斯西北的郊外的地下遇见了一个冻僵的矮人. 他问我要了 10 个蓝灵斗篷.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("我不想帮助 Bario.");
		} else if (!questState.equals("done")) {
			int cloaks = MathHelper.parseIntDefault(player.getQuest(QUEST_SLOT),  REQUIRED_CLOAKS);
			res.add("我需要给 Bario 送去 " + cloaks + "蓝灵斗篷"+ "one" + "." );
		} else {
			res.add("Bario 给了我一面贵重的 金盾 来交换 精灵斗篷!");
		}
		return res;
	}

	@Override
	public String getName() {
		return "CloaksForBario";
	}

	@Override
	public int getMinLevel() {
		return 20;
	}
	@Override
	public String getNPCName() {
		return "Bario";
	}
}
