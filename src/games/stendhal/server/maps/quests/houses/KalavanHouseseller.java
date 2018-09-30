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
package games.stendhal.server.maps.quests.houses;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.parser.ExpressionType;
import games.stendhal.common.parser.JokerExprMatcher;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.condition.AgeGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TextHasNumberCondition;

final class KalavanHouseseller extends HouseSellerNPCBase {
	/** Cost to buy house in kalavan. */
	private static final int COST_KALAVAN = 100000;
	private static final String PRINCESS_QUEST_SLOT = "imperial_princess";

	KalavanHouseseller(final String name, final String location, final HouseTax houseTax) {
		super(name, location, houseTax);
		init();
	}

	private void init() {
		// Other than the condition that you must not already own a house, there are a number of conditions a player must satisfy.
		// For definiteness we will check these conditions in a set order.
		// So then the NPC doesn't have to choose which reason to reject the player for (appears as a WARN from engine if he has to choose)

		// player has not done required quest, hasn't got a house at all
add(ConversationStates.ATTENDING,
		Arrays.asList("购房", "房子", "买房", "房价"),
		new AndCondition(new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT), new QuestNotCompletedCondition(KalavanHouseseller.PRINCESS_QUEST_SLOT)),
		ConversationStates.ATTENDING,
			"购买房子需要花费 "
		+ getCost()
		+ " 钱. 但我还不能卖给你房子, 因为还要国王能证明你的公民资格, "
		+ "国王住在卡拉文城堡的北面. 可以先向国王的女儿说, 她...挺友善.",
			null);

// player is not old enough but they have doen princess quest
// (don't need to check if they have a house, they can't as they're not old enough)
add(ConversationStates.ATTENDING,
		Arrays.asList("购房", "房子", "买房", "房价"),
		new AndCondition(
							 new QuestCompletedCondition(KalavanHouseseller.PRINCESS_QUEST_SLOT),
							 new NotCondition(new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE))),
		ConversationStates.ATTENDING,
		"购买房子需要花费 "
		+ getCost()
		+ "钱. 但恐怕我还不能想信你能长期居住此地, 当你在Faiumoni游戏时长超过 "
		+ Integer.toString((HouseSellerNPCBase.REQUIRED_AGE / 60)) + " 小时后来来购买.",
		null);

// player is eligible to buy a house
		add(ConversationStates.ATTENDING,
		Arrays.asList("购房", "房子", "买房", "房价"),
		new AndCondition(new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT),
						 new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE),
							 new QuestCompletedCondition(KalavanHouseseller.PRINCESS_QUEST_SLOT)),
		ConversationStates.QUEST_OFFERED,
		"购买房子需要花费 "
		+ getCost()
		+ " 钱. 并且, 你还要每月支付房产税 " + HouseTax.BASE_TAX
		+ " 钱. 你可以问我还有多少房子 #在售. 或者如果你已选好房, 可以现在告诉我房屋号码.",
		null);

// handle house numbers 1 to 25
addMatching(ConversationStates.QUEST_OFFERED,
		// match for all numbers as trigger expression
		ExpressionType.NUMERAL, new JokerExprMatcher(),
		new TextHasNumberCondition(getLowestHouseNumber(), getHighestHouseNumber()),
		ConversationStates.ATTENDING,
		null,
		new BuyHouseChatAction(getCost(), QUEST_SLOT));

addJob("我是售房部经理. 我是房子销售经理. 简单说, 我卖房给本地的 #居民. 如果你有兴趣可以寻问 #房价. 我们的售楼部在. #https://stendhalgame.org/wiki/StendhalHouses.");
addReply("居民",
			 "卡拉文城堡皇室可以决定居民资格.");


setDescription("你遇见了一个看上去很精明的人.");
setEntityClass("estateagentnpc");
setPosition(55, 94);
initHP(100);

	}

	@Override
	protected int getCost() {
		return KalavanHouseseller.COST_KALAVAN;
	}

	@Override
	protected int getHighestHouseNumber() {
		return 25;
	}

	@Override
	protected int getLowestHouseNumber() {
		return 1;
	}

	@Override
	protected void createPath() {
		final List<Node> nodes = new LinkedList<Node>();
		nodes.add(new Node(55, 94));
		nodes.add(new Node(93, 94));
		nodes.add(new Node(93, 73));
		nodes.add(new Node(107, 73));
		nodes.add(new Node(107, 35));
		nodes.add(new Node(84, 35));
		nodes.add(new Node(84, 20));
		nodes.add(new Node(17, 20));
		nodes.add(new Node(17, 82));
		nodes.add(new Node(43, 82));
		nodes.add(new Node(43, 94));
		setPath(new FixedPath(nodes, true));
	}
}
