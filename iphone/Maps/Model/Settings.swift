import Combine

/// The settings
@objc class Settings: NSObject {
    // MARK: Properties
    
    /// Key for storing if the sync beta alert has been shown in the user defaults
    static private let userDefaultsKeyHasShownSyncBetaAlert = "kUDDidShowICloudSynchronizationEnablingAlert"
    
    
    /// The current distance unit
    static var distanceUnit: DistanceUnit {
        get {
            if SettingsBridge.measurementUnits() == .imperial {
                return .imperial
            } else {
                return .metric
            }
        }
        set {
            if newValue == .imperial {
                SettingsBridge.setMeasurementUnits(.imperial)
            } else {
                SettingsBridge.setMeasurementUnits(.metric)
            }
        }
    }
    
    
    /// If zoom buttons should be displayed
    @objc static var hasZoomButtons: Bool {
        get {
            return SettingsBridge.zoomButtonsEnabled()
        }
        set {
            SettingsBridge.setZoomButtonsEnabled(newValue)
        }
    }
    
    
    /// If 3D buildings should be displayed
    @objc static var has3dBuildings: Bool {
        get {
            return SettingsBridge.buildings3dViewEnabled()
        }
        set {
            SettingsBridge.setBuildings3dViewEnabled(newValue)
        }
    }
    
    
    /// If automatic map downloads should be enabled
    @objc static var hasAutomaticDownload: Bool {
        get {
            return SettingsBridge.autoDownloadEnabled()
        }
        set {
            SettingsBridge.setAutoDownloadEnabled(newValue)
        }
    }
    
    
    /// The current mobile data policy
    @objc static var mobileDataPolicy: MobileDataPolicy {
        get {
            let networkPolicyPermission = NetworkPolicy.shared().permission
            if networkPolicyPermission == .always {
                return .always
            } else if networkPolicyPermission == .never {
                return .never
            } else {
                return .ask
            }
        }
        set {
            if newValue == .always {
                NetworkPolicy.shared().permission = .always
            } else if newValue == .never {
                NetworkPolicy.shared().permission = .never
            } else {
                NetworkPolicy.shared().permission = .ask
            }
        }
    }
    
    
    /// The current power saving mode
    @objc static var powerSavingMode: PowerSavingMode {
        get {
            return PowerSavingMode(rawValue: SettingsBridge.powerManagement()) ?? .never
        }
        set {
            SettingsBridge.setPowerManagement(newValue.rawValue)
        }
    }
    
    
    /// If an increased font size should be used for map labels
    @objc static var hasIncreasedFontsize: Bool {
        get {
            return SettingsBridge.largeFontSize()
        }
        set {
            SettingsBridge.setLargeFontSize(newValue)
        }
    }
    
    
    /// If names should be transliterated to Latin
    @objc static var shouldTransliterateToLatin: Bool {
        get {
            return SettingsBridge.transliteration()
        }
        set {
            SettingsBridge.setTransliteration(newValue)
        }
    }
    
    
    /// If the compass should be calibrated
    @objc static var shouldCalibrateCompass: Bool {
        get {
            return SettingsBridge.compassCalibrationEnabled()
        }
        set {
            SettingsBridge.setCompassCalibrationEnabled(newValue)
        }
    }
    
    
    /// The current appearance
    @objc static var appearance: Appearance {
        get {
            let theme = SettingsBridge.theme()
            if theme == MWMTheme.day {
                return .light
            } else if theme == MWMTheme.night {
                return .dark
            } else {
                return .auto
            }
        }
        set {
            if newValue == .light {
                SettingsBridge.setTheme(MWMTheme.day)
            } else if newValue == .dark {
                SettingsBridge.setTheme(MWMTheme.night)
            } else {
                SettingsBridge.setTheme(MWMTheme.auto)
            }
        }
    }
    
    
    /// If the bookmarks should be synced via iCloud
    @objc static var shouldSync: Bool {
        get {
            return SettingsBridge.iCLoudSynchronizationEnabled()
        }
        set {
            SettingsBridge.setICLoudSynchronizationEnabled(newValue)
        }
    }
    
    
    /// If the sync beta alert has been shown
    @objc static var hasShownSyncBetaAlert: Bool {
        get {
            return UserDefaults.standard.bool(forKey: userDefaultsKeyHasShownSyncBetaAlert)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: userDefaultsKeyHasShownSyncBetaAlert)
        }
    }
    
    
    /// The publisher for state changes of the iCloud sync
    static var syncStatePublisher: AnyPublisher<SynchronizationManagerState, Never> {
        iCloudSynchronizaionManager.shared.notifyObservers()
        return iCloudSynchronizaionManager.shared.statePublisher
    }
    
    
    /// If our custom logging is enabled
    @objc static var isLogging: Bool {
        get {
            return SettingsBridge.isFileLoggingEnabled()
        }
        set {
            SettingsBridge.setFileLoggingEnabled(newValue)
        }
    }
    
    
    /// The formatter for the size of the log file
    @objc static var logSizeFormatter: MeasurementFormatter {
        let measurementFormatter = MeasurementFormatter()
        measurementFormatter.unitStyle = .medium
        measurementFormatter.unitOptions = .naturalScale
        return measurementFormatter
    }
    
    
    /// The size of the log file in bytes
    @objc static var logSize: Measurement<UnitInformationStorage> {
        return Measurement<UnitInformationStorage>(value: Double(SettingsBridge.logFileSize()), unit: .bytes)
    }
    
    
    /// If the perspective view should be used during routing
    @objc static var hasPerspectiveViewWhileRouting: Bool {
        get {
            return SettingsBridge.perspectiveViewEnabled()
        }
        set {
            SettingsBridge.setPerspectiveViewEnabled(newValue)
        }
    }
    
    
    /// If auto zoom should be used during routing
    @objc static var hasAutoZoomWhileRouting: Bool {
        get {
            return SettingsBridge.autoZoomEnabled()
        }
        set {
            SettingsBridge.setAutoZoomEnabled(newValue)
        }
    }
    
    
    /// If voice guidance should be provided during routing
    @objc static var shouldProvideVoiceRouting: Bool {
        get {
            return MWMTextToSpeech.isTTSEnabled()
        }
        set {
            MWMTextToSpeech.setTTSEnabled(newValue)
        }
    }
    
    
    /// The available languages for voice guidance during routing
    static var availableLanguagesForVoiceRouting: [VoiceRoutingLanguage] {
        return MWMTextToSpeech.availableLanguages().map { language in
            return VoiceRoutingLanguage(id: language.key, localizedName: language.value)
        }.sorted()
    }
    
    
    /// The current language for voice guidance during routing
    @objc static var languageForVoiceRouting: VoiceRoutingLanguage.ID {
        get {
            return MWMTextToSpeech.selectedLanguage()
        }
        set {
            MWMTextToSpeech.tts().setNotificationsLocale(newValue)
        }
    }
    
    
    /// If street names should be announced in the voice guidance during routing
    @objc static var shouldAnnounceStreetnamesWhileVoiceRouting: Bool {
        get {
            return MWMTextToSpeech.isStreetNamesTTSEnabled()
        }
        set {
            MWMTextToSpeech.setStreetNamesTTSEnabled(newValue)
        }
    }
    
    
    /// The current announcement of speed traps in the voice guidance during routing
    @objc static var announcingSpeedTrapsWhileVoiceRouting: AnnouncingSpeedTrapsWhileVoiceRouting {
        get {
            return AnnouncingSpeedTrapsWhileVoiceRouting(rawValue: MWMTextToSpeech.speedCameraMode()) ?? .never
        }
        set {
            MWMTextToSpeech.setSpeedCameraMode(newValue.rawValue)
        }
    }
    
    
    /// If toll roads should be avoided during routing
    @objc static var shouldAvoidTollRoadsWhileRouting: Bool {
        get {
            return RoutingOptions().avoidToll
        }
        set {
            var routingOptions = RoutingOptions()
            routingOptions.avoidToll = newValue
            routingOptions.save()
        }
    }
    
    
    /// If unpaved roads should be avoided during routing
    @objc static var shouldAvoidUnpavedRoadsWhileRouting: Bool {
        get {
            return RoutingOptions().avoidDirty
        }
        set {
            var routingOptions = RoutingOptions()
            routingOptions.avoidDirty = newValue
            routingOptions.save()
        }
    }
    
    
    /// If ferries should be avoided during routing
    @objc static var shouldAvoidFerriesWhileRouting: Bool {
        get {
            return RoutingOptions().avoidFerry
        }
        set {
            var routingOptions = RoutingOptions()
            routingOptions.avoidFerry = newValue
            routingOptions.save()
        }
    }
    
    
    /// If motorways should be avoided during routing
    @objc static var shouldAvoidMotorwaysWhileRouting: Bool {
        get {
            return RoutingOptions().avoidMotorway
        }
        set {
            var routingOptions = RoutingOptions()
            routingOptions.avoidMotorway = newValue
            routingOptions.save()
        }
    }
    
    
    
    // MARK: Methods
    
    /// Create a bookmarks backup before enabling the sync beta
    /// - Parameter completionHandler: A compeltion handler, which returns if a backup has been created
    static func createBookmarksBackupBecauseOfSyncBeta(completionHandler: ((_ hasCreatedBackup: Bool) -> Void)?) {
        BookmarksManager.shared().shareAllCategories { status, url in
            switch status {
                case .success:
                    let window = (UIApplication.shared.connectedScenes.filter { $0.activationState == .foregroundActive }.first(where: { $0 is UIWindowScene }) as? UIWindowScene)?.keyWindow
                    if let viewController = window?.rootViewController?.presentedViewController {
                        let shareController = ActivityViewController.share(for: url, message: String(localized: "share_bookmarks_email_body")) { _, _, _, _ in
                            completionHandler?(true)
                        }
                        shareController.present(inParentViewController: viewController, anchorView: nil)
                    }
                case .emptyCategory:
                    Toast.show(withText: String(localized: "bookmarks_error_title_share_empty"))
                    completionHandler?(false)
                case .archiveError:
                    Toast.show(withText: String(localized: "dialog_routing_system_error"))
                    completionHandler?(false)
                case .fileError:
                    Toast.show(withText: String(localized: "dialog_routing_system_error"))
                    completionHandler?(false)
            }
        }
    }
    
    
    /// Play a test audio snippet for voice guidance during routing
    @objc static func playVoiceRoutingTest() {
        MWMTextToSpeech.playTest()
    }
}
