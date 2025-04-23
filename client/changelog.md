### Changes in 2.1.4
- Fixed `java.lang.NoSuchMethodError: net.minecraft.util.ResourceLocation.func_217855_b(Ljava/lang/String;)Z` on 1.16.5 forge.
- Fixed issue where voice distance visualization was not fading away with high fps.
- Fixed entity source icon not being rendered on players.
- Fixed broken render of distance visualization and static source icons on <1.18.
- Added scrollbar for dropdown widget.
- Fixed warning "Reference map '...' for 'slib.mixins.json' could not be read" on forgelike >=1.21.
- Fixed performance issues in mod's GUI due to constant reflection Vulkan check.
- Fixed an NPE when opening mod's menu with Polytone installed [#454](https://github.com/plasmoapp/plasmo-voice/issues/454). 
