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

        
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //
    }

    override fun onInterrupt() {}

    fun closeAppInBackground(context: Context, packageName: String): Boolean {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
         
            val rootNode = rootInActiveWindow
            if (rootNode == null) {
                Log.e("AccessibilityService", "Root node is null!")
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
