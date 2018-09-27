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
package games.stendhal.server.entity.npc;

import java.awt.Rectangle;
import java.util.Map;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Dice;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.Area;
import marauroa.common.Pair;

// TODO: replace this subclass with normal ChatConditions/ChatActions
public abstract class CroupierNPC extends SpeakerNPC {

	/**
	 * The time (in seconds) it takes before the NPC removes thrown dice from
	 * the table.
	 */
	private static final int CLEAR_PLAYING_AREA_TIME = 10;

	/**
	 * The area on which the dice have to be thrown.
	 */
	private Area playingArea = null;

	/**
	 * A list where each possible dice sum is the index of the element which is
	 * either the name of the prize for this dice sum and the congratulation
	 * text that should be said by the NPC, or null if the player doesn't win
	 * anything for this sum.
	 */
	private Map<Integer, Pair<String, String>> prizes = null;

	public CroupierNPC(final String name) {
		super(name);
	}

	public void setPrizes(final Map<Integer, Pair<String, String>> prizes) {
		this.prizes = prizes;
	}

	/**
	 * Sets the playing area (a table or something like that).
	 *
	 * @param playingArea
	 *            shape of the playing area (in the same zone as the NPC)
	 */
	public void setTableArea(final Rectangle playingArea) {
		this.playingArea = new Area(getZone(), playingArea);
	}

	public void onThrown(final Dice dice, final Player player) {
		if (playingArea.contains(dice)) {
			final int sum = dice.getSum();
			processWin(player, sum);
			// The croupier takes the dice away from the table after some time.
			// This is simulated by shortening the degradation time of the dice.
			SingletonRepository.getTurnNotifier().dontNotify(dice);
			SingletonRepository.getTurnNotifier().notifyInSeconds(CLEAR_PLAYING_AREA_TIME, dice);
		}
	}

	void processWin(final Player player, final int sum) {
		final Pair<String, String> prizeAndText = prizes.get(sum);
		if (prizeAndText != null) {
			final String prizeName = prizeAndText.first();
			final String text = prizeAndText.second();
			final Item prize = SingletonRepository.getEntityManager().getItem(
					prizeName);
			if (prizeName.equals("黄金护腿")) {
				prize.setBoundTo(player.getName());
			}

			say("祝贺你, " + player.getTitle() + ", 你共有 "
					+ sum + " 点. " + text);
			player.equipOrPutOnGround(prize);
		} else {
			say("Sorry, "
					+ player.getTitle()
					+ ", 你只有 "
					+ sum
					+ " 点. 什么都没赢到, 祝下次好运!");
		}
	}

	/**
	 * Access the playing area for JUnit tests.
	 * @return playing area
	 */
	public Area getPlayingArea() {
		return playingArea;
	}
}
