package me.sshcrack.texting_colonists;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
/*? if forge {*/
/*import net.minecraftforge.fml.common.Mod;
 *//*?}*/
/*? if neoforge {*/
import net.neoforged.fml.common.Mod;
/*?}*/

@Mod(TemplateMod.MOD_ID)
public class TemplateMod {
    public static final String MOD_ID = /*$ mod_id*/ "template";
    public static final String MOD_NAME = /*$ mod_name*/ "Template Mod";
    public static final Logger LOGGER = LogUtils.getLogger();
}
