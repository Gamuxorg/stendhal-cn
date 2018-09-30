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

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.MathHelper;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.ChatMessage;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.db.PostmanDAO;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.portal.HousePortal;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.command.AbstractDBCommand;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;

/**
 * House tax, and confiscation of houses.
 */
class HouseTax implements TurnListener {
	/** The base amount of tax per month. */
	protected static final int BASE_TAX = 1000;

	private static final Logger logger = Logger.getLogger(HouseTax.class);
	/** How often the tax should be paid. Time in seconds. */
	private static final int TAX_PAYMENT_PERIOD = 30 * MathHelper.SECONDS_IN_ONE_DAY;
	/** How often the payments should be checked. Time in seconds */
	private static final int TAX_CHECKING_PERIOD = MathHelper.SECONDS_IN_ONE_DAY;
	/** How many tax payments can be unpaid. Any more and the house will be confiscated */
	private static final int MAX_UNPAID_TAXES = 5;


	/** Interest rate for unpaid taxes. The taxman does not give free loans. */
	private static final double INTEREST_RATE = 0.1;


	private long previouslyChecked = 0;

	public HouseTax() {
		setupTaxman();
		SingletonRepository.getTurnNotifier().notifyInSeconds(TAX_CHECKING_PERIOD, this);
	}

	/**
	 * Get the amount of unpaid taxes for a portal.
	 *
	 * @param portal the portal to be checked
	 * @return the amount of taxes to be paid
	 */
	protected int getTaxDebt(final HousePortal portal) {
		return getTaxDebt(getUnpaidTaxPeriods(portal));
	}

	/**
	 * Get the amount of money a player owes to the state.
	 *
	 * @param periods the number of months the player has to pay at once
	 * @return the amount of debt
	 */
	private int getTaxDebt(final int periods) {
		int debt = 0;

		for (int i = 0; i < periods; i++) {
			debt += BASE_TAX * Math.pow(1 + INTEREST_RATE, i);
		}
		logger.debug(debt + " was the debt for periods " + periods);
		return debt;
	}

	/**
	 * Get the number of tax periods the player has neglected to pay.
	 *
	 * @param player the player to be checked
	 * @return number of periods
	 */
	protected int getUnpaidTaxPeriods(final Player player) {
		final HousePortal portal = HouseUtilities.getPlayersHouse(player);
		int payments = 0;

		if (portal != null) {
			payments = getUnpaidTaxPeriods(portal);
		}
		return payments;
	}

	/**
	 * Get the number of tax periods for a given portal.
	 *
	 * @param portal the portal to be checked
	 * @return number of periods
	 */
	private int getUnpaidTaxPeriods(final HousePortal portal) {
		final int timeDiffSeconds = (int) ((System.currentTimeMillis() - portal.getExpireTime()) / 1000);
		return Math.max(0, timeDiffSeconds / TAX_PAYMENT_PERIOD);
	}

	private void setTaxesPaid(final Player player, final int periods) {
		final HousePortal portal = HouseUtilities.getPlayersHouse(player);
		logger.debug("set portal expire time to " + portal.getExpireTime() + " plus " + ((long) periods * (long) TAX_PAYMENT_PERIOD * 1000));
		portal.setExpireTime(portal.getExpireTime() +  ((long) periods * (long) TAX_PAYMENT_PERIOD * 1000));
	}

	@Override
	public void onTurnReached(final int turn) {
		SingletonRepository.getTurnNotifier().notifyInSeconds(TAX_CHECKING_PERIOD, this);

		final long time =  System.currentTimeMillis();

		/*
		 * Decide the time window for notifying the players
		 * We can not rely on the time to be TAX_CHECKING_PERIOD, since
		 * there could have been overflows. If the server has been restarted,
		 * all the doors get processed. MaybeStoreMessageCommand will weed out
		 * the spurious messages.
		 */
		final long timeSinceChecked = time - previouslyChecked;
		previouslyChecked = time;


		// Check all houses, and decide if they need any action
		for (final HousePortal portal : HouseUtilities.getHousePortals()) {
			final String owner = portal.getOwner();
			if (owner.length() > 0) {
				final int timeDiffSeconds = (int) ((time - portal.getExpireTime()) / 1000);
				final int payments = timeDiffSeconds / TAX_PAYMENT_PERIOD;

				if (payments > MAX_UNPAID_TAXES) {
					confiscate(portal);
				} else if ((payments > 0) && ((timeDiffSeconds % TAX_PAYMENT_PERIOD) < (timeSinceChecked / 1000))) {
					// a new tax payment period has passed since the last check. notify the player
					String remainder;
					if (payments == MAX_UNPAID_TAXES) {
						// Give a final warning if appropriate
						remainder = " 这是给你最后的通告, 如果你不在一个月内支付房产税, "
							+ "你的房子就会卖给其他人, 房间的锁也会更换. "
							+ "你将不能进入房间, 也不能取走储物箱的东西.";

					} else {
						remainder = " 快交钱, 趁我还对你的债务感兴趣. 如果你没有在 "
							+ Integer.toString(MAX_UNPAID_TAXES + 1) + " 月内交税, 你的房产将被没收.";
					}
					notifyIfNeeded(owner, "你还欠 " + payments + "个月共"
							+ Integer.toString(getTaxDebt(payments)) + " 钱的房产税 "
							
							+ ".可以去阿多斯市政厅交纳欠款." + remainder);
				}
			}
		}
	}

	/**
	 * Confiscate a house, and notify the owner.
	 *
	 * @param portal the door of the house to confiscate
	 */
	private void confiscate(final HousePortal portal) {
		notifyIfNeeded(portal.getOwner(), "你的欠税拖得太久. "
					   + "所以你的房产已被没收, 用以还欠.");
		logger.info("repossessed " + portal.getDoorId() + ", which used to belong to " + portal.getOwner());
		portal.changeLock();
		portal.setOwner("");
	}

	/**
	 * Notifies the owner of a house in the name of Tax Man.
	 *
	 * @param owner the player to be notified
	 * @param message the delivered message
	 */
	private void notifyIfNeeded(final String owner, final String message) {
		DBCommandQueue.get().enqueue(new MaybeStoreMessageCommand("税官", owner, message));
	}

	private void setupTaxman() {
		final SpeakerNPC taxman = SingletonRepository.getNPCList().get("税官");

		taxman.addReply("房产税", "所有的屋主必须为国家 #支付 房产税.");

		taxman.add(ConversationStates.ATTENDING,
				"支付",
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
						return getUnpaidTaxPeriods(player) > 0;
					}
		},
		ConversationStates.QUESTION_1,
		"现在要交税吗?",
		null);

		taxman.add(ConversationStates.ATTENDING,
				   Arrays.asList("支付", "交税"),
				   new ChatCondition() {
					   @Override
					public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
						   return getUnpaidTaxPeriods(player) <= 0;
					   }
				   },
				   ConversationStates.ATTENDING,
				   "记录中显示你没有欠税. 一旦房产所有人发生欠税, "
				   + "我会让信使马上通知你.",
				   null);

		taxman.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final int periods = getUnpaidTaxPeriods(player);
						final int cost = getTaxDebt(periods);
						if (player.isEquipped("money", cost)) {
							player.drop("money", cost);
							setTaxesPaid(player, periods);
							StringBuilder msg = new StringBuilder();
							msg.append("谢谢! 你已付清了 ");
							if (periods > 1) {
								msg.append(periods);
								msg.append(" 个月.");
							} else {
								msg.append("月.");
							}
							msg.append(cost);
							msg.append(" 钱的欠税. ");							
							npc.say(msg.toString());
						} else {
							npc.say("你的钱不够付清 "
									+ cost + " 钱的欠税, 别拖欠, 否则还会增加利息.");
						}
				}
			});

		taxman.add(ConversationStates.QUESTION_1,
				   ConversationPhrases.NO_MESSAGES,
				   null,
				   ConversationStates.ATTENDING,
				   "好吧, 但别拖太久, 否则还会增加利息.",
				   null);
	}

	/**
	 * Store a postman message if there is not an identical one already.
	 */
	private static class MaybeStoreMessageCommand extends AbstractDBCommand {
		private final String source;
		private final String target;
		private final String message;
		private String accountName;

		/**
		 * creates a new MaybeStoreMessageCommand
		 *
		 * @param source who left the message
		 * @param target the player name the message is for
		 * @param message what the message is
		 */
		public MaybeStoreMessageCommand(String source, String target, String message) {
			this.source = source;
			this.target = target;
			this.message = message;
		}

		@Override
		public void execute(DBTransaction transaction) throws SQLException {
			CharacterDAO characterdao = DAORegister.get().get(CharacterDAO.class);
			accountName = characterdao.getAccountName(transaction, target);
			// should not really happen with taxman, but check anyway
			if (accountName != null) {
				PostmanDAO postmandao = DAORegister.get().get(PostmanDAO.class);
				List<ChatMessage> oldMessages = postmandao.getChatMessages(transaction, target);
				for (ChatMessage msg : oldMessages) {
					if (msg.getSource().equals(source) && msg.getMessage().equals(message) && msg.getMessagetype().equals("N")) {
						/*
						 * If a player has already an equal undelivered message,
						 * don't send a new one. It is presumably from a
						 * previous check and the new one happened after a
						 * reboot.
						 */
						logger.debug("NOT sending a notice to '" + target + "': " + message);
						return;
					}
				}
				logger.info("sending a notice to '" + target + "': " + message);
				postmandao.storeMessage(source, target, message, "N");
			} else {
				logger.error("Not sending a Taxman notice to '" + target
						+ ", because the account does not exist. Message': " + message);
			}
		}
	}
}
