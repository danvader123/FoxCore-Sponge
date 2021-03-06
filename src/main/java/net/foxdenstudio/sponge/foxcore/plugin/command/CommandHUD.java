package net.foxdenstudio.sponge.foxcore.plugin.command;

import com.google.common.collect.ImmutableList;
import net.foxdenstudio.sponge.foxcore.common.util.CacheMap;
import net.foxdenstudio.sponge.foxcore.plugin.FCConfigManager;
import net.foxdenstudio.sponge.foxcore.plugin.command.util.AdvCmdParser;
import net.foxdenstudio.sponge.foxcore.plugin.util.Aliases;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.GuavaCollectors;
import org.spongepowered.api.util.StartsWithPredicate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by Fox on 4/14/2016.
 */
public class CommandHUD extends FCCommandBase {

    private static final String[] RESET_ALIASES = {"reset", "r", "clear"};

    private static CommandHUD instance;

    private Map<Player, Boolean> isHUDEnabled = new CacheMap<>((k, m) -> FCConfigManager.getInstance().isDefaultHUDOn() &&
            k instanceof Player && testPermission((Player) k));

    public CommandHUD() {
        if (instance == null) instance = this;
    }

    public static CommandHUD instance() {
        return instance;
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        if (!testPermission(source)) {
            source.sendMessage(Text.of(TextColors.RED, "You don't have permission to use this command!"));
            return CommandResult.empty();
        }
        AdvCmdParser.ParseResult parse = AdvCmdParser.builder().arguments(arguments).parse();
        if (source instanceof Player) {
            if (parse.args.length == 0) {
                Text.Builder builder = Text.builder();
                builder.append(Text.of("Your HUD is currently "));
                if (isHUDEnabled.get(source)) builder.append(Text.of(TextColors.GREEN, "on"));
                else builder.append(Text.of(TextColors.RED, "off"));
                builder.append(Text.of(TextColors.RESET, "!"));
                source.sendMessage(builder.build());
            } else {
                if (parse.args[0].equalsIgnoreCase("on")) {
                    isHUDEnabled.put((Player) source, true);
                    source.sendMessage(Text.of("Turned ", TextColors.GREEN, "on", TextColors.RESET, " the HUD!"));
                } else if (parse.args[0].equalsIgnoreCase("off")) {
                    isHUDEnabled.put((Player) source, false);
                    Optional<Scoreboard> serverScoreboard = Sponge.getServer().getServerScoreboard();
                    if (serverScoreboard.isPresent()) {
                        ((Player) source).setScoreboard(serverScoreboard.get());
                    } else {
                        ((Player) source).setScoreboard(Scoreboard.builder().build());
                    }
                    source.sendMessage(Text.of("Turned ", TextColors.RED, "off", TextColors.RESET, " the HUD!"));
                } else if (Aliases.isIn(RESET_ALIASES, parse.args[0])) {
                    Optional<Scoreboard> serverScoreboard = Sponge.getServer().getServerScoreboard();
                    if (serverScoreboard.isPresent()) {
                        ((Player) source).setScoreboard(serverScoreboard.get());
                    } else {
                        ((Player) source).setScoreboard(Scoreboard.builder().build());
                    }
                    source.sendMessage(Text.of("HUD reset!"));
                } else throw new CommandException(Text.of("Not a valid HUD command!"));
            }
        } else source.sendMessage(Text.of("HUD controls are only available for players!"));
        return CommandResult.empty();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
        if (!testPermission(source)) return ImmutableList.of();
        AdvCmdParser.ParseResult parse = AdvCmdParser.builder()
                .arguments(arguments)
                .autoCloseQuotes(true)
                .excludeCurrent(true)
                .parse();
        if (parse.current.type == AdvCmdParser.CurrentElement.ElementType.ARGUMENT) {
            if (parse.current.index == 0) {
                return Stream.of("on", "off", "reset")
                        .filter(new StartsWithPredicate(parse.current.token))
                        .map(args -> parse.current.prefix + args)
                        .collect(GuavaCollectors.toImmutableList());
            }
        } else if (parse.current.type.equals(AdvCmdParser.CurrentElement.ElementType.COMPLETE))
            return ImmutableList.of(parse.current.prefix + " ");
        return ImmutableList.of();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission("foxcore.hud");
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("Command for enabling and disabling the scoreboard HUD."));
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.of(Text.of("Command for enabling and disabling the scoreboard HUD.\n" +
                "\"on\" enables the scoreboard.\n" +
                "\"off\" disables the scoreboard.\n" +
                "\"reset\" will clear the current scoreboard, but will not disable the scoreboard for the plugin."));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("hud [on|off|reset]");
    }

    public Map<Player, Boolean> getIsHUDEnabled() {
        return isHUDEnabled;
    }
}
