package su.plo.lib.mod.client.render

class LazyGlState {
    //#if MC<12105
    val state by lazy { GlState.current() }
    //#endif

    fun withState(block: Runnable) {
        //#if MC<12105
        RenderUtil.setGlState(state)
        //#endif

        block.run()

        //#if MC<12105
        RenderUtil.restoreGlState()
        //#endif
    }
}
