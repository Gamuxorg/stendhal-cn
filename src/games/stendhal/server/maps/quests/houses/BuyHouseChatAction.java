
package games.stendhal.server.maps.quests.houses;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.HouseKey;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.mapstuff.chest.StoredChest;
import games.stendhal.server.entity.mapstuff.portal.HousePortal;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.SlotIsFullException;

final class BuyHouseChatAction extends HouseChatAction implements ChatAction {


	private int cost;

	/**
	 * Creates a new BuyHouseChatAction.
	 *
	 * @param cost how much does the house cost
	 * @param questSlot name of quest slot
	 */
	BuyHouseChatAction(final int cost, final String questSlot) {
		super(questSlot);
		this.cost = cost;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

		final int number = sentence.getNumeral().getAmount();
		// now check if the house they said is free
		final String itemName = Integer.toString(number);

		final HousePortal houseportal = HouseUtilities.getHousePortal(number);

		if (houseportal == null) {
			// something bad happened
			raiser.say("抱歉, 我没明白你的意思, 能再说一次房子号码吗？");
			raiser.setCurrentState(ConversationStates.QUEST_OFFERED);
			return;
		}

		final String owner = houseportal.getOwner();
		if (owner.length() == 0) {

			// it's available, so take money
			if (player.isEquipped("money", cost)) {
				final Item key = SingletonRepository.getEntityManager().getItem(
				"房间钥匙");

				final String doorId = houseportal.getDoorId();

				final int locknumber = houseportal.getLockNumber();
				((HouseKey) key).setup(doorId, locknumber, player.getName());

				if (player.equipToInventoryOnly(key)) {
					raiser.say("祝贺, 我是你的 " + doorId
							   + " 号房子的钥匙! 如果你把钥匙弄丢了一定要换锁. 如果你想要一把备用钥匙, 请付 "
							   + HouseChatAction.COST_OF_SPARE_KEY + " 钱?");

					player.drop("money", cost);
					// remember what house they own
					player.setQuest(questslot, itemName);

					// put nice things and a helpful note in the chest
					BuyHouseChatAction.fillChest(HouseUtilities.findChest(houseportal), houseportal.getDoorId());

					// set the time so that the taxman can start harassing the player
					final long time = System.currentTimeMillis();
					houseportal.setExpireTime(time);

					houseportal.setOwner(player.getName());
					raiser.setCurrentState(ConversationStates.QUESTION_1);
				} else {
					raiser.say("抱歉, 你不能带更多钥匙了!");
				}

			} else {
				raiser.say("你的钱不够!");
			}

		} else {
			raiser.say("抱歉, " + itemName
					   + " 号房间已售, 请询问 #未售 的房子价目表, 或者选择另一个房间号码.");
			raiser.setCurrentState(ConversationStates.QUEST_OFFERED);
		}
	}

	private static void fillChest(final StoredChest chest, String id) {
		Item item = SingletonRepository.getEntityManager().getItem("笔记");
		item.setDescription("房主需知：\n"
				+ "1. 如果你没有支付房产税, 你的房子和储物箱中的东西将全部被没收.\n"
				+ "2. 可以进入房子的所有人都能使用储物箱.\n"
				+ "3. 如果你的房屋安全出现问题, 请尽快换锁.\n"
				+ "4. 你可以转卖房子 (请不要离开我)\n");
		try {
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("红酒");
			((StackableItem) item).setQuantity(2);
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("巧克力棒");
			((StackableItem) item).setQuantity(2);
			chest.add(item);
		} catch (SlotIsFullException e) {
			Logger.getLogger(BuyHouseChatAction.class).info("Could not add " + item.getName() + " to chest in " + id, e);
		}
	}
}
