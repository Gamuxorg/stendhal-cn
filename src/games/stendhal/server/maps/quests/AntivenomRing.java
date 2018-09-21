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
import java.util.Map;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

/**
 * QUEST: Antivenom Ring
 *
 * PARTICIPANTS:
 * <ul>
 * <li>詹姆森 (the retired apothecary)</li>
 * <li>Other NPCs to give hints at location of apothecary's lab (undecided)</li>
 * <li>Another NPC that fuses the ring (undecided)</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Bring note to apothecary to 詹姆森.</li>
 * <li>As a favor to 克拉丝, 詹姆森 will help you to strengthen your medicinal ring.</li>
 * <li>Bring 詹姆森 a medicinal ring, venom gland, 2 曼德拉草 and 5 fairycakes.</li>
 * <li>詹姆森 requires a bottle big enough to hold venom extracted from gland.</li>
 * <li>Bring 詹姆森 a giant bottle.</li>
 * <li>詹姆森 realizes he doesn't have a way to extract the venom.</li>
 * <li>Find [NPC undecided] who will extract the venom into the giant bottle.</li>
 * <li>Take the bottle filled with venom back to 詹姆森.</li>
 * <li>詹姆森 concocts a mixture to infuse the ring.</li>
 * <li>Take mixture and ring to [NPC undecided] to be fused.</li>
 * <li>[NPC undecided] will also have requirements for the player.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>2000 XP</li>
 * <li>antivenom ring</li>
 * <li>Karma: 25???</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 *
 *
 * @author AntumDeluge
 */
public class AntivenomRing extends AbstractQuest {

	private static final String QUEST_SLOT = "antivenom_ring";

	//public static final String NEEDED_ITEMS = "medicinal ring=1;venom gland=1;曼德拉草=2;仙女蛋糕=5";

	/* Items taken to ??? to create cobra venom */
	public static final String EXTRACTION_ITEMS = "venom gland=1;vial=1";

	/* Items taken to apothecary to create antivenom */
	public static final String MIX_ITEMS = "cobra venom=1;曼德拉草=2;仙女蛋糕=5";

	/* Items taken to ??? to create antivenom ring */
	public static final int REQUIRED_MONEY = 10000;
	public static final String FUSION_ITEMS = "antivenom=1;medicinal ring=1";

	//private static final int REQUIRED_MINUTES = 30;

	private static final int EXTRACTION_TIME = 10;

	private static final int MIX_TIME = 10;

	private static final int FUSION_TIME = 30;

	// NPCs involved in quest
	private final SpeakerNPC mixer = npcs.get("詹姆森");
	// FIXME: find NPCs for these roles
	private final SpeakerNPC extractor = npcs.get("");
	private final SpeakerNPC fuser = npcs.get("Hogart");

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("在 塞门镇 山,我找到了隐士药济师的试验室.");
		String quest = player.getQuest(QUEST_SLOT, 0);
		final String[] questState = player.getQuest(QUEST_SLOT).split(",");
		if ("done".equals(quest)) {
			res.add("我收集到了 詹姆森 要的全部原料. 他把这些特殊的原料混合进我的戒指, 使这个戒指的抗毒性更强了. 我还得到了一些经验和运气.");
		}
		else if ("rejected".equals(quest)) {
			res.add("毒非常危险, 我可不想受害. ");
		}
		else {
			if (questState[0].split("=")[0] == "mixing") {
				res.add(mixer.getName() + " 正在混入抗毒济. ");
			}
			else {
				final ItemCollection missingMixItems = new ItemCollection();
				missingMixItems.addFromQuestStateString(questState[0]);
				res.add("我还需要把 詹姆森 带来 " + missingMixItems.toStringList() + ".");
			}

			if (questState[1].split("=")[0] == "extracting") {
				res.add(extractor.getName() + " 正把毒汁提取出来. ");
			}

			if (questState[2].split("=")[0] == "fusing") {
				res.add(fuser.getName() + " 正在制成我的戒指. ");
			}
		}
		return res;
	}

	private void prepareHintNPCs() {
		final SpeakerNPC hintNPC1 = npcs.get("Valo");
		final SpeakerNPC hintNPC2 = npcs.get("海震");
		final SpeakerNPC hintNPC3 = npcs.get("Ortiv Milquetoast");

		// Valo is asked about an apothecary
		hintNPC1.add(ConversationStates.ATTENDING,
				"药济师",
				null,
				ConversationStates.ATTENDING,
				"嗯, 是的, 我知道以前有个学习药济和解毒的人. 最后我听说他 #隐居 在深山里.",
				null);

		hintNPC1.add(ConversationStates.ATTENDING,
				Arrays.asList("隐居", "深山"),
				null,
				ConversationStates.ATTENDING,
				"他可能藏起来了, 眼睁大点, 注意隐藏的入口",
				null);

		// 海震 is asked about an apothecary
		hintNPC2.add(ConversationStates.ATTENDING,
				"药济师",
				null,
				ConversationStates.ATTENDING,
				"是的, 在 Kalavan 有个贱人. 但是,由于领导权之争, 他被迫离开, 听说她现在 #隐藏 在塞门地区的某处.",
				null);

		hintNPC2.add(ConversationStates.ATTENDING,
				"隐藏",
				null,
				ConversationStates.ATTENDING,
				"要是我也藏起来, 我一定会找一个有隐蔽入口的安全一点的房子. ",
				null);

		// Ortiv Milquetoast is asked about an apothecary
		hintNPC3.add(ConversationStates.ATTENDING,
				"药济师",
				null,
				ConversationStates.ATTENDING,
				"你一定要和我的同事 詹姆森 谈谈. 因为在 Dalavan 的事, 他也被迫 #藏 了起来. 他没对我说具体在哪, 但他拜访我时, 总带一些好吃的过来.",
				null);

		hintNPC3.add(ConversationStates.ATTENDING,
				Arrays.asList("藏", "隐藏"),
				null,
				ConversationStates.ATTENDING,
				"他提到他在一个自己建的隐密的实验室, 入口也是隐藏的.",
				null);
	}

	/**
	 * Quest starting point
	 */
	private void requestAntivenom() {
		// If player has note to apothecary then quest is offered
		mixer.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(mixer.getName()),
						new PlayerHasItemWithHimCondition("note to apothecary"),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.QUEST_OFFERED,
				"Oh, 克拉丝的信. 是给我的吗?",
				null);

		// In case player dropped note before speaking to 詹姆森
		mixer.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(mixer.getName()),
						new PlayerHasItemWithHimCondition("note to apothecary"),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.QUEST_OFFERED,
				"Oh, 克拉丝 的信. 是给我的吗?",
				null);

		// Player accepts quest
		mixer.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(new SetQuestAction(QUEST_SLOT, 0, MIX_ITEMS),
						new IncreaseKarmaAction(5.0),
						new DropItemAction("note to apothecary"),
						new SayRequiredItemsFromCollectionAction(QUEST_SLOT, 0, "克拉丝 让我协助你, 我能做一个指环, 用它提高抗毒性, 我需要你带来 [items].  你带了这些物品吗？")
				)
		);

		// Player tries to leave without accepting/rejecting the quest
		mixer.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.GOODBYE_MESSAGES,
				null,
				ConversationStates.QUEST_OFFERED,
				"这不是一个 #是否 的问题. 我说, 这是你带来的信吗?",
				null);

		// Player rejects quest
		mixer.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				// NPC walks away
				ConversationStates.IDLE,
				"Oh, 好的, 带上这个.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// Player asks for quest without having Klass's note
		mixer.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new PlayerHasItemWithHimCondition("note to apothecary")),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"抱歉, 但我现在很心, 可能你应该得 #克拉丝 说.",
				null);

		// Player asks for quest after it is started
		mixer.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStartedCondition(QUEST_SLOT),
						new QuestNotCompletedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "我还在等你给我带来 [items]. 你找到这些了吗?"));

		// Quest has previously been completed.
		mixer.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				"非常感谢. 从上次吃到我喜欢的 仙女蛋糕 己过了很长时间了吧, 你喜欢你的戒指吗？",
				null);

		// Player is enjoying the ring
		mixer.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"太好了!",
				null);

		// Player is not enjoying the ring
		mixer.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Oh, 郁闷.",
				null);
		/*
        // Player asks about required items
		mixer.add(ConversationStates.QUESTION_1,
				Arrays.asList("gland", "venom gland", "glands", "venom glands"),
				null,
				ConversationStates.QUESTION_1,
				"Some #snakes have a gland in which their venom is stored.",
				null);

		npc.add(ConversationStates.QUESTION_1,
				Arrays.asList("曼德拉草", "曼德拉草s", "root of 曼德拉草", "roots of 曼德拉草", "root of 曼德拉草s", "roots of 曼德拉草s"),
				null,
				ConversationStates.QUESTION_1,
				"This is my favorite of all herbs and one of the most rare. Out past Kalavan there is a hidden path in the trees. At the end you will find what you are looking for.",
				null);
		*/
		mixer.add(ConversationStates.QUESTION_1,
				Arrays.asList("蛋糕", "仙女蛋糕"),
				null,
				ConversationStates.QUESTION_1,
				"Oh, 我是我品尝过的最好的食物. 只有最重的生物才能做出如此可口的食物. ",
				null);

		// Player asks about rings
		mixer.add(ConversationStates.QUESTION_1,
				Arrays.asList("ring", "rings"),
				null,
				ConversationStates.QUESTION_1,
				"有很多类型的指环.",
				null);

		mixer.add(ConversationStates.QUESTION_1,
				Arrays.asList("medicinal ring", "medicinal rings"),
				null,
				ConversationStates.QUESTION_1,
				"在一些毒物体内存在. ",
				null);

		mixer.add(ConversationStates.QUESTION_1,
				Arrays.asList("antivenom ring", "antivenom rings"),
				null,
				ConversationStates.QUESTION_1,
				"如果你能把它带来, 我可以加强一个 #medicinal #ring.",
				null);

		mixer.add(ConversationStates.QUESTION_1,
				Arrays.asList("antitoxin ring", "antitoxin rings", "gm antitoxin ring", "gm antitoxin rings"),
				null,
				ConversationStates.QUESTION_1,
				"Heh! 这是终极的抗毒物品了. 能得到它运气真的不错！",
				null);
		/*
		// Player asks about snakes
		npc.add(ConversationStates.QUESTION_1,
				Arrays.asList("蛇", "snakes", "眼镜蛇", "cobras"),
				null,
				ConversationStates.QUESTION_1,
				"I've heard rumor newly discovered pit full of snakes somewhere in Ados. But I've never searched for it myself. That kind of work is better left to adventurers.",
				null);

        // Player asks about required items
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("gland", "venom gland", "glands", "venom glands"),
				null,
				ConversationStates.ATTENDING,
				"Some #snakes have a gland in which their venom is stored.",
				null);

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("曼德拉草", "曼德拉草s", "root of 曼德拉草", "roots of 曼德拉草", "root of 曼德拉草s", "roots of 曼德拉草s"),
				null,
				ConversationStates.ATTENDING,
				"This is my favorite of all herbs and one of the most rare. Out past Kalavan there is a hidden path in the trees. At the end you will find what you are looking for.",
				null);
		*/
		mixer.add(ConversationStates.ATTENDING,
				Arrays.asList("蛋糕", "仙女蛋糕"),
				null,
				ConversationStates.ATTENDING,
				"Oh, 这是我吃过最好的食物了. 只有最重的生物才能做出如此好吃的食物. ",
				null);

		// Player asks about rings
		mixer.add(ConversationStates.ATTENDING,
				Arrays.asList("ring", "rings"),
				null,
				ConversationStates.ATTENDING,
				"有很多种类的指环.",
				null);

		mixer.add(ConversationStates.ATTENDING,
				Arrays.asList("medicinal ring", "medicinal rings"),
				null,
				ConversationStates.ATTENDING,
				"一些生物身上带着毒.",
				null);

		mixer.add(ConversationStates.ATTENDING,
				Arrays.asList("antivenom ring", "antivenom rings"),
				null,
				ConversationStates.ATTENDING,
				"如果你把我需要的东西带来, 我会加强一个药济指环 #medicinal #ring.",
				null);

		mixer.add(ConversationStates.ATTENDING,
				Arrays.asList("antitoxin ring", "antitoxin rings", "gm antitoxin ring", "gm antitoxin rings"),
				null,
				ConversationStates.ATTENDING,
				"Heh! 这是终极抗毒药, 能做出来运气真的不错!",
				null);
		/*
		// Player asks about snakes
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("蛇", "snakes", "眼镜蛇", "cobras"),
				null,
				ConversationStates.ATTENDING,
				"I've heard rumor newly discovered pit full of snakes somewhere in Ados. But I've never searched for it myself. That kind of work is better left to adventurers.",
				null);
		*/
	}

	private void mixAntivenom() {
		// FIXME: Condition must apply to "mixing" state and anything afterward
		mixer.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(mixer.getName()),
						new QuestActiveCondition(QUEST_SLOT),
						new NotCondition(new QuestInStateCondition(QUEST_SLOT, 0, "mixing"))),
				ConversationStates.ATTENDING,
				"你来了, 你带来我要的 #东西 了吗?",
				null);

		// player asks what is missing (says "items")
		mixer.add(ConversationStates.ATTENDING,
				Arrays.asList("什么东西", "东西", "什么"),
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "我需要 [items]. 你身上带着吗?"));

		// player says has a required item with him (says "yes")
		mixer.add(ConversationStates.ATTENDING,
				ConversationPhrases.YES_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_2,
				"你带来了什么?",
				null);

		// Players says has required items (alternate conversation state)
		mixer.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_2,
				"你带来了什么?",
				null);

		// player says does not have a required item with him (says "no")
		mixer.add(ConversationStates.ATTENDING,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Okay. 我还需要 [items]"));

		// Players says does not have required items (alternate conversation state)
		mixer.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"Okay. 我想知道你是怎么找到这些东西的.",
				null);//new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Okay. I still need [items]"));

		List<String> GOODBYE_NO_MESSAGES = new ArrayList<String>();
		for (String message : ConversationPhrases.GOODBYE_MESSAGES) {
			GOODBYE_NO_MESSAGES.add(message);
		}
		for (String message : ConversationPhrases.NO_MESSAGES) {
			GOODBYE_NO_MESSAGES.add(message);
		}

		// player says "bye" while listing items
		mixer.add(ConversationStates.QUESTION_2,
				GOODBYE_NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Okay. 我还需要 [items]"));

		// Returned too early; still working
		mixer.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(mixer.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "enhancing;"),
				new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, MIX_TIME))),
				ConversationStates.IDLE,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, MIX_TIME, "我还没完成指环制作, 请稍侯再来 "));

/*		// player says he didn't bring any items (says no)
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"Ok. Let me know when you have found something.",
				null);

		// player says he didn't bring any items to different question
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"Ok. Let me know when you have found something.",
				null);
		*/
		// player offers item that isn't in the list.
		mixer.add(ConversationStates.QUESTION_2, "",
			new AndCondition(new QuestActiveCondition(QUEST_SLOT),
					new NotCondition(new TriggerInListCondition(MIX_ITEMS))),
			ConversationStates.QUESTION_2,
			"我不确定我要的东西.", null);

		ChatAction mixAction = new MultipleActions (
		new SetQuestAction(QUEST_SLOT, 1, "mixing"),
		new SetQuestToTimeStampAction(QUEST_SLOT, 4),
		new SayTextAction("谢谢你, 在这我吃完这美味的仙女蛋糕后, 我会去调试抗毒药的正确比例. 请在" + MIX_TIME + " 分钟后回来取.")
		);

		/* add triggers for the item names */
		final ItemCollection items = new ItemCollection();
		items.addFromQuestStateString(MIX_ITEMS);
		for (final Map.Entry<String, Integer> item : items.entrySet()) {
			mixer.add(ConversationStates.QUESTION_2,
					item.getKey(),
					new QuestActiveCondition(QUEST_SLOT),
					ConversationStates.QUESTION_2,
					null,
					new CollectRequestedItemsAction(
							item.getKey(),
							QUEST_SLOT,
							"太好了！你身上还有其他东西吗？",
							"你带的东西我已经有了",
							mixAction,
							ConversationStates.IDLE
							)
			);
		}

		final List<ChatAction> mixReward = new LinkedList<ChatAction>();
		//reward.add(new IncreaseXPAction(2000));
		//reward.add(new IncreaseKarmaAction(25.0));
		mixReward.add(new EquipItemAction("antivenom", 1, true));
		mixReward.add(new SetQuestAction(QUEST_SLOT, 1, "mixing=done"));

		mixer.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(mixer.getName()),
						new QuestInStateCondition(QUEST_SLOT, 1, "mixing"),
						new TimePassedCondition(QUEST_SLOT, 1, MIX_TIME)
				),
			ConversationStates.IDLE,
			"我完成了抗毒混合药, 你城要找到某个能把它 #fuse 熔合到某个物品里的人. 没有其他事的话, 现在我要吃完剩下的仙女蛋糕了. ",
			new MultipleActions(mixReward));

	}

	private void requestCobraVenom() {
		// Player asks for antivenom
		extractor.add(ConversationStates.ATTENDING,
				Arrays.asList("詹姆森", "antivenom", "extract", "眼镜蛇", "venom"),
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						new NotCondition(new QuestInStateCondition(QUEST_SLOT, 1, "extracting=done")
						)
				),
				ConversationStates.QUESTION_1,
				"那是什么, 你需要一些毒液可能造出一个 antivemon? 我能从眼镜蛇的腺体中取出毒液, 但我需要一个小瓶子装它, 你能去找一只吗？",
				null);

		// Player will retrieve items
		extractor.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				null,
				new MultipleActions(new SetQuestAction(QUEST_SLOT, 1, EXTRACTION_ITEMS),
						new SayRequiredItemsFromCollectionAction(QUEST_SLOT, 1, "很好！我需要 [items]. 你身上带着一些吗？")
				)
		);
	}

	private void extractCobraVenom() {

	}

	private void requestAntivenomRing() {
		// Greeting while quest is active
		fuser.add(
				ConversationStates.ATTENDING,
				Arrays.asList("詹姆森", "antivenom", "ring", "fuse"),
				new AndCondition(
						new QuestActiveCondition(QUEST_SLOT),
						new NotCondition(
								new QuestInStateCondition(QUEST_SLOT, 2, "fusing=done")
								)
						),
				ConversationStates.QUESTION_1,
				null,
				new MultipleActions(new SetQuestAction(QUEST_SLOT, 2, FUSION_ITEMS),
						new SayRequiredItemsFromCollectionAction(QUEST_SLOT, 2, "你需要一个强力抗毒的物品使你免受毒害吗？我可以把抗毒液混入医疗戒指 medicinal ring 医疗戒指中, 让它的抗毒性更强, 当然代价不低, 我需要 [items]. 然后你还需要付 " + Integer.toString(REQUIRED_MONEY) + " 钱币, 你能把这些全都找来吗？")
				)
		);

		// Player will retrieve items
		fuser.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				null,
				new MultipleActions(new SetQuestAction(QUEST_SLOT, 2, EXTRACTION_ITEMS),
						new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "好吧, 你找到我要的东西了吗？")
				)
		);
	}

	private void fuseAntivenomRing() {

	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Antivenom Ring",
				"作为送给老朋友的礼物, 药济师 詹姆森 能够加强医疗戒指 medicinal ring.",
				false);
		prepareHintNPCs();
		requestAntivenom();
		mixAntivenom();
		requestCobraVenom();
		extractCobraVenom();
		requestAntivenomRing();
		fuseAntivenomRing();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "AntivenomRing";
	}

	public String getTitle() {
		return "AntivenomRing";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}

	@Override
	public String getNPCName() {
		return "詹姆森";
	}
}
