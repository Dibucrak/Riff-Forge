import SwiftUI

// NOTE [KMP Architecture]:
// This SwiftUI HomeView was created as a native reference/prototype.
// The unified, production UI across iOS, Android and macOS is powered by Compose Multiplatform
// in `composeApp/src/commonMain/kotlin/com/riffforge/feature_home/presentation/HomeScreen.kt`,
// which is rendered seamlessly on iOS via `PlatformKt.MainViewController()` in `SceneDelegate.swift`.

// MARK: - Data Models

struct QuickActionItem: Identifiable {
    let id = UUID()
    let title: String
    let subtitle: String
    let systemImage: String
    let accentColor: Color
}

struct RecentRiffItem: Identifiable {
    let id = UUID()
    let title: String
    let key: String
    let tempo: Int
    let lastPlayed: String
}

// MARK: - Apple HIG Button Styles

struct GlassPressableButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.96 : 1.0)
            .opacity(configuration.isPressed ? 0.88 : 1.0)
            .animation(.spring(response: 0.25, dampingFraction: 0.7), value: configuration.isPressed)
    }
}

// MARK: - Liquid Glass Reusable Components

/// Liquid Glass Card — follows Apple HIG continuous curvature and native materials.
struct GlassCard<Content: View>: View {
    let content: Content
    let cornerRadius: CGFloat
    let shadowColor: Color
    let onTap: (() -> Void)?
    
    init(
        cornerRadius: CGFloat = 18,
        shadowColor: Color = .black.opacity(0.12),
        onTap: (() -> Void)? = nil,
        @ViewBuilder content: () -> Content
    ) {
        self.content = content()
        self.cornerRadius = cornerRadius
        self.shadowColor = shadowColor
        self.onTap = onTap
    }
    
    var body: some View {
        Group {
            if let onTap = onTap {
                Button(action: onTap) {
                    cardBody
                }
                .buttonStyle(GlassPressableButtonStyle())
            } else {
                cardBody
            }
        }
    }
    
    private var cardBody: some View {
        content
            .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .stroke(
                        LinearGradient(
                            colors: [
                                .white.opacity(0.4),
                                .white.opacity(0.15),
                                .white.opacity(0.0)
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: 0.5
                    )
            )
            .shadow(color: shadowColor, radius: 8, x: 0, y: 4)
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
    }
}

/// Elevated Glass Card — higher material and larger shadow for hero sections.
struct GlassCardElevated<Content: View>: View {
    let content: Content
    let cornerRadius: CGFloat
    
    init(
        cornerRadius: CGFloat = 22,
        @ViewBuilder content: () -> Content
    ) {
        self.content = content()
        self.cornerRadius = cornerRadius
    }
    
    var body: some View {
        content
            .background(.regularMaterial, in: RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .stroke(
                        LinearGradient(
                            colors: [
                                .white.opacity(0.5),
                                .white.opacity(0.2),
                                .white.opacity(0.0)
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: 0.5
                    )
            )
            .shadow(color: .black.opacity(0.16), radius: 14, x: 0, y: 7)
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
    }
}

/// Stat Chip — single metric on glass following Apple HIG.
struct GlassStatChip: View {
    let label: String
    let value: String
    let accentColor: Color
    
    var body: some View {
        VStack(spacing: 3) {
            Text(value)
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(accentColor)
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(
                    LinearGradient(
                        colors: [
                            .white.opacity(0.25),
                            .clear
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    lineWidth: 0.5
                )
        )
    }
}

/// Pill Badge for song metadata (Key, Tempo, Status).
struct GlassBadgeView: View {
    let text: String
    let tintColor: Color
    
    var body: some View {
        Text(text)
            .font(.caption2)
            .fontWeight(.semibold)
            .foregroundColor(tintColor)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(tintColor.opacity(0.12), in: RoundedRectangle(cornerRadius: 6, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 6, style: .continuous)
                    .stroke(tintColor.opacity(0.25), lineWidth: 0.5)
            )
    }
}

/// Primary Prominent Glass Button (Apple Filled Button Style).
struct GlassAccentButton: View {
    let title: String
    let icon: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                Text(title)
                    .font(.subheadline)
                    .fontWeight(.bold)
            }
            .foregroundColor(.white)
            .padding(.horizontal, 18)
            .padding(.vertical, 11)
            .background(
                LinearGradient(
                    colors: [
                        Color.orange,
                        Color(red: 0.92, green: 0.52, blue: 0.0)
                    ],
                    startPoint: .top,
                    endPoint: .bottom
                ),
                in: RoundedRectangle(cornerRadius: 14, style: .continuous)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .stroke(
                        LinearGradient(
                            colors: [
                                .white.opacity(0.4),
                                .clear
                            ],
                            startPoint: .top,
                            endPoint: .bottom
                        ),
                        lineWidth: 0.5
                    )
            )
            .shadow(color: Color.orange.opacity(0.25), radius: 6, x: 0, y: 3)
        }
        .buttonStyle(GlassPressableButtonStyle())
    }
}

// MARK: - Background

/// Liquid Glass Background — neutral dark system canvas with soft refractive glow.
struct GlassBackground<Content: View>: View {
    let content: Content
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        ZStack {
            // Base dark system background
            Color.black
                .ignoresSafeArea()
            
            // Soft ambient top-trailing glow
            Circle()
                .fill(
                    RadialGradient(
                        colors: [
                            Color.orange.opacity(0.2),
                            Color.clear
                        ],
                        center: .center,
                        startRadius: 0,
                        endRadius: 180
                    )
                )
                .frame(width: 360, height: 360)
                .offset(x: 100, y: -180)
                .ignoresSafeArea()
            
            content
        }
    }
}

// MARK: - Home Screen

/**
 * Liquid Glass Home Screen — Apple Human Interface Guidelines dashboard.
 */
struct HomeView: View {
    
    let quickActions = [
        QuickActionItem(title: "Afinador", subtitle: "Precisión cromática", systemImage: "tuningfork", accentColor: .orange),
        QuickActionItem(title: "Metrónomo", subtitle: "Tempo y compás", systemImage: "metronome.fill", accentColor: .yellow),
        QuickActionItem(title: "Escalas", subtitle: "Mástil y modos", systemImage: "music.note.list", accentColor: .green),
        QuickActionItem(title: "Acordes", subtitle: "Diccionario y digitación", systemImage: "book.fill", accentColor: .orange)
    ]
    
    let recentRiffs = [
        RecentRiffItem(title: "Sunset Vibes", key: "Dm", tempo: 120, lastPlayed: "Hace 2h"),
        RecentRiffItem(title: "Blues Shuffle", key: "A7", tempo: 95, lastPlayed: "Hace 5h"),
        RecentRiffItem(title: "Metal Riff", key: "Em", tempo: 140, lastPlayed: "Ayer"),
        RecentRiffItem(title: "Jazz Fusion", key: "Cmaj7", tempo: 110, lastPlayed: "Hace 2 días")
    ]
    
    var body: some View {
        GlassBackground {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    
                    // ── Large Title Header ─────────────────────────────
                    HStack(alignment: .center) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Panel Principal")
                                .font(.caption)
                                .fontWeight(.semibold)
                                .foregroundColor(.orange)
                            Text("Riff Forge")
                                .font(.title)
                                .fontWeight(.bold)
                                .foregroundColor(.primary)
                        }
                        
                        Spacer()
                        
                        // Profile Avatar Button
                        Button(action: { /* Profile action */ }) {
                            Image(systemName: "person.fill")
                                .font(.subheadline)
                                .foregroundColor(.orange)
                                .padding(10)
                                .background(.ultraThinMaterial, in: Circle())
                                .overlay(
                                    Circle()
                                        .stroke(.white.opacity(0.2), lineWidth: 0.5)
                                )
                        }
                        .buttonStyle(GlassPressableButtonStyle())
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 16)
                    
                    Spacer().frame(height: 20)
                    
                    // ── Hero / Continue Practice Card ──────────────────
                    GlassCardElevated(cornerRadius: 22) {
                        HStack(alignment: .center) {
                            VStack(alignment: .leading, spacing: 6) {
                                HStack(spacing: 6) {
                                    Circle()
                                        .fill(Color.green)
                                        .frame(width: 8, height: 8)
                                    Text("Continuar práctica")
                                        .font(.caption2)
                                        .fontWeight(.semibold)
                                        .foregroundColor(.secondary)
                                }
                                
                                Text("Sunset Vibes")
                                    .font(.title3)
                                    .fontWeight(.bold)
                                    .foregroundColor(.primary)
                                
                                HStack(spacing: 6) {
                                    GlassBadgeView(text: "Dm", tintColor: .orange)
                                    GlassBadgeView(text: "120 BPM", tintColor: .yellow)
                                    GlassBadgeView(text: "Hace 2h", tintColor: .secondary)
                                }
                            }
                            
                            Spacer()
                            
                            GlassAccentButton(title: "Tocar", icon: "play.fill") {
                                // Play riff action
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 16)
                    }
                    .padding(.horizontal, 16)
                    
                    Spacer().frame(height: 20)
                    
                    // ── Metrics Row ────────────────────────────────────
                    HStack(spacing: 8) {
                        GlassStatChip(label: "Riffs", value: "12", accentColor: .orange)
                        GlassStatChip(label: "Práctica", value: "3.5h", accentColor: .green)
                        GlassStatChip(label: "Tonalidad", value: "Am", accentColor: .yellow)
                    }
                    .padding(.horizontal, 16)
                    
                    Spacer().frame(height: 28)
                    
                    // ── Quick Actions ──────────────────────────────────
                    VStack(alignment: .leading, spacing: 10) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Acciones Rápidas")
                                .font(.headline)
                                .fontWeight(.bold)
                                .foregroundColor(.primary)
                            Text("Herramientas de uso frecuente")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                        .padding(.horizontal, 16)
                        
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                            ForEach(quickActions) { action in
                                GlassCard(cornerRadius: 16) {
                                    VStack(alignment: .leading, spacing: 8) {
                                        Image(systemName: action.systemImage)
                                            .font(.title3)
                                            .foregroundColor(action.accentColor)
                                            .padding(8)
                                            .background(action.accentColor.opacity(0.14), in: RoundedRectangle(cornerRadius: 10, style: .continuous))
                                        
                                        Text(action.title)
                                            .font(.subheadline)
                                            .fontWeight(.bold)
                                            .foregroundColor(.primary)
                                        
                                        Text(action.subtitle)
                                            .font(.caption)
                                            .foregroundColor(.secondary)
                                            .lineLimit(1)
                                    }
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .padding(14)
                                }
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                    
                    Spacer().frame(height: 28)
                    
                    // ── Recent Riffs (Apple Inset Grouped Section) ─────
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Riffs Recientes")
                                    .font(.headline)
                                    .fontWeight(.bold)
                                    .foregroundColor(.primary)
                                Text("Tu colección de práctica")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                            
                            Spacer()
                            
                            Button(action: { /* See all */ }) {
                                Text("Ver todos")
                                    .font(.subheadline)
                                    .fontWeight(.semibold)
                                    .foregroundColor(.orange)
                            }
                        }
                        .padding(.horizontal, 16)
                        
                        // Inset Grouped Card with Hairlines
                        GlassCard(cornerRadius: 18) {
                            VStack(spacing: 0) {
                                ForEach(Array(recentRiffs.enumerated()), id: \.element.id) { index, riff in
                                    HStack {
                                        Image(systemName: "music.note")
                                            .font(.caption)
                                            .foregroundColor(.orange)
                                            .padding(8)
                                            .background(Color.white.opacity(0.06), in: Circle())
                                        
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(riff.title)
                                                .font(.subheadline)
                                                .fontWeight(.semibold)
                                                .foregroundColor(.primary)
                                            Text("\(riff.key) · \(riff.tempo) BPM · \(riff.lastPlayed)")
                                                .font(.caption)
                                                .foregroundColor(.secondary)
                                        }
                                        
                                        Spacer()
                                        
                                        Button(action: { /* Play */ }) {
                                            Image(systemName: "play.circle.fill")
                                                .font(.title3)
                                                .foregroundColor(.orange)
                                        }
                                        .buttonStyle(GlassPressableButtonStyle())
                                    }
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 12)
                                    
                                    if index < recentRiffs.count - 1 {
                                        Divider()
                                            .opacity(0.3)
                                            .padding(.leading, 56)
                                    }
                                }
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                    
                    Spacer().frame(height: 36)
                }
            }
            .scrollIndicators(.hidden)
        }
    }
}

// MARK: - Preview

#Preview {
    HomeView()
}
