/**
 *
 */
package games.stendhal.server.maps.quests.houses;

import java.util.Arrays;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.ExpressionType;
import games.stendhal.common.parser.JokerExprMatcher;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.condition.AgeGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TextHasNumberCondition;

final class AthorHouseSeller extends HouseSellerNPCBase {
	/** Cost to buy house in athor. */
	private static final int COST_ATHOR = 100000;
	private static final String FISHLICENSE2_QUEST_SLOT = "fishermans_license2";

	AthorHouseSeller(final String name, final String location, final HouseTax houseTax) {
		super(name, location, houseTax);
		init();
	}

	private void init() {
		// Other than the condition that you must not already own a house, there are a number of conditions a player must satisfy.
		// For definiteness we will check these conditions in a set order.
		// So then the NPC doesn't have to choose which reason to reject the player for (appears as a WARN from engine if he has to choose)

		// player is not old enough
		add(ConversationStates.ATTENDING,
				 Arrays.asList("购房", "房子", "买房", "房价"),
				 new NotCondition(new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE)),
				 ConversationStates.ATTENDING,
				 "阿托尔的房子需要花费 "
						 + getCost()
				 + " 钱. 但恐怕我还不能想信你能长期居住此地, 当你在Faiumoni游戏时长超过 "
				 + Integer.toString((HouseSellerNPCBase.REQUIRED_AGE / 60)) + " 小时后来来购买. 也许到那时我已晒成古铜色皮肤了.",
				 null);

		// player is old enough and hasn't got a house but has not done required quest
		add(ConversationStates.ATTENDING,
				 Arrays.asList("购房", "房子", "买房", "房价"),
				 new AndCondition(new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE),
								  new QuestNotCompletedCondition(AthorHouseSeller.FISHLICENSE2_QUEST_SLOT),
								  new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT)),
				 ConversationStates.ATTENDING,
				 "在你还没成为一个合格的 #渔民之前, 买阿托尔的公寓做什么? 我们会跟踪房主在此岛花遇的时间. 在证明了你是个好渔民后再回来吧.",
				 null);

		// player is eligible to buy a apartment
		add(ConversationStates.ATTENDING,
				 Arrays.asList("购房", "房子", "买房", "房价"),
				 new AndCondition(new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT),
								  new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE),
								  new QuestCompletedCondition(AthorHouseSeller.FISHLICENSE2_QUEST_SLOT)),
					ConversationStates.QUEST_OFFERED,
				 "新公寓要花费 "
				 + getCost()
				 + " 鍂.  而且, 你还要付 " + HouseTax.BASE_TAX
				 + " 钱的税金. 如果你有意购买, 请告诉我公寓的号码. 我需要核对公寓的情况."
				 + "阿托尔公寓的房屋号码是从 "
				 + getLowestHouseNumber() + " 到 " + getHighestHouseNumber() + ".",
				 null);

		// handle house numbers 101 to 108
		addMatching(ConversationStates.QUEST_OFFERED,
				 // match for all numbers as trigger expression
				ExpressionType.NUMERAL, new JokerExprMatcher(),
				new TextHasNumberCondition(getLowestHouseNumber(), getHighestHouseNumber()),
				ConversationStates.ATTENDING,
				null,
				new BuyHouseChatAction(getCost(), QUEST_SLOT));


		addJob("好吧, 我在这享受日光浴. 不过既然你问了, 我是在销售阿托尔的公寓. 可以看看我们的小册子 #https://stendhalgame.org/wiki/StendhalHouses.");
		addReply("渔民", "渔民合格证明是由阿多斯的圣地亚哥决定, 他设置了两个测验. 一旦你全部通过就证明你是合格的渔民.");
		setDirection(Direction.DOWN);
		setDescription("你遇见一个正要晒日光浴的人.");
		setEntityClass("swimmer1npc");
		setPosition(44, 40);
		initHP(100);

	}

	@Override
	protected int getCost() {
		return AthorHouseSeller.COST_ATHOR;
	}

	@Override
	protected void createPath() {
		setPath(null);
	}

	@Override
	public void say(final String text) {
		// He doesn't move around because he's "lying" on his towel.
		say(text, false);
	}

	@Override
	protected int getHighestHouseNumber() {
		return 108;
	}

	@Override
	protected int getLowestHouseNumber() {
		return 101;
	}
}
