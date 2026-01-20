//Created by Chris Wille at 06.02.2024
package eu.lotusgaming.bot.command;

import java.awt.Color;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import org.simpleyaml.configuration.file.YamlFile;

import eu.lotusgaming.bot.main.LotusManager;
import eu.lotusgaming.bot.misc.MySQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class SuggestionBoard extends ListenerAdapter {

	private static LinkedHashMap<Member, Long> suggestionMsgId = new LinkedHashMap<>();
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if(event.getName().equals("setsuggestionboardchannel")) {
			if(event.getOption("channel") == null) {
				event.deferReply(true).addContent("Hey, you must mention the text channel you want to have the Suggestion Board in.").queue();
			}else {
				if(event.getOption("channel").getAsChannel().getType() == ChannelType.TEXT) {
					TextChannel channel = event.getOption("channel").getAsChannel().asTextChannel();
					EmbedBuilder eb = new EmbedBuilder();
					eb.setTitle("Welcome to the Suggestion Board of Lotus Gaming");
					eb.setColor(Color.decode("#6cb547"));
					eb.setDescription("Thanks for creating a new suggestion via our suggestion board!\n"
							+ "You can select one of 4 Topics to suggest stuff in.\n"
							+ "Just choose the nearest topic for your suggestion.\n \n"
							+ "Fill out the Modal and your suggestion will appear in <#1204203118867513364>!\n"
							+ "\n"
							+ "Users can vote there as well in the dedicated thread of said suggestion they can discuss the suggestion.");
					MessageCreateBuilder mcb = new MessageCreateBuilder()
					.addEmbeds(eb.build())
					.addComponents(ActionRow.of(
							Button.secondary("gamesugg", "Game Suggestion"),
							Button.secondary("botsugg", "Bot Suggestion"),
							Button.secondary("websugg", "Website Suggestion"),
							Button.danger("miscsugg", "Other Suggestions")
							));
					channel.sendMessage(mcb.build()).queue();
					event.reply("The suggestion info board has been sent into " + channel.getAsMention()).queue();
				}else {
					event.deferReply(true).addContent("Hey, the channel must be a text channel!").queue();
				}
			}
		}else if(event.getName().equals("setsuggestionmessagechannel")) {
			if(event.getOption("channel") == null) {
				event.deferReply(true).addContent("Hey, you must mention the text channel you want to have the Suggestion Board in.").queue();
			}else {
				if(event.getOption("channel").getAsChannel().getType() == ChannelType.TEXT) {
					TextChannel channel = event.getOption("channel").getAsChannel().asTextChannel();
					try {
						YamlFile cfg = YamlFile.loadConfiguration(LotusManager.mainConfig);
						cfg.set("Suggestion.PostChannel", channel.getIdLong());
						cfg.save();
					} catch (IOException e) {
						e.printStackTrace();
					}
					event.reply("The channel where newly made suggestions are sent in has been set to " + channel.getAsMention()).queue();
				}else {
					event.deferReply(true).addContent("Hey, the channel must be a text channel!").queue();
				}
			}
		}
	}
	
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		Guild guild = event.getGuild();
		Role upperStaff = guild.getRoleById(1203440790081380443L);
		if(event.getComponentId().equals("gamesugg")) {
			Modal modal = Modal.create("gamesuggmodal", "Game Suggestion")
			.addComponents(
				Label.of("Title", TextInput.create("gametitle", TextInputStyle.SHORT).build()),
				Label.of("Description", TextInput.create("gamedesc", TextInputStyle.PARAGRAPH)
						.setPlaceholder("Explain your suggestion as good as you can. Upload Pictures in it's created thread afterward.")
						.build()),
				Label.of("Why should we add it?", TextInput.create("gamewta", TextInputStyle.PARAGRAPH)
						.setPlaceholder("Give us a reason, why we should add that suggestion!")
						.build())
			).build();
			event.replyModal(modal).queue();
		}else if(event.getComponentId().equals("botsugg")) {
			Modal modal = Modal.create("botsuggmodal", "Bot Suggestion")
			.addComponents(
				Label.of("Title", TextInput.create("bottitle", TextInputStyle.SHORT).build()),
				Label.of("Description", TextInput.create("botdesc", TextInputStyle.PARAGRAPH)
						.setPlaceholder("Explain your suggestion as good as you can. Upload Pictures in it's created thread afterward.")
						.build()),
				Label.of("Why should we add it?", TextInput.create("botwta", TextInputStyle.PARAGRAPH)
						.setPlaceholder("Give us a reason, why we should add that suggestion!")
						.setRequiredRange(50, 1000)
						.build())
			).build();
			event.replyModal(modal).queue();
		}else if(event.getComponentId().equals("websugg")) {
			Modal modal = Modal.create("websuggmodal", "Website Suggestion")
			.addComponents(
				Label.of("Title", TextInput.create("webtitle", TextInputStyle.SHORT)
						.setRequiredRange(10, 100)
						.build()),
				Label.of("Description", TextInput.create("webdesc", TextInputStyle.PARAGRAPH)
						.setRequiredRange(50, 1000)
						.setPlaceholder("Explain your suggestion as good as you can. Upload Pictures in it's created thread afterward.")
						.build()),
				Label.of("Why should we add it?", TextInput.create("webwta", TextInputStyle.PARAGRAPH)
						.setRequiredRange(50, 1000)
						.setPlaceholder("Give us a reason, why we should add that suggestion!")
						.build())
			).build();
			event.replyModal(modal).queue();
		}else if(event.getComponentId().equals("botsugg")) {
			Modal modal = Modal.create("botsuggmodal", "Bot Suggestion")
					.addComponents(
						Label.of("Title", TextInput.create("bottitle", TextInputStyle.SHORT).build()),
						Label.of("Description", TextInput.create("botdesc", TextInputStyle.PARAGRAPH)
								.setPlaceholder("Explain your suggestion as good as you can. Upload Pictures in it's created thread afterward.")
								.build()),
						Label.of("Why should we add it?", TextInput.create("botwta", TextInputStyle.PARAGRAPH)
								.setPlaceholder("Give us a reason, why we should add that suggestion!")
								.setRequiredRange(50, 1000)
								.build())
					).build();
			event.replyModal(modal).queue();
		}else if(event.getComponentId().equals("websugg")) {
			Modal modal = Modal.create("websuggmodal", "Website Suggestion")
					.addComponents(
						Label.of("Title", TextInput.create("webtitle", TextInputStyle.SHORT)
								.setRequiredRange(10, 100)
								.build()),
						Label.of("Description", TextInput.create("webdesc", TextInputStyle.PARAGRAPH)
								.setRequiredRange(50, 1000)
								.setPlaceholder("Explain your suggestion as good as you can. Upload Pictures in it's created thread afterward.")
								.build()),
						Label.of("Why should we add it?", TextInput.create("webwta", TextInputStyle.PARAGRAPH)
								.setRequiredRange(50, 1000)
								.setPlaceholder("Give us a reason, why we should add that suggestion!")
								.build())
					).build();
			event.replyModal(modal).queue();
		}else if(event.getComponentId().equals("botsugg")) {
			Modal modal = Modal.create("botsuggmodal", "Bot Suggestion")
					.addComponents(
						Label.of("Title", TextInput.create("bottitle", TextInputStyle.SHORT).build()),
						Label.of("Description", TextInput.create("botdesc", TextInputStyle.PARAGRAPH)
								.setPlaceholder("Explain your suggestion as good as you can. Upload Pictures in it's created thread afterward.")
								.build()),
						Label.of("Why should we add it?", TextInput.create("botwta", TextInputStyle.PARAGRAPH)
								.setPlaceholder("Give us a reason, why we should add that suggestion!")
								.setRequiredRange(50, 1000)
								.build())
					).build();
			event.replyModal(modal).queue();
		}else if(event.getComponentId().equals("websugg")) {
			Modal modal = Modal.create("websuggmodal", "Website Suggestion")
					.addComponents(
						Label.of("Title", TextInput.create("webtitle", TextInputStyle.SHORT)
								.setRequiredRange(10, 100)
								.build()),
						Label.of("Description", TextInput.create("webdesc", TextInputStyle.PARAGRAPH)
								.setRequiredRange(50, 1000)
								.setPlaceholder("Explain your suggestion as good as you can. Upload Pictures in it's created thread afterward.")
								.build()),
						Label.of("Why should we add it?", TextInput.create("webwta", TextInputStyle.PARAGRAPH)
								.setRequiredRange(50, 1000)
								.setPlaceholder("Give us a reason, why we should add that suggestion!")
								.build())
					).build();
			event.replyModal(modal).queue();
		}else if(event.getComponentId().equals("botsugg")) {
			Modal modal = Modal.create("botsuggmodal", "Bot Suggestion")
					.addComponents(
						Label.of("Title", TextInput.create("bottitle", TextInputStyle.SHORT).build()),
						Label.of("Description", TextInput.create("botdesc", TextInputStyle.PARAGRAPH)
								.setPlaceholder("Explain your suggestion as good as you can. Upload Pictures in it's created thread afterward.")
								.setRequiredRange(50, 1000)
								.build()),
						Label.of("Why should we add it?", TextInput.create("botwta", TextInputStyle.PARAGRAPH)
								.setPlaceholder("Give us a reason, why we should add that suggestion!")
								.setRequiredRange(50, 1000)
								.build())
					).build();
			event.replyModal(modal).queue();
		}else if(event.getComponentId().equals("websugg")) {
			Modal modal = Modal.create("websuggmodal", "Website Suggestion")
					.addComponents(
						Label.of("Title", TextInput.create("webtitle", TextInputStyle.SHORT)
								.setRequiredRange(10, 100)
								.build()),
						Label.of("Description", TextInput.create("webdesc", TextInputStyle.PARAGRAPH)
								.setRequiredRange(50, 1000)
								.setPlaceholder("Explain your suggestion as good as you can. Upload Pictures in it's created thread afterward.")
								.build()),
						Label.of("Why should we add it?", TextInput.create("webwta", TextInputStyle.PARAGRAPH)
								.setRequiredRange(50, 1000)
								.setPlaceholder("Give us a reason, why we should add that suggestion!")
								.build())
					).build();
			event.replyModal(modal).queue();
		}else if(event.getComponentId().equals("miscsugg")) {
			Modal modal = Modal.create("miscsuggmodal", "Other Suggestion")
					.addComponents(
						Label.of("Title", TextInput.create("misctitle", TextInputStyle.SHORT)
								.setRequiredRange(10, 100)
								.build()),
						Label.of("Description", TextInput.create("miscdesc", TextInputStyle.PARAGRAPH)
								.setRequiredRange(50, 1000)
								.setPlaceholder("Explain your suggestion as good as you can. Upload Pictures in it's created thread afterward.")
								.build()),
						Label.of("Why should we add it?", TextInput.create("miscwta", TextInputStyle.PARAGRAPH)
								.setRequiredRange(50, 1000)
								.setPlaceholder("Give us a reason, why we should add that suggestion!")
								.build())
					).build();
			event.replyModal(modal).queue();
		}else if(event.getComponentId().equals("suggaccept")) {
			long msgId = event.getMessageIdLong();
			if(isSuggestionMessage(msgId)) {
				if(event.getMember().getRoles().contains(upperStaff)) {
					MessageEmbed me = event.getMessage().getEmbeds().get(0);
					EmbedBuilder eb = new EmbedBuilder(me);
					eb.addField("This Suggestion has been accepted!", "Thanks for discussing and voting!", false);
					eb.setTitle("Accepted Suggestion by " + event.getMember().getEffectiveName());
					event.getMessage().editMessageEmbeds(eb.build()).queue();
					event.getMessage().editMessageComponents().queue();
					event.getMessage().getStartedThread().getManager().setArchived(true).setLocked(true).queue();
					event.deferReply(true).addContent("The suggestion has been marked as accepted.").queue();
				}else {
					event.deferReply(true).addContent("Hey, it seems you are lacking the permission to mark suggestions as accepted.").queue();
				}
			}else {
				event.deferReply(true).addContent("An error occured whilst pressing Accept!").queue();
			}
		}else if(event.getComponentId().equalsIgnoreCase("suggreject")) {
			long msgId = event.getMessageIdLong();
			if(isSuggestionMessage(msgId)) {
				if(event.getMember().getRoles().contains(upperStaff)) {
					Modal modal = Modal.create("suggrejectmodal", "Reject Suggestion")
							.addComponents(
									Label.of("Reason for Rejection", TextInput.create("rejsuggreason", TextInputStyle.PARAGRAPH)
											.setRequiredRange(10, 256)
											.setPlaceholder("Please give a reason why you reject this suggestion.")
											.build())
							).build();
					event.replyModal(modal).queue();
					suggestionMsgId.put(event.getMember(), msgId);
				}else {
					event.deferReply(true).addContent("Hey, it seems you are lacking the permission to mark suggestions as rejected.").queue();
				}
			}else {
				event.deferReply(true).addContent("An error occured whilst pressing Accept!").queue();
			}
		}else if(event.getComponentId().equals("suggLockClose")) {
			long msgId = event.getMessageIdLong();
			if(isSuggestionMessage(msgId)) {
				if(event.getMember().getRoles().contains(upperStaff)) {
					Message msg = event.getMessage();
					EmbedBuilder eb = new EmbedBuilder(msg.getEmbeds().get(0));
					eb.addField("This Suggestion has been closed!", "Thanks for discussing and voting! The suggestion is now locked and closed.", false);
					eb.setTitle("Closed Suggestion by " + event.getMember().getEffectiveName());
					msg.editMessageEmbeds(eb.build()).queue();
					msg.editMessageComponents().queue();
					event.getMessage().getStartedThread().getManager().setArchived(true).setLocked(true).queue();
					event.deferReply(true).addContent("The suggestion has been locked and closed.").queue();
				}else {
					event.deferReply(true).addContent("Hey, it seems you are lacking the permission to lock suggestions.").queue();
				}
			}else {
				event.deferReply(true).addContent("An error occured whilst pressing Lock/Close!").queue();
			}
		}
	}
	
	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		TextChannel target = null;
		if(event.getModalId().equals("gamesuggmodal")) {
			try {
				YamlFile cfg = YamlFile.loadConfiguration(LotusManager.mainConfig);
				target = guild.getTextChannelById(cfg.getLong("Suggestion.PostChannel"));
			} catch (IOException e) {
				e.printStackTrace(); 
			}
			if(target != null) {
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(Color.decode("#fe779a"));
				eb.setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl());
				eb.setTitle("Game Suggestion");
				eb.setFooter("Suggestion System by Lotus Gaming", guild.getIconUrl());
				eb.addField("Suggestion Title", event.getValue("gametitle").getAsString(), true);
				eb.addField("Suggestion Description", event.getValue("gamedesc").getAsString(), false);
				eb.addField("Why we should add this", event.getValue("gamewta").getAsString(), false);
				MessageCreateBuilder mcb = new MessageCreateBuilder();
				mcb.setEmbeds(eb.build());
				mcb.setComponents(ActionRow.of(
						Button.primary("suggaccept", "Accept").withEmoji(Emoji.fromFormatted("<:accept:1204482009355911168>")),
						Button.danger("suggreject", "Reject").withEmoji(Emoji.fromFormatted("<:deny:1204482005065146428>")),
						Button.danger("suggLockClose", "Close").withEmoji(Emoji.fromFormatted("U+1F512"))
						));
				target.sendMessage(mcb.build()).queue(ra -> {
					ra.addReaction(Emoji.fromFormatted("<:plus:1204436968268767262>")).queue();
					ra.addReaction(Emoji.fromFormatted("<:minus:1204436966276603934>")).queue();
					long messageId = ra.getIdLong();
					ra.createThreadChannel(event.getValue("gametitle").getAsString()).queue(ra1 -> {
						addSuggestion(member.getIdLong(), ra1.getIdLong(), messageId, event.getValue("gametitle").getAsString(), event.getValue("gamedesc").getAsString(), event.getValue("gamewta").getAsString(), "Game Suggestion");
					});
				});
				event.deferReply(true).addContent("Thank you for your suggestion.").queue();
			}else {
				event.deferReply(true).addContent("It seems there is an error! Please contact the Server Administrator via Ticket!").queue();
			}
		}else if(event.getModalId().equals("botsuggmodal")) {
			try {
				YamlFile cfg = YamlFile.loadConfiguration(LotusManager.mainConfig);
				target = guild.getTextChannelById(cfg.getLong("Suggestion.PostChannel"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(target != null) {
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(Color.decode("#fe779a"));
				eb.setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl());
				eb.setTitle("Bot Suggestion");
				eb.setFooter("Suggestion System by Lotus Gaming", guild.getIconUrl());
				eb.addField("Suggestion Title", event.getValue("bottitle").getAsString(), true);
				eb.addField("Suggestion Description", event.getValue("botdesc").getAsString(), false);
				eb.addField("Why we should add this", event.getValue("botwta").getAsString(), false);
				MessageCreateBuilder mcb = new MessageCreateBuilder();
				mcb.setEmbeds(eb.build());
				mcb.setComponents(ActionRow.of(
						Button.primary("suggaccept", "Accept").withEmoji(Emoji.fromFormatted("<:accept:1204482009355911168>")),
						Button.danger("suggreject", "Reject").withEmoji(Emoji.fromFormatted("<:deny:1204482005065146428>")),
						Button.danger("suggLockClose", "Close").withEmoji(Emoji.fromFormatted("U+1F512"))
						));

				target.sendMessage(mcb.build()).queue(ra -> {
					ra.addReaction(Emoji.fromFormatted("<:plus:1204436968268767262>")).queue();
					ra.addReaction(Emoji.fromFormatted("<:minus:1204436966276603934>")).queue();
					long messageId = ra.getIdLong();
					ra.createThreadChannel(event.getValue("bottitle").getAsString()).queue(ra1 -> {
						addSuggestion(member.getIdLong(), ra1.getIdLong(), messageId, event.getValue("bottitle").getAsString(), event.getValue("botdesc").getAsString(), event.getValue("botwta").getAsString(), "Bot Suggestion");
					});
				});
				event.deferReply(true).addContent("Thank you for your suggestion.").queue();
			}else {
				event.deferReply(true).addContent("It seems there is an error! Please contact the Server Administrator via Ticket!").queue();
			}
		}else if(event.getModalId().equals("websuggmodal")) {
			try {
				YamlFile cfg = YamlFile.loadConfiguration(LotusManager.mainConfig);
				target = guild.getTextChannelById(cfg.getLong("Suggestion.PostChannel"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(target != null) {
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(Color.decode("#fe779a"));
				eb.setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl());
				eb.setTitle("Website Suggestion");
				eb.setFooter("Suggestion System by Lotus Gaming", guild.getIconUrl());
				eb.addField("Suggestion Title", event.getValue("webtitle").getAsString(), true);
				eb.addField("Suggestion Description", event.getValue("webdesc").getAsString(), false);
				eb.addField("Why we should add this", event.getValue("webwta").getAsString(), false);
				MessageCreateBuilder mcb = new MessageCreateBuilder();
				mcb.setEmbeds(eb.build());
				mcb.setComponents(ActionRow.of(
						Button.primary("suggaccept", "Accept").withEmoji(Emoji.fromFormatted("<:accept:1204482009355911168>")),
						Button.danger("suggreject", "Reject").withEmoji(Emoji.fromFormatted("<:deny:1204482005065146428>")),
						Button.danger("suggLockClose", "Close").withEmoji(Emoji.fromFormatted("U+1F512"))
						));
				target.sendMessage(mcb.build()).queue(ra -> {
					ra.addReaction(Emoji.fromFormatted("<:plus:1204436968268767262>")).queue();
					ra.addReaction(Emoji.fromFormatted("<:minus:1204436966276603934>")).queue();
					long messageId = ra.getIdLong();
					ra.createThreadChannel(event.getValue("webtitle").getAsString()).queue(ra1 -> {
						addSuggestion(member.getIdLong(), ra1.getIdLong(), messageId, event.getValue("webtitle").getAsString(), event.getValue("webdesc").getAsString(), event.getValue("webwta").getAsString(), "Website Suggestion");
					});
				});
				event.deferReply(true).addContent("Thank you for your suggestion.").queue();
			}else {
				event.deferReply(true).addContent("It seems there is an error! Please contact the Server Administrator via Ticket!").queue();
			}
		}else if(event.getModalId().equals("miscsuggmodal")) {
			try {
				YamlFile cfg = YamlFile.loadConfiguration(LotusManager.mainConfig);
				target = guild.getTextChannelById(cfg.getLong("Suggestion.PostChannel"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(target != null) {
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(Color.decode("#fe779a"));
				eb.setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl());
				eb.setTitle("Other Suggestion");
				eb.setFooter("Suggestion System by Lotus Gaming", guild.getIconUrl());
				eb.addField("Suggestion Title", event.getValue("misctitle").getAsString(), true);
				eb.addField("Suggestion Description", event.getValue("miscdesc").getAsString(), false);
				eb.addField("Why we should add this", event.getValue("miscwta").getAsString(), false);
				MessageCreateBuilder mcb = new MessageCreateBuilder();
				mcb.setEmbeds(eb.build());
				mcb.setComponents(ActionRow.of(
						Button.primary("suggaccept", "Accept").withEmoji(Emoji.fromFormatted("<:accept:1204482009355911168>")),
						Button.danger("suggreject", "Reject").withEmoji(Emoji.fromFormatted("<:deny:1204482005065146428>")),
						Button.danger("suggLockClose", "Close").withEmoji(Emoji.fromFormatted("U+1F512"))
						));
				target.sendMessage(mcb.build()).queue(ra -> {
					ra.addReaction(Emoji.fromFormatted("<:plus:1204436968268767262>")).queue();
					ra.addReaction(Emoji.fromFormatted("<:minus:1204436966276603934>")).queue();
					long messageId = ra.getIdLong();
					ra.createThreadChannel(event.getValue("misctitle").getAsString()).queue(ra1 -> {
						addSuggestion(member.getIdLong(), ra1.getIdLong(), messageId, event.getValue("misctitle").getAsString(), event.getValue("miscdesc").getAsString(), event.getValue("miscwta").getAsString(), "Other Suggestion");
					});
				});
				event.deferReply(true).addContent("Thank you for your suggestion.").queue();
			}else {
				event.deferReply(true).addContent("It seems there is an error! Please contact the Server Administrator via Ticket!").queue();
			}
		}else if(event.getModalId().equals("suggrejectmodal")) {
			if(suggestionMsgId.containsKey(member)) {
				long msgId = suggestionMsgId.get(member);
				if(isSuggestionMessage(msgId)) {
					Message msg = event.getChannel().retrieveMessageById(msgId).complete();
					MessageEmbed me = msg.getEmbeds().get(0);
					EmbedBuilder eb = new EmbedBuilder(me);
					String reason = event.getValue("rejsuggreason").getAsString();
					eb.addField("This Suggestion has been rejected.", "Thank you still for feedback and voting! The reason for rejection is: " + reason, false);
					eb.setTitle("Rejected Suggestion by " + event.getMember().getEffectiveName());
					msg.editMessageEmbeds(eb.build()).queue();
					msg.getStartedThread().getManager().setArchived(true).setLocked(true).queue();
					msg.editMessageComponents().queue();
					event.deferReply(true).addContent("The suggestion has been marked as rejected.").queue();
				}else {
					event.deferReply(true).addContent("An error occured whilst rejecting the suggestion!").queue();
				}
			}else {
				event.deferReply(true).addContent("An error occured whilst rejecting the suggestion!").queue();
			}
			suggestionMsgId.remove(member);
		}
	}
	
	boolean isSuggestionMessage(long messageId) {
		boolean isSuggestionMessage = false;
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT messageId FROM bot_s_suggestions WHERE messageId = ?");
			ps.setLong(1, messageId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				isSuggestionMessage = true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isSuggestionMessage;
	}
	
	void addSuggestion(long creatorId, long threadId, long messageId, String suggestionTitle, String suggestionDescription, String suggestionWhyToAdd, String topic) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("INSERT INTO bot_s_suggestions(creatorid,createdAt,threadId,suggTitle,suggDesc,suggWTA,topic, messageId) VALUES (?,?,?,?,?,?,?,?)");
			ps.setLong(1, creatorId);
			ps.setLong(2, System.currentTimeMillis());
			ps.setLong(3, threadId);
			ps.setString(4, suggestionTitle);
			ps.setString(5, suggestionDescription);
			ps.setString(6, suggestionWhyToAdd);
			ps.setString(7, topic);
			ps.setLong(8, messageId);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}