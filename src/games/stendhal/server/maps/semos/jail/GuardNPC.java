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
package games.stendhal.server.maps.semos.jail;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Jail;
import games.stendhal.server.entity.player.Player;

/**
 * The prison guard (original name: 马鲁斯) who's patrolling along the cells.
 *
 * @author hendrik
 */
public class GuardNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("马鲁斯") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(9,7));
				nodes.add(new Node(21,7));
				nodes.add(new Node(21,8));
				nodes.add(new Node(9,8));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addGreeting("你好！有什么我能帮忙 #help 的吗?");

				add(ConversationStates.ATTENDING,
						ConversationPhrases.JOB_MESSAGES,
						new NotInJailCondition(),
						ConversationStates.ATTENDING,
						"我是监狱的看守.",
						null);

				add(ConversationStates.ATTENDING,
						ConversationPhrases.JOB_MESSAGES,
						new InJailCondition(),
						ConversationStates.ATTENDING,
						"我是这个监狱的看守, 由于你的违法行为, 你已被限制自由.",
						null);

				add(ConversationStates.ATTENDING,
						ConversationPhrases.HELP_MESSAGES,
						new InJailCondition(),
						ConversationStates.ATTENDING,
						"请等管理员来这, 给你出处理决定. 在此期间不能逃跑. 如果你下线, 关押时间会从0重新计时. ",
						null);

				add(ConversationStates.ATTENDING,
						ConversationPhrases.HELP_MESSAGES,
						new NotInJailCondition(),
						ConversationStates.ATTENDING,
						"你知道要学会本地法律吗？而这些号子里的罪犯明显没有",
						null);

				addGoodbye();
			}};
			npc.setPosition(9, 7);
			npc.setDescription("你遇到了 塞门镇 监狱的看守, 马鲁斯.");
			npc.setEntityClass("youngsoldiernpc");
			zone.add(npc);
	}

	/**
	 * Is the player speaking to us in jail?
	 */
	public static class InJailCondition implements ChatCondition {

		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
			return Jail.isInJail(player);
		}
	}

	/**
	 * Is the player speaking to us not in jail?
	 */
	public static class NotInJailCondition implements ChatCondition {

		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
			return !Jail.isInJail(player);
		}
	}
}
