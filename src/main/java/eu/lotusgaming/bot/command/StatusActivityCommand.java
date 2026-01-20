//Created by Maurice H. at 20.09.2024
package eu.lotusgaming.bot.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.simpleyaml.configuration.file.YamlFile;

import com.github.theholywaffle.teamspeak3.api.wrapper.Message;

import eu.lotusgaming.bot.main.LotusManager;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class StatusActivityCommand extends ListenerAdapter{
	
	private String[] activities = new String[]{"playing", "listening", "watching" };
	
	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
		if(event.getName().equals("set-activity") && event.getFocusedOption().getName().equals("option")) {
			List<Command.Choice> options = Stream.of(activities)
					.filter(word -> word.startsWith(event.getFocusedOption().getValue()))
					.map(word -> new Command.Choice(word, word))
					.collect(Collectors.toList());
			event.replyChoices(options).queue();
		}
	}
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		User user = event.getUser();
		if(event.getName().equals("set-status")) {
			if(getBotOwners().contains(user.getIdLong())) {
				StringSelectMenu.Builder onlinestatus = StringSelectMenu.create("lgconlinemenu");
				onlinestatus.addOption("Offline", "OFFLINE");
				onlinestatus.addOption("Online", "ONLINE");
				onlinestatus.addOption("Idle", "IDLE");
				onlinestatus.addOption("Do Not Disturb", "DONOTDISTURB");
				MessageCreateBuilder mcb = new MessageCreateBuilder();
				mcb.setContent("Select the new status for me :)");
				mcb.addComponents(ActionRow.of(onlinestatus.build()));
				event.reply(mcb.build()).setEphemeral(true).queue();
			}else {
				event.deferReply(true).addContent("Sorry, but you are not permitted to change stuff on me. Who do you think you are thinking you could change me lol?").queue();
			}
		}else if(event.getName().equals("set-activity")) {
			if(getBotOwners().contains(user.getIdLong())) {
				String activityValue = event.getOption("option").getAsString().toUpperCase();
				String textValue = event.getOption("text").getAsString();
				setActivity(activityValue, textValue);
				Activity activity = null;
				switch(activityValue) {
				case "PLAYING": activity = Activity.playing(textValue); break;
				case "LISTENING": activity = Activity.listening(textValue); break;
				case "WATCHING": activity = Activity.watching(textValue); break;
				}
				event.getJDA().getPresence().setActivity(activity);
				event.deferReply(true).addContent("Looks like I am now " + event.getOption("option").getAsString() + " " + textValue + " :)").queue();
			}else {
				event.deferReply(true).addContent("Sorry, but you are not permitted to change stuff on me. Who do you think you are thinking you could change me lol?").queue();
			}
		}
	}
	
	@Override
	public void onStringSelectInteraction(StringSelectInteractionEvent event) {
		if(event.getComponentId().equals("lgconlinemenu")) {
			String value = event.getValues().get(0);
			setOnlineStatus(value);
			OnlineStatus status = null;
			switch(value) {
			case "OFFLINE": status = OnlineStatus.OFFLINE; break;
			case "ONLINE": status = OnlineStatus.ONLINE; break;
			case "IDLE": status = OnlineStatus.IDLE; break;
			case "DONOTDISTURB": status = OnlineStatus.DO_NOT_DISTURB; break;
			default: status = OnlineStatus.ONLINE; break;
			}
			event.getJDA().getPresence().setStatus(status);
			event.deferReply(true).addContent("My status has been updated!").queue();
		}
	}
	
	List<Long> getBotOwners(){
		List<Long> owners = new ArrayList<>();
		try {
			YamlFile cfg = YamlFile.loadConfiguration(LotusManager.mainConfig);
			owners = cfg.getLongList("Bot.Owners");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return owners;
	}
	
	void setOnlineStatus(String status) {
		try {
			YamlFile cfg = YamlFile.loadConfiguration(LotusManager.mainConfig);
			cfg.set("Bot.Activity.Onlinestatus", status);
			cfg.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void setActivity(String type, String text) {
		try {
			YamlFile cfg = YamlFile.loadConfiguration(LotusManager.mainConfig);
			cfg.set("Bot.Activity.Type", type);
			cfg.set("Bot.Activity.Text", text);
			cfg.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}