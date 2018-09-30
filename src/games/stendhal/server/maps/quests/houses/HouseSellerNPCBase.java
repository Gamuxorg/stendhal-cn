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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Base class for dialogue shared by all houseseller NPCs.
 *
 */
abstract class HouseSellerNPCBase extends SpeakerNPC {

	static final String QUEST_SLOT = "house";
	/**
	 * age required to buy a house. Note, age is in minutes, not seconds! So
	 * this is 300 hours.
	 */
	static final int REQUIRED_AGE = 300 * 60;
	/** percentage of initial cost refunded when you resell a house.*/
	private static final int DEPRECIATION_PERCENTAGE = 40;

	private final String location;

	private final HouseTax houseTax;
	/**
	 *	Creates NPC dialog for house sellers.
	 * @param name
	 *            the name of the NPC
	 * @param location
	 *            where are the houses?
	 * @param houseTax
	 * 		      class which controls house tax, and confiscation of houses
	*/
	HouseSellerNPCBase(final String name, final String location, final HouseTax houseTax) {
		super(name);
		this.location = location;
		this.houseTax =  houseTax;
		createDialogNowWeKnowLocation();
	}

	@Override
	protected abstract void createPath();

	private void createDialogNowWeKnowLocation() {
		addGreeting(null, new HouseSellerGreetingAction(QUEST_SLOT));

			// quest slot 'house' is started so player owns a house
		add(ConversationStates.ATTENDING,
			Arrays.asList("房价", "买房", "购房", "buy"),
			new PlayerOwnsHouseCondition(),
			ConversationStates.ATTENDING,
			"如你所知, 买房需要花 "
				+ getCost()
			+ " 钱. 但你只能买一所房子, 因为市场需求太大! 你只能把旧房 #转卖 掉然后才能买新房.",
			null);

		// we need to warn people who buy spare keys about the house
		// being accessible to other players with a key
		add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.QUESTION_2,
			"继续之前, 我必须提醒你, 任何有钥匙的人都能进入你的房间, 并且可以使用储物箱的东西. 你仍要配一把备用钥匙吗?",
			null);

		// player wants spare keys and is OK with house being accessible
		// to other person.
		add(ConversationStates.QUESTION_2,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new BuySpareKeyChatAction(QUEST_SLOT));

		// refused offer to buy spare key for security reasons
		add(ConversationStates.QUESTION_2,
			ConversationPhrases.NO_MESSAGES,
			null,
				ConversationStates.ATTENDING,
			"再次提醒你. 你要确保可以信任的人才能进入你的房子.",
			null);

		// refused offer to buy spare key
		add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"没问题! 如果所知, 如果需要 #换锁, 我都能做到, 你也可以 #转卖 房产给我.",
			null);

		// player is eligible to resell a house
		add(ConversationStates.ATTENDING,
			Arrays.asList("转卖", "售房", "卖房"),
			new PlayerOwnsHouseCondition(),
				ConversationStates.QUESTION_3,
			"我会以百分之 "
			+ Integer.toString(DEPRECIATION_PERCENTAGE)
			+ " 的你的购房时的价格, 再减去你之前欠下的税款之后的价格付款给你. 你要记得售房前带走你房间里的私人物品. 确定要卖掉你的房产?",
			null);

		// player is not eligible to resell a house
		add(ConversationStates.ATTENDING,
			Arrays.asList("转卖", "售房", "卖房"),
			new NotCondition(new PlayerOwnsHouseCondition()),
			ConversationStates.ATTENDING,
			"现在你没有房产了. 如果想买个新房可以寻问 #房价.",
			null);

		add(ConversationStates.QUESTION_3,
			ConversationPhrases.YES_MESSAGES,
			null,
				ConversationStates.ATTENDING,
			null,
			new ResellHouseAction(getCost(), QUEST_SLOT, DEPRECIATION_PERCENTAGE, houseTax));

		// refused offer to resell a house
		add(ConversationStates.QUESTION_3,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"好吧, 很高兴你能改变主意.",
			null);

		// player is eligible to change locks
		add(ConversationStates.ATTENDING,
			"换锁",
			new PlayerOwnsHouseCondition(),
			ConversationStates.SERVICE_OFFERED,
			"如果你担心房子的安全, 或者不再想信取得你钥匙的人, "
			+ "希望给房子换一把新锁. 现在确定要换吗?",
			null);

		// player is not eligible to change locks
		add(ConversationStates.ATTENDING,
			"换锁",
			new NotCondition(new PlayerOwnsHouseCondition()),
			ConversationStates.ATTENDING,
			"你还没有房产. 如果你要买房请寻问 #房价 .",
			null);

		// accepted offer to change locks
		add(ConversationStates.SERVICE_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new ChangeLockAction(QUEST_SLOT));

		// refused offer to change locks
		add(ConversationStates.SERVICE_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"OK, 如果你确定就告诉我.",
			null);

		add(ConversationStates.ANY,
			Arrays.asList("可售", "在售", "未售"),
			null,
			ConversationStates.ATTENDING,
			null,
			new ListUnboughtHousesAction(location));

		addReply(
				 "买房",
				 "买房之前最好先询问 #房价 . 可以看看售房目录在, #https://stendhalgame.org/wiki/StendhalHouses.");
		addReply("非常",
				 "一定要, 非常, 非常, 非常. 非常.");
		addOffer("我是售楼人员, 请在 #https://stendhalgame.org/wiki/StendhalHouses 看看房内的布置. 选定后再向我询问 #房价.");
		addHelp("如果有 #可售 房源, 你有资格购买. 如果你按 #房价 支付完, 我会把房间钥匙给你. 作为房主你可以再做一把备用钥匙分享给你的朋友. 请先到 #https://stendhalgame.org/wiki/StendhalHouses 查看房内布置.");
		addQuest("可以向我买房, 如果你有兴趣可以询问 #房价, 要选房可以看看 #https://stendhalgame.org/wiki/StendhalHouses.");
		addGoodbye("再见.");
	}

	protected abstract int getCost();

	protected abstract int getLowestHouseNumber();
	protected abstract int getHighestHouseNumber();
}
final class PlayerOwnsHouseCondition implements ChatCondition {
	@Override
	public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
		return HouseUtilities.playerOwnsHouse(player);
	}
}
