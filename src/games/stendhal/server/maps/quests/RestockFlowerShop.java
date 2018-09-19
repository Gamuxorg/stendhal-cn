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
import games.stendhal.server.entity.npc.action.AddItemToCollectionAction;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
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
import games.stendhal.server.entity.npc.action.StartItemsCollectionWithLimitAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

/**
 * QUEST: Restock the Flower Shop
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Seremela, the elf girl who watches over Nalwor's flower shop</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Seremela asks you to bring a variety of flowers to restock the flower shop and 15 bottles of water to maintain them</li>
 * <li>Bring the requested amounts water and each flower type to Seremela</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>1000 XP</li>
 * <li>25 karma</li>
 * <li>5 纳尔沃城回城卷s</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Once every 3 days</li>
 * </ul>
 *
 * @author AntumDeluge
 *
 */
public class RestockFlowerShop extends AbstractQuest {
	public static final String QUEST_SLOT = "restock_flowershop";

	// Different types of flowers needed in quest
	private static final List<String> flowerTypes = Arrays.asList(
			"雏菊", "紫丁香", "三色堇", "玫瑰", "马蹄莲");
	public static List<Integer> requestedQuantities = Arrays.asList();

	private final int MAX_FLOWERS = flowerTypes.size() * 10;

	private static final int REQ_WATER = 15;

	// Time player must wait to repeat quest (3 days)
	private static final int WAIT_TIME = 60 * 24 * 3;

	// Quest NPC
	private final SpeakerNPC npc = npcs.get("Seremela");

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		String npcName = npc.getName();
		if (player.isQuestInState(QUEST_SLOT, 0, "rejected")) {
			res.add("花粉老让我打喷嚏.");
		} else if (!player.isQuestInState(QUEST_SLOT, 0, "done")) {
			String questState = player.getQuest(QUEST_SLOT);
			res.add("我已给 " + npcName + " 提供帮助重建了花店.");

			final ItemCollection remaining = new ItemCollection();
			remaining.addFromQuestStateString(questState);

			// Check to avoid ArrayIndexOutOfBoundsException
			if (remaining.size() > 0) {
				String requestedFlowers = "我还需要带来以下种类的花: " + remaining.toStringList() + ".";
				res.add(requestedFlowers);
			}
		} else {
            if (isRepeatable(player)) {
                res.add("我帮助 " + npcName + " 已经有些时日了.可能她又需要我的帮助了.");
            } else {
                res.add(npcName + " 现在花的供应够好了.");
            }
		}

		return res;
	}


	private void setupBasicResponses() {

		List<List<String>> keywords = Arrays.asList(
				Arrays.asList("花"),
				ConversationPhrases.HELP_MESSAGES);
		List<String> responses = Arrays.asList(
				"这些花不好看吗?",
				"Hmmmm, 我不认为我能帮助你什么");

		for (int i = 0; i < responses.size(); i++) {
			npc.add(ConversationStates.ANY,
					keywords.get(i),
					new NotCondition(new QuestActiveCondition(QUEST_SLOT)),
					ConversationStates.ATTENDING,
					responses.get(i),
					null);
		}
	}

	private void setupActiveQuestResponses() {

		// Player asks to be reminded of remaining flowers required
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("花", "记得", "什么", "物品", "目录", "东西"),
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "我还需要 [items]. 你身上带的有吗?"));

        npc.add(ConversationStates.QUESTION_1,
                Arrays.asList("花", "记得", "什么", "物品", "目录", "东西"),
                new QuestActiveCondition(QUEST_SLOT),
                ConversationStates.QUESTION_1,
                null,
                new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "我还需要 [items]. 你身上带的有吗?"));

        // Player asks to be reminded of remaining flowers required
        npc.add(ConversationStates.QUESTION_1,
                Arrays.asList("花", "记得", "什么", "物品", "目录"),
                new QuestActiveCondition(QUEST_SLOT),
                ConversationStates.QUESTION_1,
                null,
                new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "我还需要 [items]. 你身上带的有吗?"));

		List<List<String>> keywords = Arrays.asList(
				Arrays.asList("雏菊", "一束雏菊", "紫丁香", "三色堇"),
				Arrays.asList("玫瑰"),
				Arrays.asList("马蹄莲"),
				Arrays.asList("水"),
				Arrays.asList("谁", "在哪"),
				Arrays.asList("詹妮"),
				Arrays.asList("弗乐尔"),
				Arrays.asList("瓶子"),
				ConversationPhrases.HELP_MESSAGES);
		List<String> responses = new ArrayList<String>();
		responses.add("#詹妮 带着这种花的种子");
		responses.add("#弗乐尔 有最好的 玫瑰. ");
		responses.add("#马蹄莲 是我最喜欢的花. 一些人叫它海芋或是叫它丁香, 然而它不是真正的丁香. 可以去向 #詹妮 要一些马蹄莲球茎. ");
		responses.add("我需要用水为这些 #花 保鲜.你要找些源, 然后装到这个 #瓶子 里. 也可以去买些水. ");
		responses.add("#詹妮 懂得很多花的知识, 也可以找他问关于 #弗乐尔 的消息.");
		responses.add("詹妮常常呆在磨坊, 可以在挨着塞门镇的大风车旁边找找. ");
		responses.add("弗乐尔 在 Kirdneh 的市场工作");
		responses.add("去塞门镇 的酒吧打听一下.");
		responses.add("我还 #记得 你要带给我的 #花 !也帮你指出 #去哪里 可以找到");

		for (int f = 0; f < responses.size(); f++) {
			npc.add(ConversationStates.ANY,
					keywords.get(f),
					new QuestActiveCondition(QUEST_SLOT),
					ConversationStates.ATTENDING,
					responses.get(f),
					null);
		}
	}

	private void prepareRequestingStep() {

		// Player requests quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new NotCondition(new QuestActiveCondition(QUEST_SLOT)),
						new TimePassedCondition(QUEST_SLOT, 1, WAIT_TIME)),
				ConversationStates.QUEST_OFFERED,
				"花店的花供应严重不足. 你可以帮我进点货吗？",
				null);

		// Player requests quest after started
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"你还没带来工要的 #花",
				null);

		// Player requests quest before wait period ended
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WAIT_TIME)),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, WAIT_TIME, "这么快你就把花带来了. 或许我还需要你的帮助"));

		// Player accepts quest
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
						new StartItemsCollectionWithLimitAction(QUEST_SLOT, 0, flowerTypes, MAX_FLOWERS),
						new AddItemToCollectionAction(QUEST_SLOT, "水", REQ_WATER),
						new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "太好了, 我需要 [items]."))
		);

		// Player rejects quest
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"听到这些真的很难过. ",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}


	private void prepareBringingStep() {
		List<String> requestedItems = new ArrayList<String>();
		for (String f : flowerTypes) {
			requestedItems.add(f);
		}
		requestedItems.add("水");

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new IncreaseXPAction(1000));
		reward.add(new IncreaseKarmaAction(25.0));
		reward.add(new EquipItemAction("纳尔沃城回城卷", 5));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		reward.add(new SayTextAction("非常感谢! 现在我要开始工作了, 这些纳尔沃回卷就算是给你的谢礼."));

		ChatAction rewardAction = new MultipleActions(reward);

		/* add triggers for the item names */
		for (String item : requestedItems) {
			npc.add(ConversationStates.QUESTION_1,
					item,
					new QuestActiveCondition(QUEST_SLOT),
					ConversationStates.QUESTION_1,
					null,
					new CollectRequestedItemsAction(
							item,
							QUEST_SLOT,
							"谢谢你！你还带了别的吗？",
							"我不再需要了. ",
							rewardAction,
							ConversationStates.IDLE
							));
		}

		// NPC asks if player brought items
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				"你给店里带了什么 #东西 吗",
				null);

		// Player confirms brought flowers
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				"你带的是什么？",
				null);

		// Player didn't bring flowers
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"别再一直闻那些玫瑰了, 要开始忙了. 我 #记得 你还要给我带东西过来. ",
				null);

		// Player offers item that wasn't requested
		npc.add(ConversationStates.QUESTION_1,
				"",
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				"我不觉得这东西东西放店里会好看. ",
				null);

		// Player says "bye" or "no" while listing flowers
		List<String> endDiscussionPhrases = new ArrayList<String>();
		for (String phrase : ConversationPhrases.NO_MESSAGES) {
			endDiscussionPhrases.add(phrase);
		}
		for (String phrase : ConversationPhrases.GOODBYE_MESSAGES) {
			endDiscussionPhrases.add(phrase);
		}
		npc.add(ConversationStates.QUESTION_1,
				endDiscussionPhrases,
				null,
				ConversationStates.IDLE,
				"你找到那些花时再回来. ",
				null);
	}


	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "RestockFlowerShop";
	}

	public String getTitle() {
		return "给花店备货";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getRegion() {
		return Region.NALWOR_CITY;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				getTitle(),
				getNPCName() + " 需要为 Nalwor 城的花店备好货. ",
				true);
		setupBasicResponses();
		setupActiveQuestResponses();
		prepareRequestingStep();
		prepareBringingStep();
	}
}
