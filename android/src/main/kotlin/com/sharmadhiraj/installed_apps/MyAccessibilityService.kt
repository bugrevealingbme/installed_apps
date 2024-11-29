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
        // Gerekli olayları işlemek için buraya ekleme yapabilirsiniz.
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
    
            // startActivity'yi context üzerinden çağırıyoruz
            context.startActivity(intent)
    
            // Kısa bir bekleme süresi, ayar ekranının yüklenmesini bekler
            Thread.sleep(1000)
    
            // "Durmaya Zorla" düğmesini bul ve tıkla
            val sourceNode = event.source
            sourceNode?.let { rootNode ->
                Log.d("Test", "Source node found, starting to search for force stop button")
        
                // Force Stop butonunu bulmaya çalışıyoruz
                val forceStopButton = findForceStopButton(rootNode)
                findForceStopButtonTest(rootNode) // Test fonksiyonunu çağırıyoruz
        
                forceStopButton?.let { button ->
                    // Butona tıklıyoruz
                    button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d("AccessibilityService", "Successfully clicked 'Force Stop'")
        
                    // Geri tuşuna basıyoruz
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    return true
                } ?: run {
                    Log.d("Test", "Force Stop button not found")
                }
            } else {
                Log.d("Test", "Root node is null!")
            }

        } catch (e: Exception) {
            Log.e("AccessibilityService", "Error stopping app: ${e.message}")
        }
        return false
    }

private fun findForceStopButtonTest(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
    Log.d("Test", "findForceStopButtonTest findForceStopButtonTest findForceStopButtonTest")
    
    // Tüm butonları döngüye alarak inceleme
    for (i in 0 until root.childCount) {
        val child = root.getChild(i)
        if (child != null && child.className == "android.widget.Button") {
            val text = child.text.toString()

            // Child butonunun detaylarını log olarak yazdırma
            Log.d("Test", "Button Found:")
            Log.d("Test", "Text: $text")
            Log.d("Test", "Class Name: ${child.className}")
            Log.d("Test", "View ID: ${child.viewIdResourceName}")
            Log.d("Test", "Content Description: ${child.contentDescription}")
            Log.d("Test", "Package Name: ${child.packageName}")
            Log.d("Test", "Is Enabled: ${child.isEnabled}")
            Log.d("Test", "Is Clickable: ${child.isClickable}")
            Log.d("Test", "Is Focusable: ${child.isFocusable}")
            Log.d("Test", "Is Focused: ${child.isFocused}")
            Log.d("Test", "Is Checkable: ${child.isCheckable}")
            Log.d("Test", "Is Checked: ${child.isChecked}")
            Log.d("Test", "Is Password: ${child.isPassword}")
            
            // Buton metnini kontrol et
            if (text.contains("Force stop", ignoreCase = true)) {
                return child
            }
        }
    }
    return null
}

    
    // "Durmaya Zorla" butonunu sınıf adı ile bulma
    private fun findForceStopButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val buttons = root.findAccessibilityNodeInfosByViewId("com.android.settings:id/force_stop_button") // ID üzerinden bulma

  if (buttons.isNotEmpty()) {
        Log.d("FindButton", "Buton bulundu ID ile")
        return buttons.first()
    }
        
        return buttons.firstOrNull() ?: root.findAccessibilityNodeInfosByText("Force stop").firstOrNull()
    }
}
