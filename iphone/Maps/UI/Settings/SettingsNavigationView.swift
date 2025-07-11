import SwiftUI

/// View for the navigation settings
struct SettingsNavigationView: View {
    // MARK: - Properties
    
    /// If the perspective view should be used during routing
    @State var hasPerspectiveViewWhileRouting: Bool = true
    
    
    /// If auto zoom should be used during routing
    @State var hasAutoZoomWhileRouting: Bool = true
    
    
    /// If voice guidance should be provided during routing
    @State var shouldProvideVoiceRouting: Bool = true
    
    
    /// The selected language for voice guidance during routing
    @State var selectedLanguageForVoiceRouting: Settings.VoiceRoutingLanguage.ID? = nil
    
    
    /// If street names should be announced in the voice guidance during routing
    @State var shouldAnnounceStreetnamesWhileVoiceRouting: Bool = false
    
    
    /// The selected announcement of speed traps in the voice guidance during routing
    @State var selectedAnnouncingSpeedTrapsWhileVoiceRouting: Settings.AnnouncingSpeedTrapsWhileVoiceRouting = .never
    
    
    /// If toll roads should be avoided during routing
    @State var shouldAvoidTollRoadsWhileRouting: Bool = false
    
    
    /// If unpaved roads should be avoided during routing
    @State var shouldAvoidUnpavedRoadsWhileRouting: Bool = false
    
    
    /// If ferries should be avoided during routing
    @State var shouldAvoidFerriesWhileRouting: Bool = false
    
    
    /// If motorways should be avoided during routing
    @State var shouldAvoidMotorwaysWhileRouting: Bool = false
    
    
    /// The actual view
    var body: some View {
        NavigationView {
            List {
                Section {
                    Toggle("pref_map_3d_title", isOn: $hasPerspectiveViewWhileRouting)
                        .tint(.accent)
                    
                    Toggle("pref_map_auto_zoom", isOn: $hasAutoZoomWhileRouting)
                        .tint(.accent)
                }
                
                Section {
                    Toggle("pref_tts_enable_title", isOn: $shouldProvideVoiceRouting)
                        .tint(.accent)
                    
                    if shouldProvideVoiceRouting {
                        Picker(selection: $selectedLanguageForVoiceRouting) {
                            ForEach(Settings.availableLanguagesForVoiceRouting) { languageForVoiceRouting in
                                Text(languageForVoiceRouting.localizedName)
                                    .tag(languageForVoiceRouting.id)
                            }
                        } label: {
                            Text("pref_tts_language_title")
                        }
                        
                        Toggle(isOn: $shouldAnnounceStreetnamesWhileVoiceRouting) {
                            VStack(alignment: .leading) {
                                Text("pref_tts_street_names_title")
                                
                                Text("pref_tts_street_names_description")
                                    .font(.footnote)
                                    .foregroundStyle(.secondary)
                            }
                        }
                        .tint(.accent)
                        
                        Picker(selection: $selectedAnnouncingSpeedTrapsWhileVoiceRouting) {
                            ForEach(Settings.AnnouncingSpeedTrapsWhileVoiceRouting.allCases) { announcingSpeedTrapsWhileVoiceRouting in
                                Text(announcingSpeedTrapsWhileVoiceRouting.description)
                            }
                        } label: {
                            Text("speedcams_alert_title")
                        }
                    }
                } header: {
                    Text("pref_tts_title")
                } footer: {
                    Button {
                        Settings.playVoiceRoutingTest()
                    } label: {
                        Text("pref_tts_test_voice_title")
                            .bold()
                            .lineLimit(1)
                            .padding(4)
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(BorderedButtonStyle())
                    .foregroundStyle(.alternativeAccent)
                    .padding([.top, .bottom])
                    .listRowInsets(.init(top: 0, leading: 0, bottom: 0, trailing: 0))
                }
                
                Section {
                    Toggle("avoid_tolls", isOn: $shouldAvoidTollRoadsWhileRouting)
                        .tint(.accent)
                    
                    Toggle("avoid_unpaved", isOn: $shouldAvoidUnpavedRoadsWhileRouting)
                        .tint(.accent)
                    
                    Toggle("avoid_ferry", isOn: $shouldAvoidFerriesWhileRouting)
                        .tint(.accent)
                    
                    Toggle("avoid_motorways", isOn: $shouldAvoidMotorwaysWhileRouting)
                        .tint(.accent)
                } header: {
                    Text("driving_options_title")
                }
            }
        }
        .accentColor(.accent)
        .navigationViewStyle(StackNavigationViewStyle())
        .navigationTitle(String(localized: "prefs_group_route"))
        .onAppear {
            hasPerspectiveViewWhileRouting = Settings.hasPerspectiveViewWhileRouting
            hasAutoZoomWhileRouting = Settings.hasAutoZoomWhileRouting
            shouldProvideVoiceRouting = Settings.shouldProvideVoiceRouting
            selectedLanguageForVoiceRouting = Settings.languageForVoiceRouting
            shouldAnnounceStreetnamesWhileVoiceRouting = Settings.shouldAnnounceStreetnamesWhileVoiceRouting
            selectedAnnouncingSpeedTrapsWhileVoiceRouting = Settings.announcingSpeedTrapsWhileVoiceRouting
            shouldAvoidTollRoadsWhileRouting = Settings.shouldAvoidTollRoadsWhileRouting
            shouldAvoidUnpavedRoadsWhileRouting = Settings.shouldAvoidUnpavedRoadsWhileRouting
            shouldAvoidFerriesWhileRouting = Settings.shouldAvoidFerriesWhileRouting
            shouldAvoidMotorwaysWhileRouting = Settings.shouldAvoidMotorwaysWhileRouting
        }
        .onChange(of: hasPerspectiveViewWhileRouting) { changedHasPerspectiveViewWhileRouting in
            Settings.hasPerspectiveViewWhileRouting = changedHasPerspectiveViewWhileRouting
        }
        .onChange(of: hasAutoZoomWhileRouting) { changedHasAutoZoomWhileRouting in
            Settings.hasAutoZoomWhileRouting = changedHasAutoZoomWhileRouting
        }
        .onChange(of: shouldProvideVoiceRouting) { changedShouldProvideVoiceRouting in
            Settings.shouldProvideVoiceRouting = changedShouldProvideVoiceRouting
        }
        .onChange(of: selectedLanguageForVoiceRouting) { changedSelectedLanguageForVoiceRouting in
            if let changedSelectedLanguageForVoiceRouting {
                Settings.languageForVoiceRouting = changedSelectedLanguageForVoiceRouting
            }
        }
        .onChange(of: shouldAnnounceStreetnamesWhileVoiceRouting) { changedShouldAnnounceStreetnamesWhileVoiceRouting in
            Settings.shouldAnnounceStreetnamesWhileVoiceRouting = changedShouldAnnounceStreetnamesWhileVoiceRouting
        }
        .onChange(of: selectedAnnouncingSpeedTrapsWhileVoiceRouting) { changedSelectedAnnouncingSpeedTrapsWhileVoiceRouting in
            Settings.announcingSpeedTrapsWhileVoiceRouting = changedSelectedAnnouncingSpeedTrapsWhileVoiceRouting
        }
        .onChange(of: shouldAvoidTollRoadsWhileRouting) { changedShouldAvoidTollRoadsWhileRouting in
            Settings.shouldAvoidTollRoadsWhileRouting = changedShouldAvoidTollRoadsWhileRouting
        }
        .onChange(of: shouldAvoidUnpavedRoadsWhileRouting) { changedShouldAvoidUnpavedRoadsWhileRouting in
            Settings.shouldAvoidUnpavedRoadsWhileRouting = changedShouldAvoidUnpavedRoadsWhileRouting
        }
        .onChange(of: shouldAvoidUnpavedRoadsWhileRouting) { changedShouldAvoidUnpavedRoadsWhileRouting in
            Settings.shouldAvoidUnpavedRoadsWhileRouting = changedShouldAvoidUnpavedRoadsWhileRouting
        }
        .onChange(of: shouldAvoidFerriesWhileRouting) { changedShouldAvoidFerriesWhileRouting in
            Settings.shouldAvoidFerriesWhileRouting = changedShouldAvoidFerriesWhileRouting
        }
        .onChange(of: shouldAvoidMotorwaysWhileRouting) { changedShouldAvoidMotorwaysWhileRouting in
            Settings.shouldAvoidMotorwaysWhileRouting = changedShouldAvoidMotorwaysWhileRouting
        }
    }
}
