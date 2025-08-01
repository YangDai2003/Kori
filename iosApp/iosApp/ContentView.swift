import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

struct ContentView: View {
    var body: some View {
        ComposeView().ignoresSafeArea() // Compose has own keyboard handler
            .onOpenURL(perform:
            { url in
                // Sends the full URI on to the singleton
                ExternalUriHandler.shared.onNewUri(uri: url.absoluteString)
            })
            .onReceive(NotificationCenter.default.publisher(for: NSNotification.Name("ShortcutUri"))) { notification in
                if let uri = notification.object as? String {
                    ExternalUriHandler.shared.onNewUri(uri: uri)
                }
            }
    }
}