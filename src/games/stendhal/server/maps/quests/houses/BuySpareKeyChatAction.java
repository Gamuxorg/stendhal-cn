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

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.HouseKey;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.portal.HousePortal;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/** The sale of a spare key has been agreed, player meets conditions,
 * here is the action to simply sell it. */
final class BuySpareKeyChatAction extends HouseChatAction implements ChatAction {


	protected BuySpareKeyChatAction(final String questslot) {
		super(questslot);
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		if (player.isEquipped("money", HouseChatAction.COST_OF_SPARE_KEY)) {

			final String housenumber = player.getQuest(questslot);
			final Item key = SingletonRepository.getEntityManager().getItem(
																			"房间钥匙");
			final int number = MathHelper.parseInt(housenumber);
			final HousePortal houseportal = HouseUtilities.getHousePortal(number);

			if (houseportal == null) {
				// something bad happened
				raiser.say("抱歉, 出现点状况. 真不好意思.");
				return;
			}

			final int locknumber = houseportal.getLockNumber();
			final String doorId = houseportal.getDoorId();
			((HouseKey) key).setup(doorId, locknumber, player.getName());

			if (player.equipToInventoryOnly(key)) {
				player.drop("money", HouseChatAction.COST_OF_SPARE_KEY);
				raiser.say("给你备用钥匙, 但要记住, 只能把备用钥匙送给你 #非常, #非常, 信任的人! 任何取得钥匙的人都可以打开储藏箱. 要告诉任何拿到钥匙的人, 如果钥匙丢了要告诉你. 如真的丢了钥匙, 你应该马上 #换锁.");
			} else {
				raiser.say("抱歉, 你身上不能带更多的钥匙!");
			}
		} else {
			raiser.say("你没有足够的钱配钥匙!");
		}
	}
}
