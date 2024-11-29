package com.sharmadhiraj.installed_apps

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.content.Context

class MyAccessibilityService : AccessibilityService() {

    internal var cachedRootNode: AccessibilityNodeInfo? = null
        
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val sourceNode = event?.source
        if (sourceNode != null) {
            Log.d("AccessibilityService", "Source node found: $sourceNode")
            cachedRootNode = sourceNode
        } else {
            Log.e("AccessibilityService", "Source node is null!")
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
                    Log.d("AccessibilityService", "Root node hala null amk")
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
