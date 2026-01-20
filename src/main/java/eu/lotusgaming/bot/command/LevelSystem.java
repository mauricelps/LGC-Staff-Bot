//Created by Maurice H. at 21.09.2024
package eu.lotusgaming.bot.command;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import eu.lotusgaming.bot.handlers.modlog.ModlogController;
import eu.lotusgaming.bot.main.LotusManager;
import eu.lotusgaming.bot.misc.MySQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.utils.FileUpload;

public class LevelSystem extends ListenerAdapter {
	
	private final int basePoints = 1;
	private final int pointsPerAttachment = 2;
	private final int pointsPerKeyword = 2;
	private final int spamThresholdMilliseconds = 2000;
	private final List<String> bonusKeywords = new ArrayList<>();
	private final Map<Long, Long> userLastMessageTime = new HashMap<>();
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		
		if(event.getName().equals("leaderboard")) {
			int topCount = 10;
			if(event.getOption("top") != null) {
				topCount = event.getOption("top").getAsInt();
				if(topCount >= 26) {
					topCount = 25;
				}
			}
			
			StringBuilder sb = new StringBuilder();
			int i = 0;
			
			try {
				PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM bot_s_chatlevel WHERE guildId = ? ORDER by points DESC LIMIT " + topCount);
				ps.setLong(1, guild.getIdLong());
				ResultSet rs = ps.executeQuery();
				sb.append(String.format("%-6s | %-24s | %-6s | %-5s%n", "Rank", "Name", "Points", "Level"));
				sb.append("----------------------------------------------\n");
				while(rs.next()) {
					i++;
					Member cachedMember = guild.getMemberById(rs.getLong("memberId"));
					String memberName = "";
					if(cachedMember == null) {
						memberName = "unknown member / " + rs.getLong("memberId");
					}else {
						memberName = cachedMember.getEffectiveName();
					}
					int level = rs.getInt("level");
					int points = rs.getInt("points");
					sb.append(String.format("%02d.    | %-24s | %-6d | %-5d%n", i, truncate(memberName, 20), points, level));
				}
				sb.append("\nListed the Top " + i + " Chatters from " + guild.getName());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			EmbedBuilder eb = new EmbedBuilder();
			eb.setDescription("```" + sb.toString() + "```");
			eb.setColor(ModlogController.green);
			eb.setTitle("Top " + i + " chatters on " + guild.getName());
			event.replyEmbeds(eb.build()).queue();
		}else if(event.getName().equals("level")) {
			
			
			User target = null;
			Member targetMember = null;
			if(event.getOption("member") != null) {
				target = event.getOption("member").getAsUser();
			}else {
				target = member.getUser();
			}
			if(guild.isMember(target)) {
				targetMember = guild.getMember(target);
			}else {
				event.reply("Sorry, but the member you are looking for is not on this guild.").queue();
				return;
			}
			
			int points = getCurrentPoints(guild, target);
			int currentLevel = getCurrentLevel(guild, target);
			int nextLevel = currentLevel + 1;
			int nextLevelPoints = getPointsForNextLevel(nextLevel);
			int currentLevelPoints = getPointsForCurrentLevel(currentLevel);
			
			int pointsIntoCurrentLevel = points - currentLevelPoints;
			int pointsNeededForNextLevel = nextLevelPoints - currentLevelPoints;
			
			double progressInt = (double) pointsIntoCurrentLevel / pointsNeededForNextLevel * 100;
			
			//BETA 
			try {
				
				BufferedImage levelCard = ImageIO.read(new File(LotusManager.configFolderName + "/assets/templates/light_blue_levelcard.png"));
				BufferedImage avatar = ImageIO.read(new URL(target.getEffectiveAvatarUrl()));
				Graphics2D g2d = levelCard.createGraphics();
				Font font = Font.createFont(Font.TRUETYPE_FONT, new File(LotusManager.configFolderName + "/assets/fonts/ibm_plexsans_reg.ttf"));
				font = font.deriveFont(18F);
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				ge.registerFont(font);
				
				int avatarSize = 128;
				int rounding = 5;
				int avX = 12, avY = 26;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				RoundRectangle2D.Double clip = new RoundRectangle2D.Double(avX, avY, avatarSize, avatarSize, rounding, rounding);
				g2d.setClip(clip);
				g2d.drawImage(avatar, avX, avY, avatarSize, avatarSize, null);
				g2d.setClip(null);
				
				g2d.setFont(font);
				g2d.setColor(Color.black);
				
				{
					//username
					int boxX = 152;
					int boxY = 26; //54
					int boxW = 229;
					int boxH = 29;
					String textToDraw = targetMember.getEffectiveName();
					
					FontMetrics metrics = g2d.getFontMetrics(font);
					int textX = boxX + (boxW - metrics.stringWidth(textToDraw)) / 2;
					int textY = boxY + ((boxH - metrics.getHeight()) / 2) + metrics.getAscent();
					g2d.drawString(textToDraw, textX, textY);
					
					//OLD g2d.drawString(targetMember.getEffectiveName(), 158, 50);
				}
				{
					//level
					int boxX = 152;
					int boxY = 68; //96
					int boxW = 162;
					int boxH = 29;
					String textToDraw = "Level: " + currentLevel;
					
					FontMetrics metrics = g2d.getFontMetrics(font);
					int textX = boxX + (boxW - metrics.stringWidth(textToDraw)) / 2;
					int textY = boxY + ((boxH - metrics.getHeight()) / 2) + metrics.getAscent();
					g2d.drawString(textToDraw, textX, textY);
					//OLD g2d.drawString("Level: " + currentLevel, 158, 93);
				}
				{
					//points
					int boxX = 152;
					int boxY = 105; //132
					int boxW = 162;
					int boxH = 29;
					String textToDraw = "Points: " + points;
					
					FontMetrics metrics = g2d.getFontMetrics(font);
					int textX = boxX + (boxW - metrics.stringWidth(textToDraw)) / 2;
					int textY = boxY + ((boxH - metrics.getHeight()) / 2) + metrics.getAscent();
					g2d.drawString(textToDraw, textX, textY);
					//OLD g2d.drawString("Points: " + points, 158, 129);
				}
				
				int progressBarWidth = 367;
				int progress = (int) ((progressInt / 100) * progressBarWidth);
				
				int progressBarX = 13; //13
				int progressBarY = 195; //195
				int progressBarHeight = 29;
				int cornerRadius = 15;
				
				g2d.setColor(Color.GREEN);
				g2d.fillRoundRect(progressBarX, progressBarY, progress,  progressBarHeight, cornerRadius, cornerRadius);
				
				g2d.setColor(Color.WHITE);
				g2d.drawRoundRect(progressBarX, progressBarY, progressBarWidth, progressBarHeight, cornerRadius, cornerRadius);
				
				g2d.setColor(Color.black);
				
				{
					//points
					int boxX = 13;
					int boxY = 195;
					int boxW = progressBarWidth;
					int boxH = 29;
					String textToDraw = points + "/" + nextLevelPoints;
					
					FontMetrics metrics = g2d.getFontMetrics(font);
					int textX = boxX + (boxW - metrics.stringWidth(textToDraw)) / 2;
					int textY = boxY + ((boxH - metrics.getHeight()) / 2) + metrics.getAscent();
					g2d.drawString(textToDraw, textX, textY);
				}
				{
					//current Level
					int boxX = 13;
					int boxY = 165;
					int boxW = 29;
					int boxH = 29;
					String textToDraw = String.valueOf(currentLevel);
					
					FontMetrics metrics = g2d.getFontMetrics(font);
					int textX = boxX + (boxW - metrics.stringWidth(textToDraw)) / 2;
					int textY = boxY + ((boxH - metrics.getHeight()) / 2) + metrics.getAscent();
					g2d.drawString(textToDraw, textX, textY);
				}
				{
					//next Level
					int boxX = 351;
					int boxY = 165;
					int boxW = 29;
					int boxH = 29;
					String textToDraw = String.valueOf(nextLevel);
					
					FontMetrics metrics = g2d.getFontMetrics(font);
					int textX = boxX + (boxW - metrics.stringWidth(textToDraw)) / 2;
					int textY = boxY + ((boxH - metrics.getHeight()) / 2) + metrics.getAscent();
					g2d.drawString(textToDraw, textX, textY);
				}
				
				g2d.dispose();
				
				File tmpFile = new File(LotusManager.configFolderName + "/tmp/levelcard_" + target.getIdLong() + ".png");
				ImageIO.write(levelCard, "png", tmpFile);
				
				event.replyFiles(FileUpload.fromData(tmpFile)).queue();
				tmpFile.delete();
				
			} catch (IOException | FontFormatException e) {
				e.printStackTrace();
			}
		}else if(event.getName().equals("levelsettings")){
			String scn = event.getSubcommandName();
			if(scn.equals("levelcard")){
				String color = event.getOption("color").getAsString();

			}
		}
	}

	private static final String[] colors = new String[] {
		"Blue", "Crimson Red", "Deep Pink", "Dark Orange", "Gold", "Medium Purple", "Lime", "Cyan", "Sienna Brown", "Gray", "White", "Black"
	};

	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
		if(event.getName().equals("levelsettings")) {
			String scn = event.getSubcommandName();
			if(scn.equals("levelcard") && event.getFocusedOption().getName().equals("color")){
				List<Command.Choice> options = Stream.of(colors)
						.filter(c -> c.toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase()))
						.map(c -> new Command.Choice(c, c))
						.collect(Collectors.toList());
				event.replyChoices(options).queue();
			}
		}else if(event.getName().equals("levelsettings")) {
			String scn = event.getSubcommandName();
			if(scn.equals("levelupcard") && event.getFocusedOption().getName().equals("color")){
				List<Command.Choice> options = Stream.of(colors)
						.filter(c -> c.toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase()))
						.map(c -> new Command.Choice(c, c))
						.collect(Collectors.toList());
				event.replyChoices(options).queue();
			}
		}
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.isFromGuild()) {
			Guild guild = event.getGuild();
			Member member = event.getMember();
			
			if(member == null) return;
			if(member.getUser().isBot()) return;
			if(member.getIdLong() == 1203717266990960760L) return;
			
			addUserIfNotExists(guild, member.getUser());
			
			long currentTime = System.currentTimeMillis();
			if(userLastMessageTime.containsKey(member.getIdLong())) {
				long lastMessageTime = userLastMessageTime.get(member.getIdLong());
				if(currentTime - lastMessageTime < spamThresholdMilliseconds) {
					return;
				}
			}
			userLastMessageTime.put(member.getIdLong(), currentTime);
			
			int points = basePoints;
			int attachmentPoints = pointsPerAttachment * event.getMessage().getAttachments().size();
			int boniKeyWrds = 0;
			for(@SuppressWarnings("unused") String s : bonusKeywords) {
				boniKeyWrds += pointsPerKeyword;
			}
			points = points + attachmentPoints;
			points = points + boniKeyWrds;
			int currentPoints = getCurrentPoints(guild, member.getUser());
			int newPoints = currentPoints + points;
			setNewPoints(guild, member.getUser(), newPoints);
			
			int currentLevel = getCurrentLevel(guild, member.getUser());
			int nextLevelPoints = getPointsForNextLevel(currentLevel + 1);
			if(getCurrentPoints(guild, member.getUser()) >= nextLevelPoints) {
				setNewLevel(guild, member.getUser(), currentLevel + 1);
				
				try {
					BufferedImage levelCard = ImageIO.read(new File(LotusManager.configFolderName + "/assets/templates/purple_levelup_card.png"));
					BufferedImage avatar = ImageIO.read(new URL(member.getEffectiveAvatarUrl()));
					BufferedImage guildIcon = ImageIO.read(new URL(guild.getIconUrl()));
					Graphics2D g2d = levelCard.createGraphics();
					Font font = Font.createFont(Font.TRUETYPE_FONT, new File(LotusManager.configFolderName + "/assets/fonts/ibm_plexsans_reg.ttf"));
					font = font.deriveFont(18F);
					GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
					ge.registerFont(font);
					
					int avatarSize = 128;
					int rounding = 5;
					int avX = 10, avY = 74;
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					RoundRectangle2D.Double clip = new RoundRectangle2D.Double(avX, avY, avatarSize, avatarSize, rounding, rounding);
					g2d.setClip(clip);
					g2d.drawImage(avatar, avX, avY, avatarSize, avatarSize, null);
					
					int giS = 32, giR = 5, giX = 10, giY = 211;
					RoundRectangle2D.Double clip2 = new RoundRectangle2D.Double(giX, giY, giS, giS, giR, giR);
					g2d.setClip(clip2);
					g2d.drawImage(guildIcon, giX, giY, giS, giS, null);
					g2d.setClip(null);
					
					g2d.setFont(font);
					g2d.setColor(Color.black);
					
					{
						//title
						int boxX = 10;
						int boxY = 20;
						int boxW = 380;
						int boxH = 29;
						String textToDraw = member.getEffectiveName() + " has leveled up!";
						
						FontMetrics metrics = g2d.getFontMetrics(font);
						int textX = boxX + (boxW - metrics.stringWidth(textToDraw)) / 2;
						int textY = boxY + ((boxH - metrics.getHeight()) / 2) + metrics.getAscent();
						g2d.drawString(textToDraw, textX, textY);
					}
					{
						//old level
						int boxX = 221;
						int boxY = 116;
						int boxW = 50;
						int boxH = 50;
						String textToDraw = String.valueOf(currentLevel);
						
						FontMetrics metrics = g2d.getFontMetrics(font);
						int textX = boxX + (boxW - metrics.stringWidth(textToDraw)) / 2;
						int textY = boxY + ((boxH - metrics.getHeight()) / 2) + metrics.getAscent();
						g2d.drawString(textToDraw, textX, textY);
					}
					{
						//new level
						int boxX = 307;
						int boxY = 116;
						int boxW = 50;
						int boxH = 50;
						String textToDraw = String.valueOf((currentLevel + 1));
						
						FontMetrics metrics = g2d.getFontMetrics(font);
						int textX = boxX + (boxW - metrics.stringWidth(textToDraw)) / 2;
						int textY = boxY + ((boxH - metrics.getHeight()) / 2) + metrics.getAscent();
						g2d.drawString(textToDraw, textX, textY);
					}
					{
						//guild name
						int boxX = 48;
						int boxY = 211;
						int boxW = 230;
						int boxH = 32;
						String textToDraw = guild.getName();
						
						FontMetrics metrics = g2d.getFontMetrics(font);
						int textX = boxX + (boxW - metrics.stringWidth(textToDraw)) / 2;
						int textY = boxY + ((boxH - metrics.getHeight()) / 2) + metrics.getAscent();
						g2d.drawString(textToDraw, textX, textY);
					}
					
					g2d.dispose();
					
					File tmpFile = new File(LotusManager.configFolderName + "/tmp/levelcard_" + member.getIdLong() + ".png");
					ImageIO.write(levelCard, "png", tmpFile);
					
					event.getChannel().sendFiles(FileUpload.fromData(tmpFile)).queue(rA -> {
						rA.delete().queueAfter(30, TimeUnit.SECONDS);
					});
					tmpFile.delete();
				}catch (Exception e) {
					e.printStackTrace();
				}
				
				
				
			}
		}
	}
	
	private String truncate(String name, int length) {
		if(name.length() > length) {
			return name.substring(0, length - 3) + "...";
		}
		return name;
	}
	
	public void addUserIfNotExists(Guild guild, User member) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM bot_s_chatlevel WHERE guildId = ? AND memberId = ?");
			ps.setLong(1, guild.getIdLong());
			ps.setLong(2, member.getIdLong());
			ResultSet rs = ps.executeQuery();
			if(!rs.next()) {
				PreparedStatement ps1 = MySQL.getConnection().prepareStatement("INSERT INTO bot_s_chatlevel (guildId,memberId,points,level) VALUES (?,?,?,?)");
				ps1.setLong(1, guild.getIdLong());
				ps1.setLong(2, member.getIdLong());
				ps1.setInt(3, 1);
				ps1.setInt(4, 0);
				ps1.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int getCurrentPoints(Guild guild, User member) {
		int points = 0;
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM bot_s_chatlevel WHERE guildId = ? AND memberId = ?");
			ps.setLong(1, guild.getIdLong());
			ps.setLong(2, member.getIdLong());
			ResultSet rs = ps.executeQuery();
			rs.next();
			points = rs.getInt("points");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return points;
	}
	
	public int getCurrentLevel(Guild guild, User member) {
		int level = 0;
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM bot_s_chatlevel WHERE guildId = ? AND memberId = ?");
			ps.setLong(1, guild.getIdLong());
			ps.setLong(2, member.getIdLong());
			ResultSet rs = ps.executeQuery();
			rs.next();
			level = rs.getInt("level");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return level;
	}
	
	public void setNewPoints(Guild guild, User member, int points) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE bot_s_chatlevel SET points = ? WHERE guildId = ? AND memberId = ?");
			ps.setInt(1, points);
			ps.setLong(2, guild.getIdLong());
			ps.setLong(3, member.getIdLong());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void setNewLevel(Guild guild, User member, int level) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE bot_s_chatlevel SET level = ? WHERE guildId = ? AND memberId = ?");
			ps.setInt(1, level);
			ps.setLong(2, guild.getIdLong());
			ps.setLong(3, member.getIdLong());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private enum Card {
		LEVELCARD,
		LEVELUPCARD
	}

	private void setColor(String color, Card card, long userId) {
		try(PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE bot_s_chatlevelUO SET " + (card == Card.LEVELCARD ? "bg_color_levelcard" : "bg_color_levelup") + " = ? WHERE memberId = ?")){
			ps.setString(1, color);
			ps.setLong(2, userId);
			ps.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int getPointsForCurrentLevel(int level) {
		switch(level) {
		case 1: return 10;
		case 2: return 25;
		case 3: return 50;
		case 4: return 75;
		case 5: return 100;
		case 6: return 150;
		case 7: return 200;
		case 8: return 250;
		case 9: return 300;
		case 10: return 350;
		case 11: return 400;
		case 12: return 450;
		case 13: return 500;
		case 14: return 600;
		case 15: return 700;
		case 16: return 800;
		case 17: return 900;
		case 18: return 1000;
		case 19: return 1250;
		case 20: return 1500;
		case 21: return 1750;
		case 22: return 2000;
		case 23: return 2250;
		case 24: return 2500;
		case 25: return 2750;
		case 26: return 3000;
		case 27: return 3250;
		case 28: return 3500;
		case 29: return 3750;
		case 30: return 4000;
		case 31: return 4250;
		case 32: return 4500;
		case 33: return 4750;
		case 34: return 5000;
		case 35: return 5500;
		case 36: return 6000;
		case 37: return 6500;
		case 38: return 7000;
		case 39: return 7500;
		case 40: return 8000;
		case 41: return 8500;
		case 42: return 9000;
		case 43: return 9500;
		case 44: return 10000;
		case 45: return 11000;
		case 46: return 12000;
		case 47: return 13000;
		case 48: return 14000;
		case 49: return 15000;
		case 50: return 16000;
		case 51: return 17000;
		case 52: return 18000;
		case 53: return 19000;
		case 54: return 20000;
		case 55: return 22000;
		case 56: return 24000;
		case 57: return 26000;
		case 58: return 28000;
		case 59: return 30000;
		case 60: return 32500;
		case 61: return 35000;
		case 62: return 37500;
		case 63: return 40000;
		case 64: return 42500;
		case 65: return 45000;
		case 66: return 47500;
		case 67: return 50000;
		case 68: return 55000;
		case 69: return 60000;
		case 70: return 65000;
		case 71: return 70000;
		case 72: return 75000;
		case 73: return 100000;
		case 74: return 125000;
		case 75: return 150000;
		default: return 0;
		}
	}
	
	public int getPointsForNextLevel(int level) {
		switch(level) {
		case 1: return 10;
		case 2: return 25;
		case 3: return 50;
		case 4: return 75;
		case 5: return 100;
		case 6: return 150;
		case 7: return 200;
		case 8: return 250;
		case 9: return 300;
		case 10: return 350;
		case 11: return 400;
		case 12: return 450;
		case 13: return 500;
		case 14: return 600;
		case 15: return 700;
		case 16: return 800;
		case 17: return 900;
		case 18: return 1000;
		case 19: return 1250;
		case 20: return 1500;
		case 21: return 1750;
		case 22: return 2000;
		case 23: return 2250;
		case 24: return 2500;
		case 25: return 2750;
		case 26: return 3000;
		case 27: return 3250;
		case 28: return 3500;
		case 29: return 3750;
		case 30: return 4000;
		case 31: return 4250;
		case 32: return 4500;
		case 33: return 4750;
		case 34: return 5000;
		case 35: return 5500;
		case 36: return 6000;
		case 37: return 6500;
		case 38: return 7000;
		case 39: return 7500;
		case 40: return 8000;
		case 41: return 8500;
		case 42: return 9000;
		case 43: return 9500;
		case 44: return 10000;
		case 45: return 11000;
		case 46: return 12000;
		case 47: return 13000;
		case 48: return 14000;
		case 49: return 15000;
		case 50: return 16000;
		case 51: return 17000;
		case 52: return 18000;
		case 53: return 19000;
		case 54: return 20000;
		case 55: return 22000;
		case 56: return 24000;
		case 57: return 26000;
		case 58: return 28000;
		case 59: return 30000;
		case 60: return 32500;
		case 61: return 35000;
		case 62: return 37500;
		case 63: return 40000;
		case 64: return 42500;
		case 65: return 45000;
		case 66: return 47500;
		case 67: return 50000;
		case 68: return 55000;
		case 69: return 60000;
		case 70: return 65000;
		case 71: return 70000;
		case 72: return 75000;
		case 73: return 100000;
		case 74: return 125000;
		case 75: return 150000;
		default: return 0;
		}
	}
}