package me.mjdawson.mcdiscordlink;

import java.awt.Color;
import java.io.*;
import java.nio.file.*;
import java.util.*;

//Even tho it's grey you must not remove this it's an IDE error
import java.util.logging.LogRecord;
import java.util.logging.Logger;
//I SAID DON'T

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;



public class McDiscordLink extends JavaPlugin implements Listener {
    private static McDiscordLink instance;
    private JDA jda;
    private String channelId;
    boolean botEnabled = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        getServer().getPluginManager().registerEvents(this, this);

        //Bot
        String token = this.getConfig().getString("botToken");

        channelId = this.getConfig().getString("channelId");

        if (!Objects.equals(token, "your bot token")) {
            JDABuilder jdaBuilder = JDABuilder.createDefault(token);
            jdaBuilder.enableIntents(GatewayIntent.GUILD_PRESENCES);
            jdaBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
            jda = jdaBuilder.addEventListeners(new MessageListener(channelId)).build();
            String status = this.getConfig().getString("botStatus");
            if (!Objects.equals(status, "")) {
                try {
                    jda.awaitReady();
                    Bukkit.getLogger().info("Your discord bot is now online!");
                    jda.getPresence().setActivity(Activity.playing(status));
                    botEnabled = true;
                    //Say server started
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setDescription("Server started!");
                    embed.setColor(Color.green);
                    jda.getTextChannelById(channelId)
                            .sendMessage("").setEmbeds(embed.build()).queue();
                } catch (InterruptedException e) {
                    Bukkit.getLogger().warning("Invalid Bot Token or other error!");
                    botEnabled = false;
                }
            } else {
                Bukkit.getLogger().warning("There is no status set, to prevent errors your bot has bot started!");
            }
        }

    }


    @Override
    public void onDisable() {
        if (botEnabled == true) {
            //Say server stopped
            EmbedBuilder embed = new EmbedBuilder();
            embed.setDescription("Server stopped!");
            embed.setColor(Color.red);
            jda.getTextChannelById(channelId)
                    .sendMessage("").setEmbeds(embed.build()).queue();
            jda.shutdown();
            Bukkit.getLogger().info("Your discord bot has shutdown!");
        }
        Bukkit.getLogger().warning("Please don't reload McDiscordLink!");
        Bukkit.getLogger().warning("If your server freezes please restart your server!");

    }


    //Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("status")) {
            if (botEnabled == true) {
                if (args.length == 0) {
                    sender.sendMessage("You must provide a status message!");
                    return true;
                }

                String status = String.join(" ", args);
                jda.getPresence().setActivity(Activity.playing(status));
                this.getConfig().set("botStatus", "'" +status+ "'");
                this.saveConfig();

                sender.sendMessage("Successfully set Discord bots status to: " + status);
                return true;
            } else {
                sender.sendMessage("Your bot is offline!");
            }
        }

        return false;
    }

    public static void procedure(String procedure, List procedureArgs) throws Exception {
    }

    public static Object function(String function, List functionArgs) throws Exception {
        return null;
    }

    public static List createList(Object obj) {
        if (obj instanceof List) {
            return (List) obj;
        }
        List list = new ArrayList<>();
        if (obj.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                list.add(java.lang.reflect.Array.get(obj, i));
            }
        } else if (obj instanceof Collection<?>) {
            list.addAll((Collection<?>) obj);
        } else if (obj instanceof Iterator) {
            ((Iterator<?>) obj).forEachRemaining(list::add);
        } else {
            list.add(obj);
        }
        return list;
    }

    public static void createResourceFile(String path) {
        Path file = getInstance().getDataFolder().toPath().resolve(path);
        if (Files.notExists(file)) {
            try (InputStream inputStream = McDiscordLink.class.getResourceAsStream("/" + path)) {
                Files.createDirectories(file.getParent());
                Files.copy(inputStream, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Discord Bot listener
    private class MessageListener extends ListenerAdapter {
        private String channelId;


        public MessageListener(String channelId) {
            this.channelId = channelId;
        }

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (botEnabled == true) {
                Message message = event.getMessage();
                MessageChannel channel = message.getChannel();
                User author = message.getAuthor();

                // Check if the message was sent by a user (not a bot or webhook) & is in the correct channel
                if (!author.isBot() && channel.getId().equals(channelId)) {
                    String serverMessage = (author.getName() + " > " + message.getContentDisplay());
                    Bukkit.broadcastMessage(serverMessage);
                }
            }
        }
    }
    //Fixes server reloads
    @EventHandler
    public void onReload(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().split(" ")[0];
        if (command.equalsIgnoreCase("/reload")) {
            Bukkit.getServer().reload();
        }
    }


    //Events
    //Minecraft chat listener
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (botEnabled == true) {
            // Get the message and sender's name from the chat event
            String message = event.getMessage();
            String sender = event.getPlayer().getName();

            // Create a new embed builder
            EmbedBuilder embed = new EmbedBuilder();
            // Set the embed title to the sender's name
            embed.setTitle(sender);
            // Set the embed description to the message
            embed.setDescription(message);
            //Set the colour
            embed.setColor(Color.green);

            // Use the Discord bot to send the embed to the specified channel
            jda.getTextChannelById(channelId)
                    .sendMessage("").setEmbeds(embed.build()).queue();
        }
    }


    //Minecraft join listener
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (botEnabled == true) {

            // Get the players name from the event
            String player = event.getPlayer().getName();

            // Create a new embed builder
            EmbedBuilder embed = new EmbedBuilder();

            // Set the embed description to the message
            embed.setDescription(player + " joined the game!");

            //Set the colour
            embed.setColor(Color.yellow);

            // Use the Discord bot to send the embed to the specified channel
            jda.getTextChannelById(channelId)
                    .sendMessage("").setEmbeds(embed.build()).queue();
        }
    }


    //Minecraft leave listener
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (botEnabled == true) {

            // Get the players name from the event
            String player = event.getPlayer().getName();

            // Create a new embed builder
            EmbedBuilder embed = new EmbedBuilder();

            // Set the embed description to the message
            embed.setDescription(player + " left the game!");

            //Set the colour
            embed.setColor(Color.yellow);

            // Use the Discord bot to send the embed to the specified channel
            jda.getTextChannelById(channelId)
                    .sendMessage("").setEmbeds(embed.build()).queue();
        }
    }

    //Minecraft death listener
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (botEnabled == true) {

            // Get the death message from the event
            String deathMessage = event.getDeathMessage();

            // Create a new embed builder
            EmbedBuilder embed = new EmbedBuilder();

            // Set the embed description to the message
            embed.setDescription(deathMessage);

            //Set the colour
            embed.setColor(Color.red);

            // Use the Discord bot to send the embed to the specified channel
            jda.getTextChannelById(channelId)
                    .sendMessage("").setEmbeds(embed.build()).queue();
        }
    }


    //Minecraft achievement listener
    @EventHandler
    public void onPlayerAchievement(PlayerAdvancementDoneEvent event) throws Exception {
        if (botEnabled == true) {

            // Get the players name from the event
            String player = event.getPlayer().getName();

            // Get the achievement key from the event
            String advancementKey = String.valueOf(event.getAdvancement().getKey());

            //Stops it running if it is a recipe
            if (!advancementKey.contains("minecraft:recipes/")) {

                //Work out the achievement

                // Get the index of the "/" character
                int index = advancementKey.indexOf("/");

                // Use the index to extract the part of the string after the "/" character
                String advancementName = advancementKey.substring(index + 1);

                //Stops running if it's root
                if (!advancementName.equals("root")) {

                    //Write all advancements out and replace them with the correct thing
                    //This took FOREVER
                    //All info from https://minecraft.fandom.com/wiki/Advancement#Minecraft
                    //Now im going to have to update the plugin every new version until I figure out a more efficient way

                    String advancement = advancementName;

                    advancement = advancement.replace("mine_stone", "Stone Age");
                    advancement = advancement.replace("upgrade_tools", "Getting an Upgrade");
                    advancement = advancement.replace("smelt_iron", "Acquire Hardware");
                    advancement = advancement.replace("obtain_armor", "Suit Up");
                    advancement = advancement.replace("lava_bucket", "Hot Stuff");
                    advancement = advancement.replace("iron_tools", "Isn't It Iron Pick");
                    advancement = advancement.replace("deflect_arrow", "Not Today, Thank You");
                    advancement = advancement.replace("form_obsidian", "Ice Bucket Challenge");
                    advancement = advancement.replace("mine_diamond", "Diamonds!");
                    advancement = advancement.replace("enter_the_nether", "We Need to Go Deeper");
                    advancement = advancement.replace("shiny_gear", "Cover Me with Diamonds");
                    advancement = advancement.replace("enchant_item", "Enchanter");
                    advancement = advancement.replace("cure_zombie_villager", "Zombie Doctor");
                    advancement = advancement.replace("follow_ender_eye", "Eye Spy");
                    advancement = advancement.replace("enter_the_end", "The End?");
                    advancement = advancement.replace("enter_nether", "We Need To Go Deeper");
                    advancement = advancement.replace("return_to_sender", "Return to Sender");
                    advancement = advancement.replace("find_bastion", "Those Were the Days");
                    advancement = advancement.replace("obtain_ancient_debris", "Hidden in the Depths");
                    advancement = advancement.replace("fast_travel", "Subspace Bubble");
                    advancement = advancement.replace("find_fortress", "A Terrible Fortress");
                    advancement = advancement.replace("obtain_crying_obsidian", "Who is Cutting Onions?");
                    advancement = advancement.replace("distract_piglin", "Oh Shiny");
                    advancement = advancement.replace("ride_strider", "This Boat Has Legs");
                    advancement = advancement.replace("uneasy_alliance", "Uneasy Alliance");
                    advancement = advancement.replace("loot_bastion", "War Pigs");
                    advancement = advancement.replace("use_lodestone", "Country Lode, Take Me Home");
                    advancement = advancement.replace("netherite_armor", "Cover Me in Debris");
                    advancement = advancement.replace("get_wither_skull", "Spooky Scary Skeleton");
                    advancement = advancement.replace("obtain_blaze_rod", "Into Fire");
                    advancement = advancement.replace("charge_respawn_anchor", "Not Quite \"Nine\" Lives");
                    advancement = advancement.replace("ride_strider_in_overworld_lava", "Feels Like Home");
                    advancement = advancement.replace("explore_nether", "Hot Tourist Destinations");
                    advancement = advancement.replace("summon_wither", "Withering Heights");
                    advancement = advancement.replace("brew_potion", "Local Brewery");
                    advancement = advancement.replace("create_beacon", "Bring Home the Beacon");
                    advancement = advancement.replace("all_potions", "A Furious Cocktail");
                    advancement = advancement.replace("create_full_beacon", "Beaconator");
                    advancement = advancement.replace("all_effects", "How Did We Get Here?");
                    advancement = advancement.replace("kill_dragon", "Free the End");
                    advancement = advancement.replace("enter_end_gateway", "The Next Generation");
                    advancement = advancement.replace("dragon_egg", "Remote Getaway");
                    advancement = advancement.replace("respawn_dragon", "The End... Again..");
                    advancement = advancement.replace("dragon_breath", "You Need a Mint");
                    advancement = advancement.replace("find_end_city", "The City at the End of the Game");
                    advancement = advancement.replace("elytra", "Sky's the Limit");
                    advancement = advancement.replace("levitate", "Great View From Up Here");
                    advancement = advancement.replace("voluntary_exile", "Voluntary Exile");
                    advancement = advancement.replace("spyglass_at_parrot", "Is It a Bird?");
                    advancement = advancement.replace("kill_a_mob", "Monster Hunter");
                    advancement = advancement.replace("trade", "What a Deal!");
                    advancement = advancement.replace("honey_block_slide", "Sticky Situation");
                    advancement = advancement.replace("ol_betsy", "Ol' Betsy");
                    advancement = advancement.replace("lightning_rod_with_villager_no_fire", "Surge Protector");
                    advancement = advancement.replace("fall_from_world_height", "Caves & Cliffs");
                    advancement = advancement.replace("avoid_vibration", "Sneak 100");
                    advancement = advancement.replace("sleep_in_bed", "Sweet Dreams");
                    advancement = advancement.replace("hero_of_the_village", "Hero of the Village");
                    advancement = advancement.replace("spyglass_at_ghast", "Is It a Balloon?");
                    advancement = advancement.replace("throw_trident", "A Throwaway Joke");
                    advancement = advancement.replace("kill_mob_near_sculk_catalyst", "It Spreads");
                    advancement = advancement.replace("shoot_arrow", "Take Aim");
                    advancement = advancement.replace("kill_all_mobs", "Monsters Hunted");
                    advancement = advancement.replace("totem_of_undying", "Postmortal");
                    advancement = advancement.replace("summon_iron_golem", "Hired Help");
                    advancement = advancement.replace("trade_at_world_height", "Star Trader");
                    advancement = advancement.replace("two_birds_one_arrow", "Two Birds, One Arrow");
                    advancement = advancement.replace("whos_the_pillager_now", "Who's the Pillager Now?");
                    advancement = advancement.replace("arbalistic", "Arbalistic");
                    advancement = advancement.replace("adventuring_time", "Adventuring Time");
                    advancement = advancement.replace("play_jukebox_in_meadows", "Sound of Music");
                    advancement = advancement.replace("walk_on_powder_snow_with_leather_boots", "Light as a Rabbit");
                    advancement = advancement.replace("spyglass_at_dragon", "Is It a Plane?");
                    advancement = advancement.replace("very_very_frightening", "Very Very Frightening");
                    advancement = advancement.replace("sniper_duel", "Sniper Duel");
                    advancement = advancement.replace("bullseye", "Bullseye");
                    advancement = advancement.replace("safely_harvest_honey", "Bee Our Guest");
                    advancement = advancement.replace("breed_an_animal", "The Parrots and the Bats");
                    advancement = advancement.replace("allay_deliver_item_to_player", "You've Got a Friend in Me");
                    advancement = advancement.replace("ride_a_boat_with_a_goat", "Whatever Floats Your Goat!");
                    advancement = advancement.replace("tame_an_animal", "Best Friends Forever");
                    advancement = advancement.replace("make_a_sign_glow", "Glow and Behold!");
                    advancement = advancement.replace("fishy_business", "Fishy Business");
                    advancement = advancement.replace("silk_touch_nest", "Total Beelocation");
                    advancement = advancement.replace("tadpole_in_a_bucket", "Total Beelocation");
                    advancement = advancement.replace("plant_seed", "A Seedy Place");
                    advancement = advancement.replace("wax_on", "Wax On");
                    advancement = advancement.replace("bred_all_animals", "Two by Two");
                    advancement = advancement.replace("allay_deliver_cake_to_note_block", "Birthday Song");
                    advancement = advancement.replace("complete_catalogue", "A Complete Catalogue");
                    advancement = advancement.replace("husbandry/tactical_fishing", "Tactical Fishing");
                    advancement = advancement.replace("leash_all_frog_variants", "When the Squad Hops into Town");
                    advancement = advancement.replace("balanced_diet", "A Balanced Diet");
                    advancement = advancement.replace("obtain_netherite_hoe", "Serious Dedication");
                    advancement = advancement.replace("wax_off", "Wax Off");
                    advancement = advancement.replace("axolotl_in_a_bucket", "The Cutest Predator");
                    advancement = advancement.replace("froglights", "With Our Powers Combined!");
                    advancement = advancement.replace("kill_axolotl_target", "The Healing Power of Friendship!");

                    // Create a new embed builder
                    EmbedBuilder embed = new EmbedBuilder();

                    // Set the embed description to the message
                    embed.setDescription(player + " has completed the achievement " + advancement);

                    //Set the colour
                    embed.setColor(Color.white);

                    // Use the Discord bot to send the embed to the specified channel
                    jda.getTextChannelById(channelId)
                            .sendMessage("").setEmbeds(embed.build()).queue();

                }
            }
        }
    }


    public static McDiscordLink getInstance() {
        return instance;
    }
}
