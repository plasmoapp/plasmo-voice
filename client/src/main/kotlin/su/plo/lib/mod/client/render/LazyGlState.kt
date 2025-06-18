package su.plo.lib.mod.client.render

class LazyGlState {
    //#if MC<12105
    private var lastUpdate = 0L
    private var lastState: GlState? = null

    val state: GlState
        get() {
            if (System.currentTimeMillis() - lastUpdate > 500 || lastState == null) {
                lastUpdate = System.currentTimeMillis()
                lastState = GlState.current()
            }

            return lastState!!
        }
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
