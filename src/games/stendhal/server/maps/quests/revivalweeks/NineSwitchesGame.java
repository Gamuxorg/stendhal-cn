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
package games.stendhal.server.maps.quests.revivalweeks;

import java.util.Arrays;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.game.NineSwitchesGameBoard;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

/**
 * A Game about Nine switches game for one player
 *
 * @author hendrik
 */
public class NineSwitchesGame implements LoadableContent {
	private StendhalRPZone zone = null;
	private NineSwitchesGameBoard board;
	private SpeakerNPC npc;

	private static final int CHAT_TIMEOUT = 60;

	private void addBoard() {
		board = new NineSwitchesGameBoard(zone, 94, 106);
	}

	private void addNPC() {
		npc = new SpeakerNPC("马尔托斯") {
			@Override
				protected void createPath() {
					// NPC doesn't move
					setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Hi, 欢迎来玩我们的游戏九宫棋. 你的任务是把全部的箭头指向右."
						+ " 容易? 好吧, 这是个 #拉手.");
				addReply("拉手",
						" 每个开关连都接到隔壁, 并且能改变它们. 你只有一分钟解开这道迷题."
						+ " 要 #开始 玩吗?");
				addJob("我是本游戏管理员.");
				addGoodbye("很高兴见到你.");
				add(ConversationStates.ATTENDING,
						Arrays.asList("开始", "是", "是的"),
						ConversationStates.ATTENDING,
						"好运.",
						new PlayAction(board));
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};
		npc.setEntityClass("gamesupervisornpc");
		npc.setPlayerChatTimeout(CHAT_TIMEOUT);
		npc.setPosition(98, 104);
		npc.setDescription("你遇见了马尔托斯. 是不是嫉妒他风骚的头发?");
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}

	/**
	 * handles a play chat action
	 */
	private static class PlayAction implements ChatAction {
		private NineSwitchesGameBoard board;

		/**
		 * creates a new PlayAction.
		 *
		 * @param board
		 */
		public PlayAction(NineSwitchesGameBoard board) {
			this.board = board;
		}

		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			if (board.getPlayerName() != null) {
				npc.say("抱歉, " + player.getName() + " 有其他人正在玩. 请等一会.");
				return;
			}
			board.setPlayerName(player.getName());
		}
	}

	@Override
	public void addToWorld() {
		zone = SingletonRepository.getRPWorld().getZone("0_塞门_山_北2");

		addBoard();
		addNPC();
		board.setNPC(npc);
	}


	/**
	 * try to remove the content from the world-
	 *
	 * @return <code>true</code>
	 */
	@Override
	public boolean removeFromWorld() {
		NPCList.get().remove("保罗史瑞夫");
		zone.remove(npc);
		board.remove();
		return true;
	}
}
