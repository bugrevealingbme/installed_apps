package com.sharmadhiraj.installed_apps

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.content.Context
import android.os.Handler

class MyAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Event'in window content değişikliği ile ilgili olup olmadığını kontrol ediyoruz
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                // "Force Stop" butonunu arıyoruz
                val forceStopButton = findForceStopButtonGenerically(rootNode)
                if (forceStopButton != null && forceStopButton.isEnabled) {
                    val clickableNode = getClickableNode(forceStopButton)
                    clickableNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    
                    // "OK" popup'ını kontrol et ve tıkla
                    //Handler().postDelayed({
                        val okButton = findButtonByText(rootNode, "OK")
                        val clickableokButton = getClickableNode(okButton)
                        if (clickableokButton != null) {
                            clickableokButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                    //}, 1000)
                } else {
                    //
                    Log.d("AccessibilityService", "No button amk $forceStopButton")
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
        
        return true
    }

    private fun findForceStopButtonGenerically(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            if (child != null) {
                if (child.text?.toString()?.contains("Force stop", ignoreCase = true) == true) {
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

    private fun getClickableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        var currentNode = node
        while (currentNode != null) {
            // Tıklanabilir bir düğüm buldukça loglama yapıyoruz
            if (currentNode.isClickable) {
                Log.d("AccessibilityService", "Found clickable node:")
                Log.d("AccessibilityService", "Text: ${currentNode.text}")
                Log.d("AccessibilityService", "Content Description: ${currentNode.contentDescription}")
                Log.d("AccessibilityService", "Class Name: ${currentNode.className}")
                Log.d("AccessibilityService", "Package Name: ${currentNode.packageName}")
                Log.d("AccessibilityService", "Is Enabled: ${currentNode.isEnabled}")
                Log.d("AccessibilityService", "Is Clickable: ${currentNode.isClickable}")
                Log.d("AccessibilityService", "Is Focusable: ${currentNode.isFocusable}")
                Log.d("AccessibilityService", "Is Selected: ${currentNode.isSelected}")
                Log.d("AccessibilityService", "Child Count: ${currentNode.childCount}")
                Log.d("AccessibilityService", "Is Checkable: ${currentNode.isCheckable}")
                Log.d("AccessibilityService", "Is Checked: ${currentNode.isChecked}")
                
                return currentNode
            }
            currentNode = currentNode.parent
        }
        return null
    }


    private fun findButtonByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text != null && node.text.toString() == text) {
            return node
        }
    
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val button = findButtonByText(child, text)
                if (button != null) {
                    return button
                }
            }
        }
        return null
    }

    fun simulateBackPress() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }
    
    private fun findForceStopButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // Önce View ID ile, ardından metin ile arıyoruz
        return root.findAccessibilityNodeInfosByViewId("com.android.settings:id/force_stop_button").firstOrNull()
            ?: root.findAccessibilityNodeInfosByText("Force stop").firstOrNull()
    }
}
