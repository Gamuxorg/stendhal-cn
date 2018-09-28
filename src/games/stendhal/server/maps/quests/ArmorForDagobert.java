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
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Armor for Dagobert
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Dagobert, the consultant at the bank of 塞门镇</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Dagobert asks you to find a 皮胸甲.</li>
 * <li>You get a 皮胸甲, e.g. by killing a 独眼巨人.</li>
 * <li>Dagobert sees your 皮胸甲 and asks for it and then thanks you.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>50 XP</li>
 * <li>80 gold</li>
 * <li>Karma: 10</li>
 * <li>Access to vault</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class ArmorForDagobert extends AbstractQuest {

	private static final String QUEST_SLOT = "armor_dagobert";



	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("我见到了 Dagobert. 他是 塞门镇 银行的顾问. ");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("他让我找一件 皮胸甲 , 但我拒绝了他的请求. ");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("我答应给他找一件 皮胸甲, 因为他被抢劫过. ");
		}
		if ("start".equals(questState) && (player.isEquipped("皮胸甲") || player.isEquipped("护肩皮胸甲")) || "done".equals(questState)) {
			res.add("我找到了一件 皮胸甲 , 然后会把它带给 Dagobert.");
		}
		if ("done".equals(questState)) {
			res.add("我把 皮胸甲 带给 Dagobert. 作为答谢, 他允许我使用私人仓库. ");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Dagobert");

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"我太怕被抢了. 我没有任何保护, 你可以帮帮我吗?",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"感觉感谢你能带来盔甲, 暂时没有其他需要你帮忙的地方了.",
			null);

		// player is willing to help
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"之前我有一件完好的 #'皮胸甲', 但因最近的一次抢劫事件被弄坏了, 如果你能拿个新的给我, 我一定会报答你. ",
			new SetQuestAction(QUEST_SLOT, "start"));

		// player is not willing to help
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"好吧, 我猜以后我只能靠躲了. ",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// player wants to know what a 皮胸甲 is
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("皮胸甲", "leather", "cuirass"),
			null,
			ConversationStates.ATTENDING,
			"皮胸甲 是独眼巨人的常备护甲. 一些独眼巨人生活在城市下的地窂深处. ",
			null);
	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Dagobert");

		// player returns while quest is still active
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				new OrCondition(
					new PlayerHasItemWithHimCondition("皮胸甲"),
					new PlayerHasItemWithHimCondition("护肩皮胸甲"))),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"打扰一下, 我看到你拿了一件 皮胸甲 , 是给我的吗?",
			null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				new NotCondition(new OrCondition(
					new PlayerHasItemWithHimCondition("皮胸甲"),
					new PlayerHasItemWithHimCondition("护肩皮胸甲")))),
			ConversationStates.ATTENDING,
			"太幸运了, 你走后我还没被抢过. 很高兴收到你的 皮胸甲. 还有, 我能 #帮助 你做点什么？",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new EquipItemAction("money", 80));
		reward.add(new IncreaseXPAction(50));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(10));

		final List<ChatAction> reward1 = new LinkedList<ChatAction>(reward);
		reward1.add(new DropItemAction("皮胸甲"));

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			// make sure the player isn't cheating by putting the armor
			// away and then saying "yes"
			new PlayerHasItemWithHimCondition("皮胸甲"),
			ConversationStates.ATTENDING, "Oh, 太感谢了！这里有一些金币, ...嗯...另外, 现在你已被认可为受信客户, 可以随时开通自已的私人 #金库 了.",
			new MultipleActions(reward1));

		final List<ChatAction> reward2 = new LinkedList<ChatAction>(reward);
		reward2.add(new DropItemAction("护肩皮胸甲"));
		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			// make sure the player isn't cheating by putting the armor
			// away and then saying "yes"
			new AndCondition(
				new NotCondition(new PlayerHasItemWithHimCondition("皮胸甲")),
				new PlayerHasItemWithHimCondition("护肩皮胸甲")),
			ConversationStates.ATTENDING, "Oh, 太感谢了！这里有一些金币, ...嗯...另外, 现在你已被认可为受信客户, 可以随时开通自已的私人 #金库 了.",
			new MultipleActions(reward2));

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"好吧, 希望我下次被抢劫之前, 你能再找一件多的给我.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Dagobert的皮胸甲",
				"Dagobert, 塞门镇的银行顾问, 需要安全保护.",
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
		return "ArmorForDagobert";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Dagobert";
	}
}
