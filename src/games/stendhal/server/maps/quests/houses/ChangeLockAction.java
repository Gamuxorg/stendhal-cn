/**
 *
 */
package games.stendhal.server.maps.quests.houses;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.HouseKey;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.portal.HousePortal;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

final class ChangeLockAction extends HouseChatAction implements ChatAction {
	private static final Logger logger = Logger.getLogger(ChangeLockAction.class);

	protected ChangeLockAction(final String questslot) {
		super(questslot);
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		if (player.isEquipped("money", HouseChatAction.COST_OF_SPARE_KEY)) {
			// we need to find out which this houseportal is so we can change lock
			final String claimedHouse = player.getQuest(questslot);

			try {
				final int id = Integer.parseInt(claimedHouse);
				final HousePortal portal = HouseUtilities.getHousePortal(id);
				// change the lock
				portal.changeLock();
				// make a new key for the player, with the new locknumber
				final String doorId = portal.getDoorId();
				final Item key = SingletonRepository.getEntityManager().getItem("房间钥匙");
				final int locknumber = portal.getLockNumber();

				((HouseKey) key).setup(doorId, locknumber, player.getName());
				if (player.equipToInventoryOnly(key)) {
					player.drop("money", HouseChatAction.COST_OF_SPARE_KEY);
					raiser.say("号码为 " + doorId + " 的房子的锁已更换, 这把新钥匙给你. 如果还需要备用钥匙, 请付 "
							   + HouseChatAction.COST_OF_SPARE_KEY + " 钱?");
					raiser.setCurrentState(ConversationStates.QUESTION_1);
				} else {
					// if the player doesn't have the space for the key, change the locks anyway as a security measure, but don't charge.
					raiser.say("号码为 "
							   + doorId + " 的房子的锁已更换, 但你没有地方带走这把钥匙了. 我已留置了对你的服务. "
							   + "如果你身上留出了空间再回来, 那时我会把备用钥匙给你. 再见.");
					raiser.setCurrentState(ConversationStates.IDLE);
				}
			} catch (final NumberFormatException e) {
				logger.error("Invalid number in house slot", e);
				raiser.say("抱歉, 出现点状况. 真不好意思.");
				return;
			}
		} else {
			raiser.say("你需要支付 " + HouseChatAction.COST_OF_SPARE_KEY + " 换锁钱, 并且配一把新钥匙.");
		}
	}
}
