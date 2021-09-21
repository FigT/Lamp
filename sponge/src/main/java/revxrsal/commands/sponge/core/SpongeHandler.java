package revxrsal.commands.sponge.core;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.world.World;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.BaseCommandHandler;
import revxrsal.commands.sponge.SpongeCommandActor;
import revxrsal.commands.sponge.SpongeCommandHandler;
import revxrsal.commands.sponge.exception.InvalidPlayerException;
import revxrsal.commands.sponge.exception.SpongeExceptionAdapter;

import static revxrsal.commands.util.Preconditions.notNull;

public class SpongeHandler extends BaseCommandHandler implements SpongeCommandHandler {

    private final Object plugin;

    public SpongeHandler(Object plugin) {
        super();
        this.plugin = notNull(plugin, "plugin");
        registerSenderResolver(SpongeSenderResolver.INSTANCE);
        registerContextValue((Class) plugin.getClass(), plugin);
        registerDependency((Class) plugin.getClass(), plugin);
        registerContextValue(Game.class, Sponge.getGame());
        registerContextValue(Server.class, Sponge.getServer());
        registerContextValue(Scheduler.class, Sponge.getScheduler());
        registerContextValue(Platform.class, Sponge.getPlatform());
        registerValueResolver(Selector.class, (arguments, actor, parameter, command) -> Selector.parse(arguments.pop()));
        registerValueResolver(Player.class, (arguments, actor, parameter, command) -> {
            String name = arguments.pop();
            if (name.equalsIgnoreCase("me") || name.equalsIgnoreCase("self"))
                return actor.as(SpongeCommandActor.class).requirePlayer();
            return Sponge.getServer().getPlayer(name)
                    .orElseThrow(() -> new InvalidPlayerException(parameter, name));
        });
        registerValueResolver(World.class, (arguments, actor, parameter, command) -> {
            String name = arguments.pop();
            if (name.equalsIgnoreCase("me") || name.equalsIgnoreCase("self"))
                return actor.as(SpongeCommandActor.class).requirePlayer().getWorld();
            return Sponge.getServer().getWorld(name)
                    .orElseThrow(() -> new InvalidPlayerException(parameter, name));
        });
        getAutoCompleter()
                .registerSuggestion("players", SuggestionProvider.map(Sponge.getServer()::getOnlinePlayers, Player::getName))
                .registerSuggestion("worlds", SuggestionProvider.map(Sponge.getServer()::getWorlds, World::getName))
                .registerParameterSuggestions(Player.class, "players")
                .registerParameterSuggestions(World.class, "worlds");
        registerResponseHandler(String.class, (response, actor, command) -> actor.as(SpongeCommandActor.class).getSource().sendMessage(Text.of(response)));
        registerResponseHandler(Text.class, (response, actor, command) -> actor.as(SpongeCommandActor.class).getSource().sendMessage(response));
        registerResponseHandler(Text[].class, (response, actor, command) -> actor.as(SpongeCommandActor.class).getSource().sendMessages(response));
        setExceptionHandler(SpongeExceptionAdapter.INSTANCE);
    }

    @Override public CommandHandler register(@NotNull Object... commands) {
        super.register(commands);
        for (ExecutableCommand command : registration.getExecutables().values()) {
            if (command.getParent() != null) continue;
            createPluginCommand(command.getName());
        }
        for (CommandCategory category : registration.getSubcategories().values()) {
            if (category.getParent() != null) continue;
            createPluginCommand(category.getName());
        }
        return this;
    }

    private void createPluginCommand(String name) {
        CommandCallable command = new SpongeCommandCallable(this);
        Sponge.getCommandManager().register(name, command);
    }

    @Override public @NotNull Object getPlugin() {
        return plugin;
    }
}
