package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.player.Player;

public class HerbsForJynath extends AbstractQuest {

    public static final String QUEST_SLOT = "herbs_jynath";
    // The things that need to be collected by user to
    protected static final String NEEDED_ITEMS = "啤酒=2;木头=2";
    // "herbs" added to quest messages
    List<String> NEW_QUEST_MESSAGES = ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, "药草");

    @Override
    public void addToWorld() {
        prepareQuestStep();
        prepareBringingStep();
    }

    @Override
    public String getSlotName() {
        return QUEST_SLOT;
    }

    @Override
    public String getName() {
        return "HerbsForJynath";
    }

    // Add the following in at a later date

    // Other quests have a getTitle() method, I thought I should add this
    // as well
    public String getTitle() {
    	return "Jynath的药草";
	}

    /*
	@Override
	public int getMinLevel() {
		return 3;
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	} */

    // TODO: update this for the different stages of the quest
    @Override
	public List<String> getHistory(final Player player) {
        final List<String> questHistory = new ArrayList<String>();
        if (!player.hasQuest(QUEST_SLOT)) {
            return questHistory;
        }
        questHistory.add("Jynath 需要一些药草, 去试验一种魔法, 她想让我帮忙找一些.");
        final String questState = player.getQuest(QUEST_SLOT);
        if ("rejected".equals(questState)) {
        	questHistory.add("我选择不帮 Jynath.");
        } else if (!"done".equals(questState)) {
            questHistory.add("我要带一些药草给 Jynath.");
        } else {
        	questHistory.add("我带来一些药草给了 Jynath. 我认为她会用它们制毒.");
        }
        return questHistory;
    }

    public void prepareQuestStep() {
        // get a reference to the Jynath npc
        SpeakerNPC npc = npcs.get("Jynath");

        // make sure that #herbs is only replied to if the quest has been
        // offered, (accepted or not) but not yet completed.

        // The first message when the player says "quest" and initiates the
        // quest (note: no asking if the players wants to accept)
        npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            // the quest is not active and not completed
            // ie. first time
            new AndCondition(new QuestNotActiveCondition(QUEST_SLOT), new QuestNotCompletedCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "rejected")),
            ConversationStates.QUEST_OFFERED,
            "我想制作一种魔法药, 但还需要配方中的几种 #药草 .你可以帮我找点吗?",
            null);

        // if the player wants to ask what herbs are needed, just after
        // being offered the quest
        npc.add(ConversationStates.QUEST_OFFERED,
        	"药草",
        	null,
        	ConversationStates.QUEST_OFFERED,
        	"我还需要的 #药草 清单有 [list]",
            null);

        // if the quest is accepted
        npc.add(ConversationStates.QUEST_OFFERED,
        	ConversationPhrases.YES_MESSAGES,
        	null,
        	ConversationStates.ATTENDING,
        	"太好了! 我需要的药草有 [list]",
        	new SetQuestAction(QUEST_SLOT, "start"));

        // if the quest is rejected
        npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            null,
            ConversationStates.ATTENDING,
            "哦 好吧, 如果你改变主意了再和我说.",
            new SetQuestAction(QUEST_SLOT, "rejected"));

        // if the player wants to start the quest, and has rejected it earlier

        npc.add(ConversationStates.ATTENDING,
        	NEW_QUEST_MESSAGES,
        	new QuestInStateCondition(QUEST_SLOT, "rejected"),
        	ConversationStates.QUEST_OFFERED,
        	"我们换个思路, 对吧? 如果你喜欢,你还可以帮我找 #药草. 你还要这么做吗?",
        	null);

        // this lists the herbs needed. This npc.add() is for QUEST_OFFERED, just after the
        // quest was offered, code is repeated just below for ATTENDING, (otherwise the same)
        npc.add(ConversationStates.ATTENDING,
        	NEW_QUEST_MESSAGES,
        	new AndCondition(new QuestActiveCondition(QUEST_SLOT), new QuestNotCompletedCondition(QUEST_SLOT)),
        	ConversationStates.ATTENDING,
        	"我需要 #药草 配制毒药. 我需要的有 [list]",
            null);

        // send him away if he has completed the quest already.
        npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new QuestCompletedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "谢谢你的药草. 现在我可以制作魔力药水了!",
            null);
    }

    public void prepareBringingStep() {

    	// get a reference to the Jynath npc
        SpeakerNPC npc = npcs.get("Jynath");

        // created conditions and actions for the player having all the necessary items
        // purpose: to clean up the code
        AndCondition playerHasAllCondition = new AndCondition(new PlayerHasItemWithHimCondition("啤酒", 2), new PlayerHasItemWithHimCondition("木头", 2));
        AndCondition playerIsInQuestCondition = new AndCondition(new QuestActiveCondition(QUEST_SLOT), new QuestNotCompletedCondition(QUEST_SLOT));

    	// create the reward that is given to player for winning
    	List<ChatAction> rewardActions = new LinkedList<ChatAction>();
        rewardActions.add(new DropItemAction("啤酒", 2));
        rewardActions.add(new DropItemAction("木头", 2));
        rewardActions.add(new EquipItemAction("money", 1234));
        rewardActions.add(new IncreaseXPAction(1234));
        rewardActions.add(new IncreaseKarmaAction(10));
        rewardActions.add(new SetQuestAction(QUEST_SLOT, "done"));

        // change text
        npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new QuestActiveCondition(QUEST_SLOT), new QuestNotCompletedCondition(QUEST_SLOT)),
            ConversationStates.ATTENDING,
            "欢迎! 需要我的帮助吗? 你带来我要的 #药草 了吧. 我要用它们制作魔力药水.",
            null);

        // add a yes response to the above

        npc.add(
            ConversationStates.ATTENDING,
            NEW_QUEST_MESSAGES,
            new AndCondition(playerHasAllCondition, playerIsInQuestCondition),
            ConversationStates.ATTENDING,
            "确实是我要的! 太好了! 谢谢你的帮忙.",
            new MultipleActions(rewardActions));
    }

	@Override
	public String getNPCName() {
		return "Jynath";
	}
}