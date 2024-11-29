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

    private var cachedRootNode: AccessibilityNodeInfo? = null // Global node

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val sourceNode = event?.source
        if (sourceNode != null) {
            Log.d("AccessibilityService", "Source node cached")
            cachedRootNode = sourceNode // Global node'u sakla
        } else {
            Log.e("AccessibilityService", "Source node is null!")
        }
    }

    override fun onInterrupt() {
        // Servis kesintiye uğradığında yapılacak işler.
    }

    fun closeAppInBackground(context: Context, packageName: String): Boolean {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Ayarlar ekranını açıyoruz
            context.startActivity(intent)

            // Kısa bir bekleme süresi
            Thread.sleep(1000)

            // Global node'u kullan
            val rootNode = cachedRootNode

            if (rootNode != null) {
                Log.d("AccessibilityService", "Root node found, searching for 'Force Stop' button")
                val forceStopButton = findForceStopButton(rootNode)
                findForceStopButtonTest(rootNode) // Test fonksiyonunu çağırıyoruz

                forceStopButton?.let { button ->
                    button.performAction(AccessibilityNodeInfo.ACTION_CLICK) // Butona tıklama
                    Log.d("AccessibilityService", "Successfully clicked 'Force Stop'")
                    Thread.sleep(500) // Tıklamanın işlenmesini bekle
                    performGlobalAction(GLOBAL_ACTION_BACK) // Geri tuşuna basarak ekranı kapat
                    return true
                }
            } else {
                Log.d("AccessibilityService", "Root node is null!")
            }
        } catch (e: Exception) {
            Log.e("AccessibilityService", "Error stopping app: ${e.message}")
        }
        return false
    }

    private fun findForceStopButtonTest(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        Log.d("Test", "Testing child nodes for Force Stop button")

        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            if (child != null && child.className == "android.widget.Button") {
                val text = child.text.toString()

                // Log buton detaylarını
                Log.d("Test", "Button Found: Text: $text, Class: ${child.className}")

                if (text.contains("Force stop", ignoreCase = true)) {
                    return child
                }
            }
        }
        return null
    }

    private fun findForceStopButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // Butonu ID üzerinden bulma
        val buttons = root.findAccessibilityNodeInfosByViewId("com.android.settings:id/force_stop_button")
        if (buttons.isNotEmpty()) {
            Log.d("AccessibilityService", "Found 'Force Stop' button via ID")
            return buttons.first()
        }

        // Alternatif olarak metinle bulma
        return buttons.firstOrNull() ?: root.findAccessibilityNodeInfosByText("Force stop").firstOrNull()
    }
}
