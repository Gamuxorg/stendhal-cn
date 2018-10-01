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
package games.stendhal.server.entity.npc.behaviour.adder;

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.HealerBehaviour;
import games.stendhal.server.entity.npc.behaviour.journal.ServicersRegister;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;

public class HealerAdder {

    private final ServicersRegister servicersRegister = SingletonRepository.getServicersRegister();

	/**
	 * Behaviour parse result in the current conversation.
	 * Remark: There is only one conversation between a player and the NPC at any time.
	 */
	private ItemParserResult currentBehavRes;

	/**
	 *<p>Makes this NPC a healer, i.e. someone who sets the player's hp to
	 * the value of their base hp.
	 *
	 *<p>Player killers are not healed at all even by healers who charge.
	 *
	 *<p>Too strong players (atk >35 or def > 35) cannot be healed for free.
	 *
	 *<p>Players who have done PVP in the last 2 hours cannot be healed free,
	 * unless they are very new to the game.
	 *
	 * @param npc
	 *            SpeakerNPC
	 * @param cost
	 *            The price which can be positive for a lump sum cost, 0 for free healing
	 *            or negative for a price dependent on level of player.
	 */
	public void addHealer(final SpeakerNPC npc, final int cost) {
		final HealerBehaviour healerBehaviour = new HealerBehaviour(cost);
		servicersRegister.add(npc.getName(), healerBehaviour);

		// Give attribute to healers
		npc.put("job_healer", 0);

		final Engine engine = npc.getEngine();

		engine.add(ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES, null,
				false, ConversationStates.ATTENDING, "我不能为你 #医疗 .", null);

		engine.add(ConversationStates.ATTENDING, ConversationPhrases.HEAL_MESSAGES , null,
				false, ConversationStates.ATTENDING,
				null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						ItemParserResult res = new ItemParserResult(true, "heal", 1, null);

						int cost = healerBehaviour.getCharge(res, player);
						currentBehavRes = res;

						String badboymsg = "";
						if (player.isBadBoy()) {
							cost = cost * 2;
							currentBehavRes.setAmount(2);
							badboymsg = " 杀人犯的医疗费要比正常人高.";
						}

						if (cost > 0) {
							raiser.say("医疗费用 " + cost
									+ "." + badboymsg + " 你带够钱了吗?");

							raiser.setCurrentState(ConversationStates.HEAL_OFFERED); // success
						} else if (cost < 0) {
							// price depends on level if cost was set at -1
							// where the factor is |cost| and we have a +1
							// to avoid 0 charge.
							cost = player.getLevel() * Math.abs(cost) + 1;
							raiser.say("治好你的某项能力费用 "
									+ cost
									+ " 钱. " + badboymsg + " 你带够钱了吗？");

							raiser.setCurrentState(ConversationStates.HEAL_OFFERED); // success
						} else {
							if ((player.getAtk() > 35) || (player.getDef() > 35)) {
								raiser.say("抱歉, 你太强壮, 已超过我医疗的极限.");
							} else if ((!player.isNew()
									&& (player.getLastPVPActionTime() > System
											.currentTimeMillis()
											- 2
											* MathHelper.MILLISECONDS_IN_ONE_HOUR) || player.isBadBoy())) {
								// ignore the PVP flag for very young
								// characters
								// (low atk, low def AND low level)
								raiser.say("抱歉, 你身上都有股怪味, 所以现在我不想为你治疗.");
							} else {
								raiser.say("好了, 你已康复, 还有别的事吗？");
								healerBehaviour.heal(player);
							}
						}

					}
		});

		engine.add(ConversationStates.HEAL_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				false, ConversationStates.ATTENDING,
				null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						int cost = healerBehaviour.getCharge(currentBehavRes, player);
						if (cost < 0) {
							cost = player.getLevel() * Math.abs(cost) + 1;
						}
						if (player.drop("money",
								cost)) {
							healerBehaviour.heal(player);
							raiser.say("好了, 你已康复, 还有别的事吗？");
						} else {
							raiser.say("抱歉, 你的钱不够啊.");
						}

						currentBehavRes = null;
					}
				});

		engine.add(ConversationStates.HEAL_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				false, ConversationStates.ATTENDING,
				"OK, 还有别的事吗?", null);
	}

}
