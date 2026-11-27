import UIKit
import PhotosUI
import SwiftUI
import FirebaseCore
import FirebaseAuth
import FirebaseFirestore
import composeApp

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    // Auth bridge reference
    static var authBridge: AuthBridge?
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // Initialize Firebase
        FirebaseApp.configure()
        return true
    }

    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        let configuration = UISceneConfiguration(name: nil, sessionRole: connectingSceneSession.role)
        configuration.delegateClass = SceneDelegate.self
        return configuration
    }

    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        if Auth.auth().canHandle(url) {
            return true
        }
        return false
    }
}

// MARK: - Auth Bridge
// Handles Firebase Auth operations and calls back into Kotlin IOSAuthManager
class AuthBridge: NSObject, PHPickerViewControllerDelegate {
    
    static let shared = AuthBridge()
    private var authStateListener: Any?
    private var authManager: IOSAuthManager?
    private var pollTimer: Timer?

    private func linkedProvidersCsv(for user: User) -> String {
        user.providerData.map(\.providerID).joined(separator: ",")
    }

    private func resolvedPhotoUrl(for user: User) -> String {
        let googlePhoto = user.providerData
            .first(where: { $0.providerID == "google.com" && $0.photoURL != nil })?
            .photoURL?.absoluteString
        let raw = googlePhoto ?? user.photoURL?.absoluteString ?? ""
        return raw.replacingOccurrences(of: "=s96-c", with: "=s400-c")
    }
    
    func setup(with manager: IOSAuthManager) {
        self.authManager = manager
        
        // Set up Firebase auth state listener
        authStateListener = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            guard let self = self else { return }
            if let user = user {
                let providersCsv = self.linkedProvidersCsv(for: user)
                let googlePhotoUrl = self.resolvedPhotoUrl(for: user)
                self.fetchUserData(for: user.uid, fallbackPhotoUrl: googlePhotoUrl) { username, isAdmin, photoUrl, photoData in
                    self.authManager?.onAuthStateChanged(
                        uid: user.uid,
                        email: user.email ?? "",
                        displayName: user.displayName ?? "",
                        username: username,
                        isAdmin: isAdmin,
                        linkedProvidersCsv: providersCsv,
                        photoUrl: photoUrl,
                        photoData: photoData
                    )
                }
            } else {
                self.authManager?.onAuthStateChanged(uid: "", email: "", displayName: "", username: "", isAdmin: false, linkedProvidersCsv: "", photoUrl: "", photoData: "")
            }
        }
        
        // Send initial auth state
        if let user = Auth.auth().currentUser {
            let providersCsv = linkedProvidersCsv(for: user)
            let googlePhotoUrl = resolvedPhotoUrl(for: user)
            fetchUserData(for: user.uid, fallbackPhotoUrl: googlePhotoUrl) { username, isAdmin, photoUrl, photoData in
                self.authManager?.onAuthStateChanged(
                    uid: user.uid,
                    email: user.email ?? "",
                    displayName: user.displayName ?? "",
                    username: username,
                    isAdmin: isAdmin,
                    linkedProvidersCsv: providersCsv,
                    photoUrl: photoUrl,
                    photoData: photoData
                )
            }
        } else {
            manager.onAuthStateChanged(uid: "", email: "", displayName: "", username: "", isAdmin: false, linkedProvidersCsv: "", photoUrl: "", photoData: "")
        }
        
        // Start polling for pending requests from Kotlin
        startPolling()
    }
    
    private func startPolling() {
        pollTimer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { [weak self] _ in
            self?.checkPendingRequests()
        }
    }
    
    private func stopPolling() {
        pollTimer?.invalidate()
        pollTimer = nil
    }
    
    private func checkPendingRequests() {
        guard let manager = authManager else { return }
        
        if manager.pendingSignOut {
            manager.pendingSignOut = false
            try? Auth.auth().signOut()
            manager.handleSignOut()
            return
        }
        
        if let email = manager.pendingSignInEmail,
           let password = manager.pendingSignInPassword {
            manager.pendingSignInEmail = nil
            manager.pendingSignInPassword = nil
            executeSignIn(email: email, password: password)
            return
        }
        
        if let email = manager.pendingSignUpEmail,
           let password = manager.pendingSignUpPassword,
           let displayName = manager.pendingSignUpDisplayName {
            manager.pendingSignUpEmail = nil
            manager.pendingSignUpPassword = nil
            manager.pendingSignUpDisplayName = nil
            executeSignUp(email: email, password: password, displayName: displayName)
            return
        }
        
        if let userId = manager.pendingBackupUserId,
           let jsonString = manager.pendingBackupJson {
            manager.pendingBackupUserId = nil
            manager.pendingBackupJson = nil
            executeBackup(userId: userId, jsonString: jsonString)
            return
        }
        
        if let userId = manager.pendingRestoreUserId {
            manager.pendingRestoreUserId = nil
            executeRestore(userId: userId)
            return
        }
        
        if let userId = manager.pendingUpdateUsernameUserId,
           let newUsername = manager.pendingUpdateUsernameNewName {
            manager.pendingUpdateUsernameUserId = nil
            manager.pendingUpdateUsernameNewName = nil
            executeUpdateUsername(userId: userId, newUsername: newUsername)
            return
        }

        if let userId = manager.pendingUpdatePhotoUserId,
           let photoData = manager.pendingUpdatePhotoData {
            manager.pendingUpdatePhotoUserId = nil
            manager.pendingUpdatePhotoData = nil
            executeUpdateProfilePhoto(userId: userId, photoData: photoData)
            return
        }

        if manager.pendingPickProfilePhoto {
            manager.pendingPickProfilePhoto = false
            executePickProfilePhoto()
            return
        }
        
        if manager.pendingFetchUsers {
            manager.pendingFetchUsers = false
            executeFetchUsers()
            return
        }
        
        if let userId = manager.pendingDeleteUserId {
            manager.pendingDeleteUserId = nil
            executeDeleteUser(userId: userId)
            return
        }

        if let userId = manager.pendingUpdateRoleUserId {
            let grantAdmin = manager.pendingUpdateRoleIsAdmin
            manager.pendingUpdateRoleUserId = nil
            executeUpdateUserRole(userId: userId, isAdmin: grantAdmin)
            return
        }

        if manager.pendingFetchContributions {
            manager.pendingFetchContributions = false
            executeFetchContributions()
            return
        }

        if let payloadJson = manager.pendingPublishContributionJson {
            manager.pendingPublishContributionJson = nil
            executePublishContribution(jsonString: payloadJson)
            return
        }

        if let contribId = manager.pendingApproveContributionId {
            manager.pendingApproveContributionId = nil
            executeApproveContribution(id: contribId)
            return
        }

        if let contribId = manager.pendingDeleteContributionId {
            manager.pendingDeleteContributionId = nil
            executeDeleteContribution(id: contribId)
            return
        }

        if manager.pendingGoogleSignIn {
            manager.pendingGoogleSignIn = false
            executeGoogleSignIn()
            return
        }

        if manager.pendingLinkGoogle {
            manager.pendingLinkGoogle = false
            executeLinkWithGoogle()
            return
        }

        if let resetEmail = manager.pendingResetEmail {
            manager.pendingResetEmail = nil
            executeSendPasswordReset(email: resetEmail)
            return
        }

        if manager.pendingDeleteAccount {
            manager.pendingDeleteAccount = false
            executeDeleteAccount()
            return
        }
    }
    
    private func executeSignIn(email: String, password: String) {
        Auth.auth().signIn(withEmail: email, password: password) { [weak self] authResult, error in
            guard let self = self else { return }
            
            if let error = error {
                self.authManager?.handleSignInResult(uid: "", email: "", displayName: "", username: "", isAdmin: false, linkedProvidersCsv: "", photoUrl: "", photoData: "", error: error.localizedDescription)
                return
            }
            
            guard let user = authResult?.user else {
                self.authManager?.handleSignInResult(uid: "", email: "", displayName: "", username: "", isAdmin: false, linkedProvidersCsv: "", photoUrl: "", photoData: "", error: "Usuario no encontrado")
                return
            }
            
            let providersCsv = self.linkedProvidersCsv(for: user)
            let googlePhotoUrl = self.resolvedPhotoUrl(for: user)
            self.fetchUserData(for: user.uid, fallbackPhotoUrl: googlePhotoUrl) { username, isAdmin, photoUrl, photoData in
                self.authManager?.handleSignInResult(
                    uid: user.uid,
                    email: user.email ?? "",
                    displayName: user.displayName ?? "",
                    username: username,
                    isAdmin: isAdmin,
                    linkedProvidersCsv: providersCsv,
                    photoUrl: photoUrl,
                    photoData: photoData,
                    error: nil
                )
            }
        }
    }
    
    private func executeSignUp(email: String, password: String, displayName: String) {
        Auth.auth().createUser(withEmail: email, password: password) { [weak self] authResult, error in
            guard let self = self else { return }
            
            if let error = error {
                self.authManager?.handleSignUpResult(uid: "", email: "", displayName: "", username: "", isAdmin: false, linkedProvidersCsv: "", photoUrl: "", photoData: "", error: error.localizedDescription)
                return
            }
            
            guard let user = authResult?.user else {
                self.authManager?.handleSignUpResult(uid: "", email: "", displayName: "", username: "", isAdmin: false, linkedProvidersCsv: "", photoUrl: "", photoData: "", error: "Error al crear la cuenta")
                return
            }
            
            let changeRequest = user.createProfileChangeRequest()
            changeRequest.displayName = displayName
            changeRequest.commitChanges { _ in }
            
            let username = displayName.lowercased().replacingOccurrences(of: " ", with: "_")
            let userData: [String: Any] = [
                "email": email,
                "displayName": displayName,
                "username": username,
                "isAdmin": false,
                "joinDate": Int64(Date().timeIntervalSince1970 * 1000)
            ]
            
            let db = Firestore.firestore()
            db.collection("users").document(user.uid).setData(userData) { [weak self] _ in
                guard let self = self else { return }
                self.authManager?.handleSignUpResult(
                    uid: user.uid,
                    email: user.email ?? "",
                    displayName: displayName,
                    username: username,
                    isAdmin: false,
                    linkedProvidersCsv: self.linkedProvidersCsv(for: user),
                    photoUrl: "",
                    photoData: "",
                    error: nil
                )
            }
        }
    }
    
    private func executeBackup(userId: String, jsonString: String) {
        guard let data = jsonString.data(using: .utf8),
              let jsonObject = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            authManager?.handleBackupResult(success: false, error: "Formato de datos de respaldo inválido")
            return
        }
        
        let db = Firestore.firestore()
        db.collection("users")
            .document(userId)
            .collection("backups")
            .document("latest")
            .setData(jsonObject) { [weak self] error in
                if let error = error {
                    self?.authManager?.handleBackupResult(success: false, error: error.localizedDescription)
                } else {
                    self?.authManager?.handleBackupResult(success: true, error: nil)
                }
            }
    }
    
    private func executeRestore(userId: String) {
        let db = Firestore.firestore()
        db.collection("users")
            .document(userId)
            .collection("backups")
            .document("latest")
            .getDocument { [weak self] snapshot, error in
                guard let self = self else { return }
                if let error = error {
                    self.authManager?.handleRestoreResult(json: nil, error: error.localizedDescription)
                    return
                }
                guard let snapshot = snapshot, snapshot.exists, let data = snapshot.data() else {
                    self.authManager?.handleRestoreResult(json: nil, error: "No se encontró ningún respaldo en la nube.")
                    return
                }
                
                let sanitized = self.sanitizeForJson(data)
                guard JSONSerialization.isValidJSONObject(sanitized) else {
                    self.authManager?.handleRestoreResult(json: nil, error: "Formato de respaldo incompatible.")
                    return
                }
                do {
                    let jsonData = try JSONSerialization.data(withJSONObject: sanitized, options: [])
                    let jsonString = String(data: jsonData, encoding: .utf8)
                    self.authManager?.handleRestoreResult(json: jsonString, error: nil)
                } catch {
                    self.authManager?.handleRestoreResult(json: nil, error: "Error al deserializar el respaldo: \(error.localizedDescription)")
                }
            }
    }

    private func sanitizeForJson(_ value: Any) -> Any {
        if let ts = value as? Timestamp {
            return Int64(ts.dateValue().timeIntervalSince1970 * 1000)
        }
        if let date = value as? Date {
            return Int64(date.timeIntervalSince1970 * 1000)
        }
        if let dict = value as? [String: Any] {
            var out: [String: Any] = [:]
            for (k, v) in dict {
                out[k] = sanitizeForJson(v)
            }
            return out
        }
        if let arr = value as? [Any] {
            return arr.map { sanitizeForJson($0) }
        }
        return value
    }
    
    private func syncPublicProfile(
        uid: String,
        displayName: String? = nil,
        username: String? = nil,
        photoUrl: String? = nil,
        photoData: String? = nil
    ) {
        guard !uid.isEmpty else { return }
        var payload: [String: Any] = [:]
        if let displayName = displayName {
            payload["displayName"] = String(displayName.prefix(100))
        }
        if let username = username {
            payload["username"] = String(username.prefix(60))
        }
        if let photoUrl = photoUrl {
            payload["photoUrl"] = String(photoUrl.prefix(2048))
        }
        if let photoData = photoData {
            payload["photoData"] = String(photoData.prefix(180000))
        }
        guard !payload.isEmpty else { return }
        Firestore.firestore().collection("public_profiles").document(uid).setData(payload, merge: true)
    }

    private func executeUpdateUsername(userId: String, newUsername: String) {
        let db = Firestore.firestore()
        db.collection("users").document(userId).updateData(["username": newUsername]) { [weak self] error in
            if let error = error {
                self?.authManager?.handleUpdateUsernameResult(success: false, error: error.localizedDescription)
            } else {
                self?.syncPublicProfile(uid: userId, username: newUsername)
                self?.authManager?.handleUpdateUsernameResult(success: true, error: nil)
            }
        }
    }

    private func executeUpdateProfilePhoto(userId: String, photoData: String) {
        let db = Firestore.firestore()
        db.collection("users").document(userId).updateData(["photoData": photoData]) { [weak self] error in
            if let error = error {
                self?.authManager?.handleUpdatePhotoResult(success: false, error: error.localizedDescription)
            } else {
                self?.syncPublicProfile(uid: userId, photoData: photoData)
                self?.authManager?.handleUpdatePhotoResult(success: true, error: nil)
            }
        }
    }

    private func topViewController(base: UIViewController? = nil) -> UIViewController? {
        let root: UIViewController?
        if let base = base {
            root = base
        } else {
            root = UIApplication.shared.connectedScenes
                .compactMap { $0 as? UIWindowScene }
                .flatMap(\.windows)
                .first(where: \.isKeyWindow)?
                .rootViewController
        }
        if let nav = root as? UINavigationController {
            return topViewController(base: nav.visibleViewController)
        }
        if let tab = root as? UITabBarController, let selected = tab.selectedViewController {
            return topViewController(base: selected)
        }
        if let presented = root?.presentedViewController {
            return topViewController(base: presented)
        }
        return root
    }

    private func executePickProfilePhoto() {
        DispatchQueue.main.async { [weak self] in
            guard let self = self, let presenter = self.topViewController() else {
                self?.authManager?.handlePickProfilePhotoResult(base64: nil, error: "No se encontró una ventana activa para abrir la galería.")
                return
            }
            var config = PHPickerConfiguration(photoLibrary: .shared())
            config.filter = .images
            config.selectionLimit = 1
            let picker = PHPickerViewController(configuration: config)
            picker.delegate = self
            presenter.present(picker, animated: true)
        }
    }

    func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        picker.dismiss(animated: true)
        guard let provider = results.first?.itemProvider,
              provider.canLoadObject(ofClass: UIImage.self) else {
            authManager?.handlePickProfilePhotoResult(base64: nil, error: nil)
            return
        }
        provider.loadObject(ofClass: UIImage.self) { [weak self] object, error in
            guard let self = self else { return }
            if let error = error {
                DispatchQueue.main.async {
                    self.authManager?.handlePickProfilePhotoResult(base64: nil, error: error.localizedDescription)
                }
                return
            }
            guard let image = object as? UIImage,
                  let encoded = self.encodeProfileImage(image) else {
                DispatchQueue.main.async {
                    self.authManager?.handlePickProfilePhotoResult(base64: nil, error: "No se pudo procesar la imagen seleccionada.")
                }
                return
            }
            DispatchQueue.main.async {
                self.authManager?.handlePickProfilePhotoResult(base64: encoded, error: nil)
            }
        }
    }

    private func encodeProfileImage(_ image: UIImage) -> String? {
        let targetSize = CGSize(width: 256, height: 256)
        let format = UIGraphicsImageRendererFormat()
        format.scale = 1.0
        let renderer = UIGraphicsImageRenderer(size: targetSize, format: format)
        let resized = renderer.image { _ in
            let w = image.size.width
            let h = image.size.height
            guard w > 0, h > 0 else { return }
            let scale = max(targetSize.width / w, targetSize.height / h)
            let scaledSize = CGSize(width: w * scale, height: h * scale)
            let origin = CGPoint(
                x: (targetSize.width - scaledSize.width) / 2.0,
                y: (targetSize.height - scaledSize.height) / 2.0
            )
            image.draw(in: CGRect(origin: origin, size: scaledSize))
        }
        for quality in [0.78, 0.62, 0.45] {
            if let data = resized.jpegData(compressionQuality: CGFloat(quality)) {
                let b64 = data.base64EncodedString()
                if b64.count <= 170_000 {
                    return b64
                }
            }
        }
        return nil
    }
    
    private func executeFetchUsers() {
        let db = Firestore.firestore()
        db.collection("users").getDocuments { [weak self] snapshot, error in
            guard let self = self else { return }
            if let error = error {
                self.authManager?.handleFetchUsersResult(usersJson: nil, error: error.localizedDescription)
                return
            }
            guard let docs = snapshot?.documents else {
                self.authManager?.handleFetchUsersResult(usersJson: "[]", error: nil)
                return
            }
            let userList: [[String: Any]] = docs.map { doc in
                let data = doc.data()
                let uid = doc.documentID
                let email = data["email"] as? String ?? ""
                let username = data["username"] as? String ?? (data["displayName"] as? String ?? "musico")
                let isAdmin = data["isAdmin"] as? Bool ?? false
                let role = isAdmin ? "ADMIN" : "USER"
                let photoUrl = data["photoUrl"] as? String ?? ""
                let photoData = data["photoData"] as? String ?? ""
                return [
                    "uid": uid,
                    "email": email,
                    "username": username,
                    "role": role,
                    "photoUrl": photoUrl,
                    "photoData": photoData
                ]
            }
            if let jsonData = try? JSONSerialization.data(withJSONObject: userList, options: []),
               let jsonString = String(data: jsonData, encoding: .utf8) {
                self.authManager?.handleFetchUsersResult(usersJson: jsonString, error: nil)
            } else {
                self.authManager?.handleFetchUsersResult(usersJson: "[]", error: nil)
            }
        }
    }
    
    private func executeDeleteUser(userId: String) {
        let db = Firestore.firestore()
        db.collection("users").document(userId).delete { [weak self] error in
            if let error = error {
                self?.authManager?.handleDeleteUserResult(success: false, error: error.localizedDescription)
                return
            }
            db.collection("public_profiles").document(userId).delete()
            // Delete backups and contributions authored by this user
            db.collection("users").document(userId).collection("backups").getDocuments { snap, _ in
                snap?.documents.forEach { $0.reference.delete() }
            }
            db.collection("contributions").whereField("authorId", isEqualTo: userId).getDocuments { snap, _ in
                snap?.documents.forEach { $0.reference.delete() }
            }
            self?.authManager?.handleDeleteUserResult(success: true, error: nil)
        }
    }

    private func executeUpdateUserRole(userId: String, isAdmin: Bool) {
        let db = Firestore.firestore()
        db.collection("users").document(userId).updateData(["isAdmin": isAdmin]) { [weak self] error in
            if let error = error {
                self?.authManager?.handleUpdateUserRoleResult(success: false, error: error.localizedDescription)
            } else {
                self?.authManager?.handleUpdateUserRoleResult(success: true, error: nil)
            }
        }
    }

    private func executeFetchContributions() {
        let db = Firestore.firestore()
        db.collection("contributions").getDocuments { [weak self] snapshot, error in
            guard let self = self else { return }
            if let error = error {
                self.authManager?.handleFetchContributionsResult(contributionsJson: nil, error: error.localizedDescription)
                return
            }
            guard let docs = snapshot?.documents else {
                self.authManager?.handleFetchContributionsResult(contributionsJson: "[]", error: nil)
                return
            }
            var baseList: [[String: Any]] = docs.compactMap { doc in
                let data = doc.data()
                guard let title = data["title"] as? String else { return nil }
                let approved = (data["isApproved"] as? Bool) ?? (data["approved"] as? Bool) ?? false
                let rawTimestamp = data["timestamp"]
                let millis: Int64
                if let intTs = rawTimestamp as? Int64 {
                    millis = intTs
                } else if let numTs = rawTimestamp as? NSNumber {
                    millis = numTs.int64Value
                } else if let fsTs = rawTimestamp as? Timestamp {
                    millis = Int64(fsTs.dateValue().timeIntervalSince1970 * 1000)
                } else {
                    millis = 0
                }
                return [
                    "id": doc.documentID,
                    "title": title,
                    "artist": (data["artist"] as? String) ?? "",
                    "authorId": (data["authorId"] as? String) ?? "",
                    "authorUsername": (data["authorName"] as? String) ?? "Músico",
                    "bpm": (data["bpm"] as? NSNumber)?.intValue ?? 120,
                    "key": (data["key"] as? String) ?? "C",
                    "tuning": (data["tuning"] as? String) ?? "E Standard",
                    "lyricsAndChords": (data["content"] as? String) ?? "",
                    "status": approved ? "APPROVED" : "PENDING",
                    "timestamp": millis
                ]
            }.sorted {
                (($0["timestamp"] as? Int64) ?? 0) > (($1["timestamp"] as? Int64) ?? 0)
            }

            let authorIds = Array(Set(baseList.compactMap { $0["authorId"] as? String }.filter { !$0.isEmpty }))
            if authorIds.isEmpty {
                if let jsonData = try? JSONSerialization.data(withJSONObject: baseList, options: []),
                   let jsonString = String(data: jsonData, encoding: .utf8) {
                    self.authManager?.handleFetchContributionsResult(contributionsJson: jsonString, error: nil)
                } else {
                    self.authManager?.handleFetchContributionsResult(contributionsJson: "[]", error: nil)
                }
                return
            }

            let group = DispatchGroup()
            let lock = NSLock()
            var avatars: [String: (String, String)] = [:]
            let currentUser = Auth.auth().currentUser
            let myFallbackPhotoUrl = currentUser.map { self.resolvedPhotoUrl(for: $0) } ?? ""

            for uid in authorIds {
                group.enter()
                db.collection("public_profiles").document(uid).getDocument { snap, _ in
                    var url = (snap?.data()?["photoUrl"] as? String) ?? ""
                    let b64 = (snap?.data()?["photoData"] as? String) ?? ""
                    if uid == currentUser?.uid && url.isEmpty && !myFallbackPhotoUrl.isEmpty {
                        url = myFallbackPhotoUrl
                        self.syncPublicProfile(uid: uid, photoUrl: url)
                    }
                    lock.lock()
                    avatars[uid] = (url, b64)
                    lock.unlock()
                    group.leave()
                }
            }

            group.notify(queue: .main) {
                for idx in baseList.indices {
                    let uid = (baseList[idx]["authorId"] as? String) ?? ""
                    if let pair = avatars[uid] {
                        baseList[idx]["authorPhotoUrl"] = pair.0
                        baseList[idx]["authorPhotoData"] = pair.1
                    }
                }
                if let jsonData = try? JSONSerialization.data(withJSONObject: baseList, options: []),
                   let jsonString = String(data: jsonData, encoding: .utf8) {
                    self.authManager?.handleFetchContributionsResult(contributionsJson: jsonString, error: nil)
                } else {
                    self.authManager?.handleFetchContributionsResult(contributionsJson: "[]", error: nil)
                }
            }
        }
    }

    private func executePublishContribution(jsonString: String) {
        guard let data = jsonString.data(using: .utf8),
              let parsed = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let currentUid = Auth.auth().currentUser?.uid else {
            authManager?.handlePublishContributionResult(success: false, error: "Debes iniciar sesión para compartir canciones.")
            return
        }
        let db = Firestore.firestore()
        let doc = db.collection("contributions").document()
        let title = String(((parsed["title"] as? String) ?? "Sin título").trimmingCharacters(in: .whitespacesAndNewlines).prefix(200))
        let artist = String(((parsed["artist"] as? String) ?? "Original").trimmingCharacters(in: .whitespacesAndNewlines).prefix(200))
        let key = String(((parsed["key"] as? String) ?? "C").prefix(50))
        let tuning = String(((parsed["tuning"] as? String) ?? "E Standard").prefix(100))
        let bpm = max(0, min(500, (parsed["bpm"] as? NSNumber)?.intValue ?? 120))
        let content = String(((parsed["content"] as? String) ?? "Sin letra ni acordes").prefix(50000))
        let authorName = String(((parsed["authorName"] as? String) ?? "Músico Riff Forge").trimmingCharacters(in: .whitespacesAndNewlines).prefix(100))
        let payload: [String: Any] = [
            "id": doc.documentID,
            "title": title.isEmpty ? "Sin título" : title,
            "artist": artist.isEmpty ? "Original" : artist,
            "key": key.isEmpty ? "C" : key,
            "tuning": tuning.isEmpty ? "E Standard" : tuning,
            "bpm": bpm,
            "content": content.isEmpty ? "Sin letra ni acordes" : content,
            "authorId": currentUid,
            "authorName": authorName.isEmpty ? "Músico Riff Forge" : authorName,
            "isApproved": false,
            "approved": false,
            "timestamp": Int64(Date().timeIntervalSince1970 * 1000)
        ]
        doc.setData(payload) { [weak self] error in
            if let error = error {
                self?.authManager?.handlePublishContributionResult(success: false, error: error.localizedDescription)
            } else {
                self?.authManager?.handlePublishContributionResult(success: true, error: nil)
            }
        }
    }

    private func executeApproveContribution(id: String) {
        let db = Firestore.firestore()
        db.collection("contributions").document(id).updateData([
            "isApproved": true,
            "approved": true
        ]) { [weak self] error in
            if let error = error {
                self?.authManager?.handleApproveContributionResult(success: false, error: error.localizedDescription)
            } else {
                self?.authManager?.handleApproveContributionResult(success: true, error: nil)
            }
        }
    }

    private func executeDeleteContribution(id: String) {
        let db = Firestore.firestore()
        db.collection("contributions").document(id).delete { [weak self] error in
            if let error = error {
                self?.authManager?.handleDeleteContributionResult(success: false, error: error.localizedDescription)
            } else {
                self?.authManager?.handleDeleteContributionResult(success: true, error: nil)
            }
        }
    }
    
    private func fetchUserData(
        for uid: String,
        fallbackPhotoUrl: String = "",
        completion: @escaping (String, Bool, String, String) -> Void
    ) {
        let db = Firestore.firestore()
        db.collection("users").document(uid).getDocument { [weak self] document, _ in
            if let document = document, document.exists {
                let data = document.data()
                let username = data?["username"] as? String ?? ""
                let displayName = data?["displayName"] as? String ?? ""
                let isAdmin = data?["isAdmin"] as? Bool ?? false
                let storedPhotoUrl = (data?["photoUrl"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
                let effectivePhotoUrl = storedPhotoUrl.isEmpty ? fallbackPhotoUrl : storedPhotoUrl
                let photoData = data?["photoData"] as? String ?? ""
                if uid == Auth.auth().currentUser?.uid {
                    self?.syncPublicProfile(
                        uid: uid,
                        displayName: displayName,
                        username: username,
                        photoUrl: effectivePhotoUrl,
                        photoData: photoData
                    )
                }
                completion(username, isAdmin, effectivePhotoUrl, photoData)
            } else {
                completion("", false, fallbackPhotoUrl, "")
            }
        }
    }

    private func ensureUserDocument(
        for user: User,
        fallbackName: String? = nil,
        completion: @escaping (String, Bool, String, String) -> Void
    ) {
        let db = Firestore.firestore()
        let docRef = db.collection("users").document(user.uid)
        let googlePhotoUrl = resolvedPhotoUrl(for: user)
        docRef.getDocument { [weak self] snapshot, _ in
            if let snapshot = snapshot, snapshot.exists, let data = snapshot.data() {
                let username = data["username"] as? String ?? ""
                let displayName = (data["displayName"] as? String) ?? user.displayName ?? ""
                let isAdmin = data["isAdmin"] as? Bool ?? false
                let storedPhotoUrl = (data["photoUrl"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
                let effectivePhotoUrl = storedPhotoUrl.isEmpty ? googlePhotoUrl : storedPhotoUrl
                let photoData = data["photoData"] as? String ?? ""
                if storedPhotoUrl.isEmpty && !googlePhotoUrl.isEmpty {
                    docRef.updateData(["photoUrl": googlePhotoUrl])
                }
                self?.syncPublicProfile(
                    uid: user.uid,
                    displayName: displayName,
                    username: username,
                    photoUrl: effectivePhotoUrl,
                    photoData: photoData
                )
                completion(username, isAdmin, effectivePhotoUrl, photoData)
                return
            }

            let email = user.email ?? ""
            let baseName = [user.displayName, fallbackName, email.components(separatedBy: "@").first]
                .compactMap { $0?.trimmingCharacters(in: .whitespacesAndNewlines) }
                .first(where: { !$0.isEmpty }) ?? "Guitarrista"
            let username = baseName
                .lowercased()
                .replacingOccurrences(of: " ", with: "_")

            var payload: [String: Any] = [
                "email": email,
                "displayName": baseName,
                "username": username,
                "isAdmin": false,
                "joinDate": Int64(Date().timeIntervalSince1970 * 1000)
            ]
            if !googlePhotoUrl.isEmpty {
                payload["photoUrl"] = googlePhotoUrl
            }
            docRef.setData(payload) { _ in
                self?.syncPublicProfile(
                    uid: user.uid,
                    displayName: baseName,
                    username: username,
                    photoUrl: googlePhotoUrl,
                    photoData: ""
                )
                completion(username, false, googlePhotoUrl, "")
            }
        }
    }

    private func executeSendPasswordReset(email: String) {
        Auth.auth().sendPasswordReset(withEmail: email) { [weak self] error in
            if let error = error {
                self?.authManager?.handlePasswordResetResult(success: false, error: self?.friendlyAuthError(error))
            } else {
                self?.authManager?.handlePasswordResetResult(success: true, error: nil)
            }
        }
    }

    private func executeDeleteAccount() {
        guard let user = Auth.auth().currentUser else {
            authManager?.handleDeleteAccountResult(success: false, error: "No hay una sesión activa.")
            return
        }

        let uid = user.uid
        let db = Firestore.firestore()
        let userRef = db.collection("users").document(uid)
        let publicRef = db.collection("public_profiles").document(uid)
        let backupRef = userRef.collection("backups").document("latest")

        userRef.getDocument { [weak self] userSnap, _ in
            let savedUserData = userSnap?.data()
            backupRef.getDocument { backupSnap, _ in
                let savedBackupData = backupSnap?.data()

                let finishDeletion = {
                    user.delete { deleteError in
                        if let deleteError = deleteError {
                            if let savedUserData = savedUserData {
                                userRef.setData(savedUserData)
                            }
                            if let savedBackupData = savedBackupData {
                                backupRef.setData(savedBackupData)
                            }
                            self?.authManager?.handleDeleteAccountResult(
                                success: false,
                                error: self?.friendlyAuthError(deleteError)
                            )
                        } else {
                            try? Auth.auth().signOut()
                            self?.authManager?.handleDeleteAccountResult(success: true, error: nil)
                        }
                    }
                }

                backupRef.delete { _ in
                    publicRef.delete { _ in
                        userRef.delete { _ in
                            finishDeletion()
                        }
                    }
                }
            }
        }
    }

    private func executeGoogleSignIn() {
        let provider = OAuthProvider(providerID: "google.com")
        provider.customParameters = ["prompt": "select_account"]
        provider.getCredentialWith(nil) { [weak self] credential, error in
            guard let self = self else { return }
            if let error = error {
                self.authManager?.handleGoogleSignInResult(
                    uid: "", email: "", displayName: "", username: "", isAdmin: false, linkedProvidersCsv: "", photoUrl: "", photoData: "",
                    error: self.friendlyAuthError(error)
                )
                return
            }
            guard let credential = credential else {
                self.authManager?.handleGoogleSignInResult(
                    uid: "", email: "", displayName: "", username: "", isAdmin: false, linkedProvidersCsv: "", photoUrl: "", photoData: "",
                    error: "No se obtuvo credencial de Google."
                )
                return
            }
            Auth.auth().signIn(with: credential) { authResult, signInError in
                if let signInError = signInError {
                    self.authManager?.handleGoogleSignInResult(
                        uid: "", email: "", displayName: "", username: "", isAdmin: false, linkedProvidersCsv: "", photoUrl: "", photoData: "",
                        error: self.friendlyAuthError(signInError)
                    )
                    return
                }
                guard let user = authResult?.user else {
                    self.authManager?.handleGoogleSignInResult(
                        uid: "", email: "", displayName: "", username: "", isAdmin: false, linkedProvidersCsv: "", photoUrl: "", photoData: "",
                        error: "Usuario de Google no encontrado."
                    )
                    return
                }
                let providersCsv = self.linkedProvidersCsv(for: user)
                self.ensureUserDocument(for: user) { username, isAdmin, photoUrl, photoData in
                    self.authManager?.handleGoogleSignInResult(
                        uid: user.uid,
                        email: user.email ?? "",
                        displayName: user.displayName ?? "",
                        username: username,
                        isAdmin: isAdmin,
                        linkedProvidersCsv: providersCsv,
                        photoUrl: photoUrl,
                        photoData: photoData,
                        error: nil
                    )
                }
            }
        }
    }

    private func executeLinkWithGoogle() {
        guard let currentUser = Auth.auth().currentUser else {
            authManager?.handleLinkGoogleResult(
                uid: "", email: "", displayName: "", username: "", isAdmin: false, linkedProvidersCsv: "", photoUrl: "", photoData: "",
                error: "Debes iniciar sesión antes de vincular Google."
            )
            return
        }
        let provider = OAuthProvider(providerID: "google.com")
        provider.customParameters = ["prompt": "select_account"]
        provider.getCredentialWith(nil) { [weak self] credential, error in
            guard let self = self else { return }
            if let error = error {
                self.authManager?.handleLinkGoogleResult(
                    uid: "", email: "", displayName: "", username: "", isAdmin: false, linkedProvidersCsv: "", photoUrl: "", photoData: "",
                    error: self.friendlyAuthError(error)
                )
                return
            }
            guard let credential = credential else {
                self.authManager?.handleLinkGoogleResult(
                    uid: "", email: "", displayName: "", username: "", isAdmin: false, linkedProvidersCsv: "", photoUrl: "", photoData: "",
                    error: "No se obtuvo credencial de Google."
                )
                return
            }
            currentUser.link(with: credential) { authResult, linkError in
                if let linkError = linkError {
                    self.authManager?.handleLinkGoogleResult(
                        uid: "", email: "", displayName: "", username: "", isAdmin: false, linkedProvidersCsv: "", photoUrl: "", photoData: "",
                        error: self.friendlyAuthError(linkError)
                    )
                    return
                }
                let updated = authResult?.user ?? currentUser
                updated.reload { _ in
                    let refreshed = Auth.auth().currentUser ?? updated
                    let providersCsv = self.linkedProvidersCsv(for: refreshed)
                    let googlePhotoUrl = self.resolvedPhotoUrl(for: refreshed)
                    let finishLink: () -> Void = {
                        self.fetchUserData(for: refreshed.uid, fallbackPhotoUrl: googlePhotoUrl) { username, isAdmin, photoUrl, photoData in
                            self.authManager?.handleLinkGoogleResult(
                                uid: refreshed.uid,
                                email: refreshed.email ?? "",
                                displayName: refreshed.displayName ?? "",
                                username: username,
                                isAdmin: isAdmin,
                                linkedProvidersCsv: providersCsv,
                                photoUrl: photoUrl,
                                photoData: photoData,
                                error: nil
                            )
                        }
                    }
                    if !googlePhotoUrl.isEmpty {
                        self.syncPublicProfile(uid: refreshed.uid, photoUrl: googlePhotoUrl)
                        Firestore.firestore().collection("users").document(refreshed.uid)
                            .updateData(["photoUrl": googlePhotoUrl]) { _ in
                                finishLink()
                            }
                    } else {
                        finishLink()
                    }
                }
            }
        }
    }

    private func friendlyAuthError(_ error: Error) -> String {
        let nsError = error as NSError
        if let code = AuthErrorCode(rawValue: nsError.code) {
            switch code {
            case .operationNotAllowed:
                return "Proveedor desactivado en Firebase Console. Actívalo en Authentication → Sign-in method."
            case .requiresRecentLogin:
                return "Por seguridad, cierra sesión y vuelve a entrar antes de eliminar tu cuenta."
            case .invalidEmail:
                return "El formato del correo electrónico no es válido."
            case .userNotFound:
                return "No existe una cuenta registrada con ese correo."
            case .accountExistsWithDifferentCredential:
                return "Ya existe una cuenta con este correo usando otro método de acceso."
            case .providerAlreadyLinked:
                return "Este proveedor ya está vinculado a tu cuenta."
            case .credentialAlreadyInUse:
                return "Esa cuenta externa ya está vinculada a otro usuario de Riff Forge."
            case .webContextCancelled:
                return "Operación cancelada."
            default:
                break
            }
        }
        return error.localizedDescription
    }
}
