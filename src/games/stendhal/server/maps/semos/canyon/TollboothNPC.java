/***************************************************************************
 *                   (C) Copyright 2003-2013 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.canyon;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.TeleportAction;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;

/**
 * The bridge tollbooth NPC
 *
 * @author AntumDeluge
 */
public class TollboothNPC implements ZoneConfigurator  {

    private final int REQUIRED_COINS = 25;

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("陶乐") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			public void createDialog() {
			    addGreeting("你好, 如果你想过桥去对面的 #安图木 ,你需要 #支付 " + REQUIRED_COINS + " 金币.");
				addHelp("如果你想过桥去对面的 #安图木 ,你需要 #支付 " + REQUIRED_COINS + " 金币.");
				addJob("我守护这座连接着 塞门镇 和 安图木.");
				addGoodbye("再会.");
				addReply("安图木", "安图木 是圣地.");

			}

            @Override
            protected void onGoodbye(RPEntity player) {
                setDirection(Direction.LEFT);
            }
		};

        // Player has enough money and pays toll
        npc.add(ConversationStates.ATTENDING,
                Arrays.asList("支付","付钱"),
                new PlayerHasItemWithHimCondition("money", 25),
                ConversationStates.IDLE,
                "返回 塞门地区 不用付费, 只用走过大门就行.",
                new MultipleActions(new DropItemAction("money", 25),
                        new TeleportAction("0_塞门_峡谷", 36, 29, Direction.UP))
                );

        // Player does not have enough money for toll
        npc.add(ConversationStates.ATTENDING,
                Arrays.asList("支付","付钱"),
                new NotCondition(new PlayerHasItemWithHimCondition("money", 25)),
                ConversationStates.ATTENDING,
                "很抱歉, 你的钱不够.",
                null
                );

        npc.setPosition(37, 30);
        npc.setDirection(Direction.LEFT);
        npc.setEntityClass("youngsoldiernpc");
        npc.setDescription("你见到了大桥收费员陶乐.");
        zone.add(npc);
	}
}
