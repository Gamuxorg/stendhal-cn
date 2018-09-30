/**
 *
 */
package games.stendhal.server.maps.quests.houses;

import java.util.List;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

final class ListUnboughtHousesAction implements ChatAction {
	private final String location;

	/**
	 * Creates a new ListUnboughtHousesAction.
	 *
	 * @param location
	 *            where are the houses?
	 */
	ListUnboughtHousesAction(final String location) {
		this.location = location;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		final List<String> unbought = HouseUtilities.getUnboughtHousesInLocation(location);
		if (unbought.size() > 0) {
			raiser.say("据我统计, " + unbought + " 就是全部在售的 #房子 了.");
		} else {
			raiser.say("抱歉, " + location + " 地区没有房源在售.");
		}
	}
}
