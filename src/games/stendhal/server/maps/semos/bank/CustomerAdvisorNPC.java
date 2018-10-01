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
package games.stendhal.server.maps.semos.bank;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.PlaySoundAction;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
/**
 * ZoneConfigurator configuring the NPC (former known as Dagobert) in semos bank
 */
public class CustomerAdvisorNPC implements ZoneConfigurator {

	private static final class VaultChatAction implements ChatAction {

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			final StendhalRPZone vaultzone = (StendhalRPZone) SingletonRepository
					.getRPWorld().getRPZone("int_vault");
			String zoneName = player.getName() + "_vault";

			final StendhalRPZone zone = new Vault(zoneName, vaultzone, player);


			SingletonRepository.getRPWorld().addRPZone(zone);
			player.teleport(zone, 4, 5, Direction.UP, player);
			((SpeakerNPC) npc.getEntity()).setDirection(Direction.DOWN);
		}
	}

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("达格伯特") {

			@Override
			public void createDialog() {
				addGreeting("欢迎来到 塞门镇 银库! 我在这里为你服务. #帮助 管理你的个人物品箱.");
				addHelp("顺着走廊向右走, 你会发现特殊的物品箱. 你可以把你的随身物品放进里面, 其他人却不能碰！箱子区域布了阵法保障存取人的 #安全.");
				addReply("安全", "当你站在箱子旁整理物品时, 其他人或动物都不能走到你附近. 一种魔法阵会阻止其他对你施法想靠近你的人. 当你需要离开, 下面我会告诉你关于一些 #安全交易 的事.");
				addReply("安全交易", "要和另一个玩家交易时, 鼠标右键点击他, 选择 '交易'. 如果他们也想与你交易, 你会看到一个弹出窗口, 你可以把需要交易的物品拖进里面. 也能看到对方给你的东西. 双方点击“出价” , 这时需要你们双方同意后交易完成. ");
				addJob("我是 塞门镇银库的客户经理.");
				addOffer("如果你希望单独访问你的个人仓库, 我能给你一个 #私人仓库 . 里面的指南手册会解释它是如何工作的. ");
				addGoodbye("很荣幸为您服务.");
				add(ConversationStates.ANY, "私人仓库", new QuestCompletedCondition("armor_dagobert"), ConversationStates.IDLE, null,
						new MultipleActions(new PlaySoundAction("keys-1", true), new VaultChatAction()));

				add(ConversationStates.ANY, "私人仓库", new QuestNotCompletedCondition("armor_dagobert"), ConversationStates.ATTENDING, "或许你能够赢得我的 #好感 , 那时我会告诉你更多关于私人银行仓库的东西. ", null);

				// remaining behaviour defined in games.stendhal.server.maps.quests.ArmorForDagobert
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}

		};
		npc.setPosition(9, 23);
		npc.setDirection(Direction.DOWN);
		npc.setDescription("你见到了 达格伯特. 他看起来值得信赖.");
		npc.setHP(95);
		npc.setEntityClass("youngnpc");
		zone.add(npc);
	}

}
