package me.sshcrack.texting_colonists;

import com.mojang.logging.LogUtils;
import me.sshcrack.mc_talking.ConversationManager;
import me.sshcrack.mc_talking.manager.GeminiWsClient;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

/*? if forge {*/
/*import net.minecraftforge.event.ServerChatEvent;
 *//*?}*/
/*? if forge {*/
/*import net.minecraftforge.eventbus.api.SubscribeEvent;
 *//*?}*/
/*? if neoforge {*/
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.bus.api.SubscribeEvent;
/*?}*/

public class ChatEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player == null) return;

        var uuid = player.getUUID();
        if (!ConversationManager.isPlayerInConversation(uuid)) return;

        var citizen = ConversationManager.getActiveEntityForPlayer(uuid);
        if (citizen == null) return;

        var client = ConversationManager.getClientForEntity(citizen.getUUID());
        if (client == null) return;

        String message = event.getRawText();

        LOGGER.info("Forwarding chat message to citizen: {}", message);
        client.addPromptTextImmediate(message);

        event.setCanceled(true);
    }
}
