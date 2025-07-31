import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    // Register the AppDelegate to handle incoming URLs
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
        open uri: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        // Sends the full URI on to the singleton
        ExternalUriHandler.shared.onNewUri(uri: uri.absoluteString)
        return true
    }
}