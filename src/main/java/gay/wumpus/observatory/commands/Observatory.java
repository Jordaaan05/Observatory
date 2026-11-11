package gay.wumpus.observatory.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;

public class Observatory {

    private Observatory() {}

    public static LiteralCommandNode<CommandSourceStack> create() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("observatory");
        LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("reload");

        reload
                // execute all at some point

                // reload messages
                .then(Commands.literal("messages")
                        .executes(context -> {
                            boolean success = JavaPlugin.getPlugin(gay.wumpus.observatory.Observatory.class).reloadMessages();
                            context.getSource().getSender().sendMessage(
                                    Component.text(success ? "Messages reloaded." : "Reload failed, check console.")
                            );
                            return Command.SINGLE_SUCCESS;
                        })
                )

                // reload config
                .then(Commands.literal("config")
                    .executes(context -> {
                    boolean success = JavaPlugin.getPlugin(gay.wumpus.observatory.Observatory.class).safeReloadConfig();
                    context.getSource().getSender().sendMessage(
                            Component.text(success ? "Config reloaded." : "Config reload failed, check console.")
                    );
                    return Command.SINGLE_SUCCESS;
                })
        );

        root.then(reload);

        return root.build();
    }
}
