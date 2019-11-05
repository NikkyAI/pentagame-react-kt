package penta.app

import java.awt.Image
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon

object TrayUtil {
    val tray: SystemTray = SystemTray.getSystemTray()
    val image: Image = Toolkit.getDefaultToolkit().createImage(javaClass.getResource("/rainblob.png"))
    val trayIcon = TrayIcon(image, "Pentagame")

    init {
        trayIcon.isImageAutoSize = true
        trayIcon.toolTip = "System tray icon demo"
        tray.add(trayIcon)
    }
}