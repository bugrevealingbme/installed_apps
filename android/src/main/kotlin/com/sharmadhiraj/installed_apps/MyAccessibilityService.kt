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
                logAllButtons(rootNode)
                // "Force Stop" butonunu arıyoruz
                val forceStopButton = findForceStopButtonGenerically(rootNode)
                if (forceStopButton != null && forceStopButton.isEnabled) {
                    forceStopButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d("AccessibilityService", "'Force Stop' button clicked.")
                } else {
                    //
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
        }

        Thread.sleep(1000)
        
        return true
    }

    private fun logAllButtons(root: AccessibilityNodeInfo) {
        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            if (child != null) {
                Log.d("AccessibilityService", "Class: ${child.className}, Text: ${child.text}, ViewID: ${child.viewIdResourceName}")

                // Çocuk düğümlerini de taramak için rekürsif çağrı yapıyoruz
                logAllButtons(child)
            }
        }
    }

    private fun findForceStopButtonGenerically(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            if (child != null) {
                // Eğer düğme metni "Force stop" içeriyorsa ve tıklanabiliyorsa
                if (child.isClickable && child.text?.toString()?.contains("Force stop", ignoreCase = true) == true) {
                    return child
                }
    
                // Çocuk düğümleri de tara
                val result = findForceStopButtonGenerically(child)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }
    
    private fun findForceStopButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // Önce View ID ile, ardından metin ile arıyoruz
        return root.findAccessibilityNodeInfosByViewId("com.android.settings:id/force_stop_button").firstOrNull()
            ?: root.findAccessibilityNodeInfosByText("Force stop").firstOrNull()
    }
}
