import SwiftUI
import UIKit
import FirebaseAuth
import composeApp

class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }

        // Initialize Koin with iOS modules before accessing any DI
        Platform_iosKt.doInitKoin()

        // Set up the Swift auth bridge with the Kotlin auth manager
        let authManager = Platform_iosKt.getAuthManager()
        AuthBridge.shared.setup(with: authManager)

        let controller = Platform_iosKt.MainViewController()
        window = UIWindow(windowScene: windowScene)
        window?.rootViewController = controller
        window?.makeKeyAndVisible()
    }

    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
        for ctx in URLContexts {
            if Auth.auth().canHandle(ctx.url) {
                return
            }
        }
    }
}
