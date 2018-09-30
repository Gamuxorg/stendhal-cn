
package games.stendhal.server.maps.quests.houses;

import java.util.Arrays;

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

final class KirdnehHouseSeller extends HouseSellerNPCBase {
	/** Cost to buy house in 克德内. */
	private static final int COST_KIRDNEH = 120000;
	private static final String KIRDNEH_QUEST_SLOT = "weekly_item";

	KirdnehHouseSeller(final String name, final String location, final HouseTax houseTax) {
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
				 "克德内新房售价为 "
						 + getCost()
				 + " 钱. 但恐怕我还不能想信你能长期居住此地, 当你在Faiumoni游戏时长超过  "
				 + Integer.toString((HouseSellerNPCBase.REQUIRED_AGE / 60)) + " 小时后来来购买.",
				 null);

		// player is old enough and hasn't got a house but has not done required quest
		add(ConversationStates.ATTENDING,
				 Arrays.asList("购房", "房子", "买房", "房价"),
				 new AndCondition(new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE),
								  new QuestNotCompletedCondition(KirdnehHouseSeller.KIRDNEH_QUEST_SLOT),
									 new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT)),
				 ConversationStates.ATTENDING,
				 "克德内新房售价为 "
				 + getCost()
				 + " 钱. 但我的原则是, 不卖给没有好 #信誉 的购房者.",
				 null);

		// player is eligible to buy a house
		add(ConversationStates.ATTENDING,
				 Arrays.asList("购房", "房子", "买房", "房价"),
				 new AndCondition(new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT),
								  new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE),
								  new QuestCompletedCondition(KirdnehHouseSeller.KIRDNEH_QUEST_SLOT)),
					ConversationStates.QUEST_OFFERED,
				 "克德内新房售价为 "
				 + getCost()
				 + " 钱.  而且, 并且, 你还要每月支付房产税 " + HouseTax.BASE_TAX
				 + " 钱. 如果你已有房产, 请告诉我房间号码. 我会核对是否有效. "
				 + "克德内房产的号码包括从 "
				 + getLowestHouseNumber() + " 到 " + getHighestHouseNumber() + ".",
				 null);

		// handle house numbers 26 to 49
		addMatching(ConversationStates.QUEST_OFFERED,
				// match for all numbers as trigger expression
				ExpressionType.NUMERAL, new JokerExprMatcher(),
				new TextHasNumberCondition(getLowestHouseNumber(), getHighestHouseNumber()),
				ConversationStates.ATTENDING,
				null,
				new BuyHouseChatAction(getCost(), QUEST_SLOT));

		addJob("我是房子销售经理. 简单说, 我卖房给克德内的居民. 如果你有兴趣可以寻问 #房价. 我们的售楼部在 #https://stendhalgame.org/wiki/StendhalHouses.");
		addReply("信誉", "我会向 哈泽尔 咨询你的情况. 她提供你最近为她完成的任务, 并没有留下没完成的任务, 她才会认同你.");
		addReply("安伯", "Oh 安伯... 我真的想她了, 刚刚我们还讨论过. 然后她就 #离开 了. 希望她还好.");
		addReply("离开", "我也不知道她现在去了哪儿. 她儿子杰夫还在等她回家, 但我听说一些人曾在法多森林里见过她, 就在南面.");
		setDescription("你遇见了一个看上去很精明的人.");
		setEntityClass("man_004_npc");
		setPosition(31, 4);
		initHP(100);

	}

	@Override
	protected int getCost() {
		return KirdnehHouseSeller.COST_KIRDNEH;
	}

	@Override
	protected void createPath() {
		setPath(null);
	}

	@Override
	protected int getHighestHouseNumber() {
		return 49;
	}

	@Override
	protected int getLowestHouseNumber() {
		return 26;
	}
}
