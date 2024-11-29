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
        if (event == null) return

        // Event'in window content değişikliği ile ilgili olup olmadığını kontrol ediyoruz
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                Log.d("AccessibilityService", "Root node detected. Checking for 'Force Stop' button.")
                
                // "Force Stop" butonunu arıyoruz
                val forceStopButton = findForceStopButton(rootNode)
                if (forceStopButton != null && forceStopButton.isEnabled) {
                    forceStopButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d("AccessibilityService", "'Force Stop' button clicked.")
                } else {
                    Log.d("AccessibilityService", "'Force Stop' button not found or not enabled.")
                }
            } else {
                Log.d("AccessibilityService", "Root node is null.")
            }
        }
    }

    override fun onInterrupt() {}

    // Ayar ekranını açmak için kullanılır
    fun closeAppInBackground(context: Context, packageName: String): Boolean {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.d("AccessibilityService", "App settings screen opened for package: $packageName")
        } catch (e: Exception) {
            Log.e("AccessibilityService", "Error opening app settings: ${e.message}")
            return false
        }

        Thread.sleep(1000)
        
        return true
    }

    private fun findForceStopButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // Önce View ID ile, ardından metin ile arıyoruz
        return root.findAccessibilityNodeInfosByViewId("com.android.settings:id/force_stop_button").firstOrNull()
            ?: root.findAccessibilityNodeInfosByText("Force stop").firstOrNull()
    }
}
