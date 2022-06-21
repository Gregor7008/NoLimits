package actions.administration;

import org.json.JSONArray;

import components.actions.Action;
import components.actions.ActionData;
import components.actions.ActionRequest;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class AutoRole implements ActionRequest {

	private Guild guild;
	private User user;
	
	@Override
	public void execute(Action event) {
		if (event.getSubAction().equals("add")) {
			Role role = event.getSubAction().getOptionAsRole(0);
			ConfigLoader.getGuildConfig(guild).getJSONArray("autoroles").put(role.getIdLong());
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "addsuccess")).queue();
			return;
		}
		if (event.getSubAction().equals("remove")) {
			Role role = event.getSubAction().getOptionAsRole(0);
			ConfigLoader.removeValueFromArray(ConfigLoader.getGuildConfig(guild).getJSONArray("autoroles"), role.getIdLong());
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "removesuccess")).queue();
			return;
		}
		if (event.getSubAction().equals("list")) {
			this.listroles(event);
		}
	}

	@Override
	public ActionData initialize() {
		ActionData actionData = new ActionData(this).setName("Auto Roles")
												    .setInfo("Configure roles that should be given to every new user joining")
													.setMinimumPermission(Permission.MANAGE_ROLES)
													.setCategory(ActionData.ADMINISTRATION)
													.setSubActions(new String[] {"add", "remove", "list"});
		return actionData;
	}
	
	private void listroles(Action event) {
		StringBuilder sB = new StringBuilder();
		JSONArray autoroles = ConfigLoader.getGuildConfig(guild).getJSONArray("autoroles");
		if (autoroles.isEmpty()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "noautoroles")).queue();
			return;
		}
		for (int i = 0; i < autoroles.length(); i++) {
			sB.append('#')
			  .append(String.valueOf(i) + "\s\s");
			if (i+1 == autoroles.length()) {
				sB.append(guild.getRoleById(autoroles.getLong(i)).getAsMention());
			} else {
				sB.append(guild.getRoleById(autoroles.getLong(i)).getAsMention() + "\n");
			}
		}
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,  this, "list").replaceDescription("{list}", sB.toString())).queue();
	}
}