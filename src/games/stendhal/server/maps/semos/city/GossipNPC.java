/***************************************************************************
 *                   (C) Copyright 2003-2016 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.city;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;

/**
 * A guy (original name: 诺姆安巴) who looks into the windows of the bakery
 * and the house next to it.
 *
 * Basically all he does is sending players to the retired adventurer at
 * the dungeon entrance.
 */
public class GossipNPC implements ZoneConfigurator {

    @Override
	public void configureZone(StendhalRPZone zone,
            Map<String, String> attributes) {
        buildNPC(zone);
    }

    private void buildNPC(StendhalRPZone zone) {
        final SpeakerNPC npc = new SpeakerNPC("诺姆安巴") {

            @Override
            public void createDialog() {
                addGreeting(null,new SayTextAction("又见面了, [name]. 现在需要我 #帮忙 吗?"));

                // A little trick to make NPC remember if it has met
                // player before and react accordingly
                // NPC_name quest doesn't exist anywhere else neither is
                // used for any other purpose
                add(ConversationStates.IDLE,
                    ConversationPhrases.GREETING_MESSAGES,
                    new AndCondition(new GreetingMatchesNameCondition(getName()),
                    		new QuestNotCompletedCondition("Nomyr")),
                    ConversationStates.INFORMATION_1,
                    "呵呵... Oh, 你好陌生人! 你好像有点迷惑...想听听最近的小道消息吗？",
                    new SetQuestAction("Nomyr", "done"));

                add(ConversationStates.ATTENDING,
                    ConversationPhrases.YES_MESSAGES,
                    null,
                    ConversationStates.INFORMATION_1,
                    "年轻人都加入 Deniran 帝国军去了南方打仗. 所以城内的防御几乎瘫痪, 地牢时常跑出来一些怪兽, 你能帮助我们吗？",
                    null);

                add(ConversationStates.ATTENDING,
                    ConversationPhrases.NO_MESSAGES,
                    null,
                    ConversationStates.IDLE,
                    "嗯. 好的, 你不忙的话, 可以帮我到那边的窗口边瞄一眼, 我正想弄清楚里面到底发生了什么, ",
                    null);

                add(ConversationStates.ATTENDING,
                    "布袋",
                    null,
                    ConversationStates.ATTENDING,
                    "啊. 你不是真的好奇我麻袋里装了什么, 你可以向 卡尔 要一个空的, 可以把一些你喜欢的东西放进去, 甚至是糖!",
                    null);

                add(ConversationStates.ATTENDING,
                    "karl",
                    null,
                    ConversationStates.ATTENDING,
                    "Oh. 他是一个平易近人的老农夫, 住在离这不远的东面, 顺着到 阿多斯 的路很容易就找到他",
                    null);

                add(ConversationStates.INFORMATION_1,
                    ConversationPhrases.YES_MESSAGES,
                    null,
                    ConversationStates.IDLE,
                    "首先你应该和 海云那冉 谈谈. 他是个很棒的好英雄, 也是这里留下不多的最好的守卫...我想他会乐意给你一些建议！祝好运.",
                    null);

                add(ConversationStates.INFORMATION_1,
                    ConversationPhrases.NO_MESSAGES,
                    null,
                    ConversationStates.IDLE,
                    "Awww... 因为你是个胆小鬼？呼呼.",
                    null);

                addHelp("我是... 他们都叫我万事通. 我可以告诉你最近的一些传言. 你要听吗?");
                addJob("我知道每个 塞门镇 的每个传言！也虚构了传言中的大部分. 然而,其中一个关于 黑姆伊索 把私武器走私给像你一样的冒险者的消息却是真的!");
                addQuest("感谢你的提问, 但现在不需要任何东西！... 咦! 我只希望这个 #布袋 不那么笨重.");
                addOffer("Tz 尽管我背着这个 #布袋, 也不意味着我能卖你什么东西...但我听说那边的老家货 梦金斯 需要帮助");
                addGoodbye();
            }


            @Override
            protected void createPath() {
                final List<Node> nodes = new LinkedList<Node>();
                nodes.add(new Node(46, 20));
                nodes.add(new Node(46, 21));
                nodes.add(new Node(50, 21));
                nodes.add(new Node(50, 20));
                nodes.add(new Node(46, 21));
                setPath(new FixedPath(nodes, true));
            }

        };
        npc.setPosition(46, 20);
        npc.setEntityClass("thiefnpc");
        zone.add(npc);
        npc.setDescription("诺姆安巴 老是背着一个大包,看起来有点古怪. ");
    }

}
