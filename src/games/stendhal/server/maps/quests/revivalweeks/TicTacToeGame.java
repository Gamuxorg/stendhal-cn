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
import games.stendhal.server.entity.mapstuff.game.TicTacToeBoard;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.NPCSetDirection;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;

/**
 * A 井字棋 game for two players
 *
 * @author hendrik
 */
public class TicTacToeGame implements LoadableContent {
	private StendhalRPZone zone = null;
	private TicTacToeBoard board;
	private SpeakerNPC paul;

	/**
	 * creates TicTacToeBoard and adds it to the world.
	 */
	private void addBoard() {
		board = new TicTacToeBoard();
		board.setPosition(83, 114);
		zone.add(board);
		board.addToWorld();
	}

	/**
	 * adds the NPC which moderates the game to the world.
	 */
	private void addNPC() {
		paul = new SpeakerNPC("保罗史瑞夫") {
			@Override
			protected void createPath() {
				// NPC doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new GreetingMatchesNameCondition(getName()), true,
						ConversationStates.IDLE,
						"Hi, 欢迎来玩井字棋. 你的任务是用一种棋子填满一条直线 "
						+ "(横向, 竖向, 对角) . "
						+ "你需要一个对手 #开始 游戏.",
						null);

				add(ConversationStates.IDLE,
						ConversationPhrases.HELP_MESSAGES,
						ConversationStates.IDLE,
						"要站在棋子旁边才能移动它.",
						null);
				add(ConversationStates.IDLE,
						ConversationPhrases.JOB_MESSAGES,
						ConversationStates.IDLE,
						"我是本游戏管理员.",
						null);
				add(ConversationStates.IDLE,
						ConversationPhrases.GOODBYE_MESSAGES,
						ConversationStates.IDLE,
						"很高兴见到你",
						new NPCSetDirection(Direction.DOWN));
				add(ConversationStates.IDLE,
						Arrays.asList("开始", "是的", "是"),
						ConversationStates.IDLE,
						null,
						new PlayAction(board));
			}
		};
		paul.setEntityClass("paulnpc");
		paul.setPosition(84, 112);
		paul.setDirection(Direction.DOWN);
		zone.add(paul);
	}

	/**
	 * handles a play chat action
	 */
	private static class PlayAction implements ChatAction {
		private TicTacToeBoard board;
		private long lastPlayerAdded;

		/**
		 * creates a new PlayAction.
		 *
		 * @param board
		 */
		public PlayAction(TicTacToeBoard board) {
			this.board = board;
		}

		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			if (board.isGameActive()) {
				npc.say("抱歉, " + player.getName() + " 有其他人正在游戏. 请等一会.");
				return;
			}

			if (lastPlayerAdded + 60000 < System.currentTimeMillis()) {
				board.getPlayers().clear();
			}

			if (board.getPlayers().isEmpty()) {
				lastPlayerAdded = System.currentTimeMillis();
				npc.say("好, " + player.getName() + " 你已报名下次游戏对战. 你要 #开始 与 " + player.getName() + " 对战吗?");
				board.getPlayers().add(player.getName());
			} else {
				if (board.getPlayers().get(0).equals(player.getName())) {
					npc.say("Okay " + player.getName() + ", 你已报名下次游戏对战. 你要 #开始 与 " + player.getName() + " 对战吗?");
					return;
				}

				npc.say(board.getPlayers().get(0) + ", 你是蓝色 X 一方, 所以你先下. " + player.getName() + ", 你是 红 O 方. 看看谁能赢!");
				board.startGame();
				board.getPlayers().add(player.getName());
			}
		}
	}

	@Override
	public void addToWorld() {
		zone = SingletonRepository.getRPWorld().getZone("0_塞门_山_北2");

		addBoard();
		addNPC();
		board.setNPC(paul);
	}

	/**
	 * try to remove the content from the world-
	 *
	 * @return <code>true</code>
	 */
	@Override
	public boolean removeFromWorld() {
		NPCList.get().remove("保罗史瑞夫");
		zone.remove(paul);
		zone.remove(board);
		return true;
	}

}
