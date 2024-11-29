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
            val rootNode = rootInActiveWindow
            rootNode?.let {
                // "Durmaya Zorla" butonunu sınıf adına göre bulmaya çalışıyoruz
                val forceStopButton = findForceStopButton(it)
                forceStopButton?.let { button ->
                    button.performAction(AccessibilityNodeInfo.ACTION_CLICK) // Butona tıklama
                    Log.d("AccessibilityService", "Successfully clicked 'Force Stop'")
                    Thread.sleep(500) // Tıklamanın işlenmesini bekle
                    performGlobalAction(GLOBAL_ACTION_BACK) // Geri tuşuna basarak ekranı kapat
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e("AccessibilityService", "Error stopping app: ${e.message}")
        }
        return false
    }

    // "Durmaya Zorla" butonunu sınıf adı ile bulma
    private fun findForceStopButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val buttons = root.findAccessibilityNodeInfosByViewId("com.android.settings:id/force_stop_button") // ID üzerinden bulma

  if (buttons.isNotEmpty()) {
        Log.d("FindButton", "Buton bulundu ID ile")
        return buttons.first()
    }
        
        return buttons.firstOrNull() ?: root.findAccessibilityNodeInfosByText("Durmaya Zorla").firstOrNull()
    }
}
