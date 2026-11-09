package gay.wumpus.observatory.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class Ping {

    private Ping() {}

    public static LiteralCommandNode<CommandSourceStack> create() {
        return Commands.literal("ping")
                // /ping
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    Entity executor = ctx.getSource().getExecutor();

                    if (!(executor instanceof Player)) {
                        sender.sendPlainMessage("Pong!");
                        return Command.SINGLE_SUCCESS;
                    }

                    executor.sendRichMessage("<yellow>Pong!");
                    return Command.SINGLE_SUCCESS;
                })
                // /ping <player>
                .then(Commands.argument("target", ArgumentTypes.player())
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        Entity executor = ctx.getSource().getExecutor();
                        final PlayerSelectorArgumentResolver resolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                        final Player target = resolver.resolve(ctx.getSource()).getFirst();

                        if (!(executor instanceof Player)) {
                            sender.sendPlainMessage(target.getName() + " has a ping of " + target.getPing() + "ms.");
                            return Command.SINGLE_SUCCESS;
                        }

                        sender.sendRichMessage("<yellow><player_name> has a ping of <aqua><ping><yellow>ms.",
                                Placeholder.component("player_name", target.displayName()),
                                Placeholder.unparsed("ping", String.valueOf(target.getPing()))
                        );

                        return Command.SINGLE_SUCCESS;
                    }))
                .build();
    }
}
