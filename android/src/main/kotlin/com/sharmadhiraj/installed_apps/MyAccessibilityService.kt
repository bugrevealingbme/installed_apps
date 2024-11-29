class MyAccessibilityService : AccessibilityService() {

    private var cachedRootNode: AccessibilityNodeInfo? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.className?.contains("SettingsActivity", ignoreCase = true) == true) {
                cachedRootNode = event.source ?: rootInActiveWindow
                Log.d("AccessibilityService", "Cached node updated for SettingsActivity.")
            }
        }
    }

    override fun onInterrupt() {}

    fun closeAppInBackground(context: Context, packageName: String): Boolean {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            // Bekleme döngüsü
            for (i in 1..10) {
                Thread.sleep(1000)
                val rootNode = cachedRootNode ?: rootInActiveWindow
                if (rootNode != null) {
                    Log.d("AccessibilityService", "Root node found after waiting.")
                    cachedRootNode = rootNode
                    break
                }
            }

            val rootNode = cachedRootNode ?: rootInActiveWindow
            if (rootNode == null) {
                Log.e("AccessibilityService", "Root node is null even after waiting!")
                return false
            }

            val forceStopButton = findForceStopButton(rootNode)
            forceStopButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d("AccessibilityService", "Successfully clicked 'Force Stop'")
            return true
        } catch (e: Exception) {
            Log.e("AccessibilityService", "Error stopping app: ${e.message}")
        }
        return false
    }

    private fun findForceStopButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return root.findAccessibilityNodeInfosByViewId("com.android.settings:id/force_stop_button").firstOrNull()
            ?: root.findAccessibilityNodeInfosByText("Force stop").firstOrNull()
    }
}
