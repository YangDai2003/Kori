import SwiftUI

@main
struct iOSApp: App {
    // 注册 AppDelegate 以处理快捷方式
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        let config = UISceneConfiguration(name: nil, sessionRole: connectingSceneSession.role)
        config.delegateClass = SceneDelegate.self
        return config
    }
}

// SceneDelegate 处理 quick action
class SceneDelegate: NSObject, UIWindowSceneDelegate {
    // 保存冷启动时的 shortcutItem
    private var pendingShortcutItem: UIApplicationShortcutItem?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        if let shortcutItem = connectionOptions.shortcutItem {
            // 冷启动时保存，待 scene 激活后处理
            pendingShortcutItem = shortcutItem
        }
    }

    func sceneDidBecomeActive(_ scene: UIScene) {
        // 冷启动后首次激活时处理 pendingShortcutItem
        if let shortcutItem = pendingShortcutItem {
            handleShortCutItem(shortcutItem: shortcutItem)
            pendingShortcutItem = nil
        }
    }

    func windowScene(_ windowScene: UIWindowScene,
                     performActionFor shortcutItem: UIApplicationShortcutItem,
                     completionHandler: @escaping (Bool) -> Void) {
        let handled = handleShortCutItem(shortcutItem: shortcutItem)
        completionHandler(handled)
    }

    @discardableResult
    private func handleShortCutItem(shortcutItem: UIApplicationShortcutItem) -> Bool {
        let uri: String
        switch shortcutItem.type {
        case "note":
            uri = "kori://screen/note"
        case "template":
            uri = "kori://screen/template"
        case "folders":
            uri = "kori://screen/folders"
        case "settings":
            uri = "kori://screen/settings"
        default:
            return false
        }
        NotificationCenter.default.post(name: NSNotification.Name("ShortcutUri"), object: uri)
        return true
    }
}