//#if FABRIC && MC<21.6
// todo: update when available
package su.plo.voice.client.integration

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> =
        ConfigScreenFactory { parent ->
            try {
                createClothConfigMenu(parent)
            } catch (ignored: NoClassDefFoundError) {
                throw IllegalStateException("ClothConfig is not installed")
            }
        }
}
//#endif
