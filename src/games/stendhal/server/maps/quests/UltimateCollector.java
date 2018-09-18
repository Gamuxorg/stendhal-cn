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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;


/**
 * QUEST: Ultimate Collector
 * <p>
 * PARTICIPANTS: <ul><li> Balduin  </ul>
 *
 * STEPS:
 * <ul><li> Balduin challenges you to be the ultimate weapons collector
 *     <li> Balduin asks you to complete each quest where you win a rare item
 *	   <li> Balduin asks you to bring him one extra rare item from a list
 *</ul>
 *
 * REWARD: <ul>
 * <li> You can sell black items to Balduin
 * <li> 100000 XP
 * <li> 90 karma
 * </ul>
 *
 * REPETITIONS: <ul><li> None. </ul>
 */
public class UltimateCollector extends AbstractQuest {

	/** Quest slot for this quest, the Ultimate Collector */
	private static final String QUEST_SLOT = "ultimate_collector";

	/** 类牙棍 in Kotoch: The Orc Saman is the NPC */
	private static final String CLUB_THORNS_QUEST_SLOT = "club_thorns"; // kotoch

	/** Vampire Sword quest: Hogart is the NPC */
	private static final String VAMPIRE_SWORD_QUEST_SLOT = "vs_quest"; // dwarf blacksmith

	/** Obsidian Knife quest: Alrak is the NPC */
	private static final String OBSIDIAN_KNIFE_QUEST_SLOT = "obsidian_knife"; // dwarf blacksmith

	/** Immortal Sword Quest in Kotoch: Vulcanus is the NPC */
	private static final String IMMORTAL_SWORD_QUEST_SLOT = "immortalsword_quest"; // kotoch

	/** 密银斗篷 quest: Ida is the NPC */
	private static final String MITHRIL_CLOAK_QUEST_SLOT = "mithril_cloak"; // mithril

	/** 密银盾 quest: Baldemar is the NPC */
	private static final String MITHRIL_SHIELD_QUEST_SLOT = "mithrilshield_quest"; // mithril

	/** Cloak Collector 2nd quest: Josephine is the NPC (Completing 2nd requires 1st) */
	private static final String CLOAKSCOLLECTOR2_QUEST_SLOT = "cloaks_collector_2"; // cloaks

	/** Cloaks For Bario (Freezing Dwarf) quest: Bario is the NPC  */
	private static final String CLOAKS_FOR_BARIO_QUEST_SLOT = "cloaks_for_bario"; // cloaks

	// private static final String HELP_TOMI_QUEST_SLOT = "help_tomi"; don't require

	/** 精灵护甲 quest: Lupos is the NPC */
	private static final String ELVISH_ARMOR_QUEST_SLOT = "elvish_armor"; // specific for this one

	/** Kanmararn Soldiers quest: Henry is the NPC  */
	private static final String KANMARARN_QUEST_SLOT = "soldier_henry"; // specific for this one

	/** Weapons Collector 2nd quest: Balduin is the NPC (Completing 2nd requires 1st) */
	private static final String WEAPONSCOLLECTOR2_QUEST_SLOT = "weapons_collector2";


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
		res.add("Balduin 想要我帮他找到最新的终极武器.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("现在我不想给他任何武器");
			return res;
		}
		res.add("我接受了他的终极武器收集请求, 答应他找到一个特号而且罕见的武器.");
		if (!isCompleted(player)) {
			res.add("Balduin 让我带给他 " + player.getRequiredItemName(QUEST_SLOT,0) + ".");
		}
		if (isCompleted(player)) {
			res.add("Yay! 我乃终极武器收藏家, 我卖各种黑武器给 Balduin!");
		}
		return res;
	}

	private void checkCollectingQuests() {
		final SpeakerNPC npc = npcs.get("Balduin");


		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"老朋友你好, 我给你准备了我的另一个藏品.",
			null);

		npc.add(ConversationStates.ATTENDING,
			"challenge",
			new AndCondition(new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new OrCondition(new QuestNotCompletedCondition(CLUB_THORNS_QUEST_SLOT),
							 new QuestNotCompletedCondition(IMMORTAL_SWORD_QUEST_SLOT))),
			ConversationStates.ATTENDING,
			"在 Kotoch 我还有一个请求你没有完成. 要深入探究, 你才能真正变成武器收藏家. !",
			null);


		npc.add(ConversationStates.ATTENDING,
			"challenge",
			new AndCondition(new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new OrCondition(new QuestNotCompletedCondition(MITHRIL_CLOAK_QUEST_SLOT),
							 new QuestNotCompletedCondition(MITHRIL_SHIELD_QUEST_SLOT))),
			ConversationStates.ATTENDING,
			"你错过了一个特别的密银装备, 如果你帮助了正确的人就能赢得. 没有这个武器你就不能成为收藏家. .",
			null);

		npc.add(ConversationStates.ATTENDING,
			"challenge",
			new AndCondition(new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new OrCondition(new QuestNotCompletedCondition(OBSIDIAN_KNIFE_QUEST_SLOT),
							 new QuestNotCompletedCondition(VAMPIRE_SWORD_QUEST_SLOT))),
			ConversationStates.ATTENDING,
			"在侏儒铁匠铺里的深深的地下室里独居着一个能给你打造特殊武器的人, 没有这把武器你就不能成为武器收藏家. ",
			null);

		npc.add(ConversationStates.ATTENDING,
			"challenge",
			new AndCondition(new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new OrCondition(new QuestNotCompletedCondition(CLOAKSCOLLECTOR2_QUEST_SLOT),
							 new QuestNotCompletedCondition(CLOAKS_FOR_BARIO_QUEST_SLOT))),
			ConversationStates.ATTENDING,
			"如果你收集了很多斗篷, 那这把特别的武器就是你的. 不论是为了你的虚荣心, 或者为了你的热情, 都是你必须完成的任务. ",
			null);

		npc.add(ConversationStates.ATTENDING,
			"challenge",
			new AndCondition(new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new QuestNotCompletedCondition(ELVISH_ARMOR_QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"另一个收藏家也需要你的帮助, 你需去 Fado 森林中到到他, 因为不找到他要的东西, 就不能成为开武器收藏家. ",
			null);

		npc.add(ConversationStates.ATTENDING,
			"challenge",
			new AndCondition(new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new QuestNotCompletedCondition(KANMARARN_QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"你已收集了很多特殊装备, 但你还从没有帮助过 Kanmararn 地下的这些人, 你应该完成他们的要求.",
			null);

	}

	private void requestItem() {

		final SpeakerNPC npc = npcs.get("Balduin");
		final Map<String,Integer> items = new HashMap<String, Integer>();

		// the numbers are based on depo's metric for rarity (bigger number = more rare) which may be out of date https://sourceforge.net/tracker/?func=detail&aid=2066597&group_id=1111&atid=973767
		// nothing rarer than a 赤魔剑, and not included items which are quest rewards elsewhere
		items.put("nihonto",1); // 5169
		items.put("魔法双刃斧",1); // 1010
		items.put("imperator sword",1); // 2393
		items.put("都灵之斧",1); // 4331
		items.put("vulcano hammer",1); // 4474
		items.put("xeno sword",1); // 1347
		items.put("黑镰刀",1); // 3918 (pretty sure this is rarer now but a lot of old ones about to buy)
		items.put("混沌匕首",1); // 1691
		items.put("black sword",1); // 6285

		// If all quests are completed, ask for an item
		npc.add(ConversationStates.ATTENDING,
				"challenge",
				new AndCondition(
						new QuestCompletedCondition(WEAPONSCOLLECTOR2_QUEST_SLOT),
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(KANMARARN_QUEST_SLOT),
						new QuestCompletedCondition(ELVISH_ARMOR_QUEST_SLOT),
						new QuestCompletedCondition(CLOAKSCOLLECTOR2_QUEST_SLOT),
						new QuestCompletedCondition(CLOAKS_FOR_BARIO_QUEST_SLOT),
						new QuestCompletedCondition(OBSIDIAN_KNIFE_QUEST_SLOT),
						new QuestCompletedCondition(VAMPIRE_SWORD_QUEST_SLOT),
						new QuestCompletedCondition(MITHRIL_CLOAK_QUEST_SLOT),
						new QuestCompletedCondition(MITHRIL_SHIELD_QUEST_SLOT),
						new QuestCompletedCondition(CLUB_THORNS_QUEST_SLOT),
						new QuestCompletedCondition(IMMORTAL_SWORD_QUEST_SLOT)),
				ConversationStates.ATTENDING,
				null,
				new StartRecordingRandomItemCollectionAction(QUEST_SLOT, items, "好吧, 你已经向Faiumoni 的居民证明了自已, 这样你就成成为武器收藏家, " +
						"但我还有最后一个任务给你, 请带给我  [item]."));
	}

	private void collectItem() {

		final SpeakerNPC npc = npcs.get("Balduin");

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUEST_ITEM_QUESTION,
				"你把我要的稀有货带来了吗?",
				null);

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT))),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT, "Hm, 不, 你没有 [item], 不要愚弄我!"));

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Wow, 这东西能在我眼前简直难以至信, 哇, 或许我们可以一起处理 #deal .",
				new MultipleActions(new DropRecordedItemAction(QUEST_SLOT),
									new SetQuestAction(QUEST_SLOT, "done"),
									new IncreaseXPAction(100000),
									new IncreaseKarmaAction(90)));

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT, "很好, 当你有了这东西后再回来 [the item] ."));
	}

	private void offerSteps() {
  		final SpeakerNPC npc = npcs.get("Balduin");

		// player returns after finishing the quest and says offer
		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"我收购一些黑色装备, 但我只会付给你合适的价钱.",
				null);


		// player returns when the quest is in progress and says offer
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES,
				new QuestNotCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"我可以收购你的黑色装备, 要在你完成我给你的所有收集任务后. ", null);
	}


	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Ultimate Weapon Collector",
				"Balduin, 是一个集在 Ados 山上的隐士, 他要收集一个最终的装备才能成为收藏家. ",
				true);

		checkCollectingQuests();
		requestItem();
		collectItem();
		offerSteps();

	}

	@Override
	public String getName() {
		return "UltimateCollector";
	}

	// This is the max level of the min levels for the other quests
	@Override
	public int getMinLevel() {
		return 100;
	}

	@Override
	public String getNPCName() {
		return "Balduin";
	}

	@Override
	public String getRegion() {
		return Region.ADOS_SURROUNDS;
	}
}
