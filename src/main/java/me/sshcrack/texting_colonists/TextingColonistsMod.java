package me.sshcrack.texting_colonists;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
/*? if forge {*/
/*import net.minecraftforge.common.MinecraftForge;
 *//*?}*/
/*? if forge {*/
/*import net.minecraftforge.fml.common.Mod;
 *//*?}*/
/*? if neoforge {*/
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
/*?}*/

@Mod(TextingColonistsMod.MOD_ID)
public class TextingColonistsMod {
    public static final String MOD_ID = /*$ mod_id*/ "texting_colonists";
    public static final String MOD_NAME = /*$ mod_name*/ "Texting Colonists (Talking Colonists Add-On)";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TextingColonistsMod() {
        /*? if forge {*/
        /*MinecraftForge.EVENT_BUS.register(new ChatEventHandler());
         *//*?}*/
        /*? if neoforge {*/
        NeoForge.EVENT_BUS.register(new ChatEventHandler());
        /*?}*/
    }
}
