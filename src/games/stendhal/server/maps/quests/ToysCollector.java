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
import java.util.List;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuest;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuestLogic;

/**
 * QUEST: Toys Collector
 *
 * PARTICIPANTS: <ul>
 * <li> Anna, a girl who live in Ados </ul>
 *
 * STEPS:
 * <ul><li> Anna asks for some toys
 * <li> You guess she might like a teddy, dice or dress
 * <li> You bring the toy to Anna
 * <li> Repeat until Anna received all toys. (Of course you can bring several
 * toys at the same time.)
 * <li> Anna gives you a reward
 * </ul>
 * REWARD:<ul>
 * <li> 3 pies
 * <li> 100 XP
 * <li> 10 Karma
 * </ul>
 * REPETITIONS: <ul><li> None.</ul>
 */
public class ToysCollector extends AbstractQuest implements
		BringListOfItemsQuest {

	private static final String QUEST_SLOT = "toys_collector";

	private static final List<String> neededToys =
		Arrays.asList("teddy", "dice", "dress");

	// don't want to use the standard history for this kind of quest for anna as we dont want to say what she needs.
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			if (!"done".equals(questState)) {
				res.add("Anna 想要一些玩具, 我需要怎么能哄那个小女孩开心!");
			} else {
				res.add("我拿到一个有趣的玩具给 Anna, Jens 和 George 玩.");
			}
			return res;
	}

	private void setupAbstractQuest() {
		final BringListOfItemsQuest concreteQuest = this;
		BringListOfItemsQuestLogic bringItems = new BringListOfItemsQuestLogic(concreteQuest);
		bringItems.addToWorld();
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Toys Collector",
				"Anna 很无聊, 找一些玩具和她一起玩.",
				false);
		setupAbstractQuest();
		specialStuff();
	}

	private void specialStuff() {
		getNPC().add(
				ConversationStates.ATTENDING,
				ConversationPhrases.NO_MESSAGES,
				new QuestNotCompletedCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"你应该在我和你说话引来麻烦之前离开这里. 再见.",
				null);
	}

	@Override
	public SpeakerNPC getNPC() {
		return npcs.get("Anna");
	}

	@Override
	public List<String> getNeededItems() {
		return neededToys;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getTriggerPhraseToEnumerateMissingItems() {
		return ConversationPhrases.EMPTY;
	}

	@Override
	public List<String> getAdditionalTriggerPhraseForQuest() {
		return Arrays.asList("toys");
	}

	@Override
	public double getKarmaDiffForQuestResponse() {
		return 8.0;
	}

	@Override
	public String welcomeBeforeStartingQuest() {
		return "妈妈说, 我们不许和陌生人说话, 但我很无聊, 我想要玩具 #toys!";
	}

	@Override
	public String welcomeDuringActiveQuest() {
		return "Hello! 我还是无聊, 你把玩具带来了吗?";
	}

	@Override
	public String welcomeAfterQuestIsCompleted() {
		return "Hi! 我正和我的玩具玩, 没有大人的份";
	}

	@Override
	public boolean shouldWelcomeAfterQuestIsCompleted() {
		return true;
	}

	@Override
	public String respondToQuest() {
		return "我不确定什么玩具, 但不管什么好玩的, 你都可以带给我,好吗";
	}

	@Override
	public String respondToQuestAfterItHasAlreadyBeenCompleted() {
		return "这个玩具太好了, 谢谢";
	}

	@Override
	public String respondToQuestAcception() {
		return "Hooray! 太激动了. 再见";
	}

	@Override
	public String respondToQuestRefusal() {
		return "Oh ... 你真卑鄙.";
	}

	// not used
	@Override
	public String firstAskForMissingItems(final List<String> missingItems) {
		return "我不确定什么玩具, 但不管什么好玩的东西, 都请你把它带给我, 好吗？";
	}

	@Override
	public String askForMissingItems(final List<String> missingItems) {
		return "你带来的是什么玩具?";
	}

	@Override
	public String respondToPlayerSayingHeHasNoItems(final List<String> missingItems) {
		return "Okay 一会回来..";
	}

	@Override
	public String askForItemsAfterPlayerSaidHeHasItems() {
		return "你带来的是什么玩具?";
	}

	@Override
	public String respondToItemBrought() {
		return "谢谢! 你还带了别的玩具吗?";
	}

	@Override
	public String respondToLastItemBrought() {
		return "这些玩具让我很开心, 请拿这个 pies . Arlindo 给我们烤的, 但我觉得你应该有了.";
	}

	@Override
	public void rewardPlayer(final Player player) {
		final StackableItem pie = (StackableItem) SingletonRepository.getEntityManager().getItem(
				"馅饼");
		pie.setQuantity(3);
		player.equipOrPutOnGround(pie);
		player.addXP(100);
		player.addKarma(10.0);
	}

	@Override
	public String respondToOfferOfNotExistingItem(final String itemName) {
		return "Hey! 说谎不好! 你根本没有 "
				+ itemName + " . ";
	}

	@Override
	public String respondToOfferOfNotMissingItem() {
		return "我已经有了一个!";
	}

	@Override
	public String respondToOfferOfNotNeededItem() {
		return "这不是好玩具!";
	}

	@Override
	public String getName() {
		return "ToysCollector";
	}

	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Anna";
	}
}
