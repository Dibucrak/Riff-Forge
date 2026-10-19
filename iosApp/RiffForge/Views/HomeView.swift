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

// MARK: - Liquid Glass Reusable Components

/// Liquid Glass Card — uses Apple's native `.ultraThinMaterial` for true frosted glass,
/// combined with an inner stroke for edge highlights and a soft colored shadow for depth.
struct GlassCard<Content: View>: View {
    let content: Content
    let cornerRadius: CGFloat
    let shadowColor: Color
    let onTap: (() -> Void)?
    
    init(
        cornerRadius: CGFloat = 16,
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
        content
            .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: cornerRadius))
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius)
                    .stroke(
                        LinearGradient(
                            colors: [
                                .white.opacity(0.5),
                                .white.opacity(0.25),
                                .white.opacity(0)
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: 0.5
                    )
            )
            .shadow(color: shadowColor, radius: 8, x: 0, y: 4)
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius))
            .contentShape(RoundedRectangle(cornerRadius: cornerRadius))
            .animation(.easeInOut(duration: 0.2), value: onTap)
            .simulatedClick(action: onTap)
    }
}

/// Elevated Glass Card — higher material and larger shadow for hero sections.
struct GlassCardElevated<Content: View>: View {
    let content: Content
    let cornerRadius: CGFloat
    
    init(
        cornerRadius: CGFloat = 20,
        @ViewBuilder content: () -> Content
    ) {
        self.content = content()
        self.cornerRadius = cornerRadius
    }
    
    var body: some View {
        content
            .background(.regularMaterial, in: RoundedRectangle(cornerRadius: cornerRadius))
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius)
                    .stroke(
                        LinearGradient(
                            colors: [
                                .white.opacity(0.6),
                                .white.opacity(0.3),
                                .white.opacity(0)
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: 0.5
                    )
            )
            .shadow(color: .black.opacity(0.18), radius: 16, x: 0, y: 8)
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius))
    }
}

/// Stat Chip — small glass panel for displaying a single metric.
struct GlassStatChip: View {
    let label: String
    let value: String
    let accentColor: Color
    
    var body: some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(accentColor)
            Text(label)
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 12))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(
                    LinearGradient(
                        colors: [
                            accentColor.opacity(0.4),
                            .clear
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    lineWidth: 0.5
                )
        )
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

/// Accent Button — vibrant gradient glass button for primary CTAs.
struct GlassAccentButton: View {
    let title: String
    let icon: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.caption)
                Text(title)
                    .fontWeight(.semibold)
            }
            .foregroundColor(.primary)
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(
                LinearGradient(
                    colors: [
                        Color.orange,
                        Color(red: 0.9, green: 0.55, blue: 0.0)
                    ],
                    startPoint: .leading,
                    endPoint: .trailing
                ),
                in: Capsule()
            )
            .overlay(
                Capsule()
                    .stroke(
                        LinearGradient(
                            colors: [
                                Color.orange.opacity(0.9),
                                Color.yellow.opacity(0.6)
                            ],
                            startPoint: .leading,
                            endPoint: .trailing
                        ),
                        lineWidth: 0.5
                    )
            )
            .shadow(color: .orange.opacity(0.3), radius: 6, x: 0, y: 3)
        }
    }
}

// MARK: - Background

/// Liquid Glass Background — vibrant gradient with ambient glow orbs.
struct GlassBackground<Content: View>: View {
    let content: Content
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        ZStack {
            // Base gradient — neutral dark per HIG
            LinearGradient(
                colors: [
                    Color.black,
                    Color(red: 0.06, green: 0.06, blue: 0.08),
                    Color.black
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            // Ambient glow orbs — subtle orange tint
            Circle()
                .fill(
                    RadialGradient(
                        colors: [
                            Color.orange.opacity(0.5),
                            .clear
                        ],
                        center: .center,
                        startRadius: 0,
                        endRadius: 100
                    )
                )
                .frame(width: 200, height: 200)
                .opacity(0.15)
                .offset(x: -60, y: -60)
            
            Circle()
                .fill(
                    RadialGradient(
                        colors: [
                            Color(red: 0.9, green: 0.55, blue: 0.0).opacity(0.4),
                            .clear
                        ],
                        center: .center,
                        startRadius: 0,
                        endRadius: 125
                    )
                )
                .frame(width: 250, height: 250)
                .opacity(0.12)
                .offset(x: 60, y: 80)
            
            Circle()
                .fill(
                    RadialGradient(
                        colors: [
                            Color.orange.opacity(0.3),
                            .clear
                        ],
                        center: .center,
                        startRadius: 0,
                        endRadius: 90
                    )
                )
                .frame(width: 180, height: 180)
                .opacity(0.1)
                .offset(x: -40, y: 100)
            
            // Content
            content
        }
    }
}

// MARK: - Home Screen

/**
 * Liquid Glass Home Screen — the main dashboard for Riff Forge on iOS.
 *
 * Visual design:
 *   - Full-screen vibrant gradient background with ambient glow orbs
 *   - Overlapping frosted glass panels using Apple's native `.ultraThinMaterial` and `.regularMaterial`
 *   - Each panel has a 0.5px gradient inner stroke simulating specular light on glass edges
 *   - Soft colored shadows (purple/cyan) for volumetric depth
 *   - Stat chips with accent-colored borders
 *   - Horizontal scrolling glass cards for quick actions
 *   - Primary CTA uses an accent gradient glass button
 */
struct HomeView: View {
    
    // Sample data
    let quickActions = [
        QuickActionItem(title: "Nuevo Riff", systemImage: "plus.circle.fill", accentColor: .orange),
        QuickActionItem(title: "Afinador", systemImage: "mic.fill", accentColor: .blue),
        QuickActionItem(title: "Metrónomo", systemImage: "timer.fill", accentColor: .orange),
        QuickActionItem(title: "Escalas", systemImage: "music.note.list", accentColor: .green)
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
                    
                    // ── Header / Greeting ──────────────────────────────
                    Spacer().frame(height: 16)
                    
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Bienvenido")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                            Text("Riff Forge")
                                .font(.headline)
                                .fontWeight(.bold)
                                .foregroundColor(.primary)
                        }
                        
                        Spacer()
                        
                        // Avatar / Profile
                        Image(systemName: "person.circle.fill")
                            .font(.title2)
                            .foregroundColor(.orange)
                            .padding(8)
                            .background(.ultraThinMaterial, in: Circle())
                            .overlay(
                                Circle()
                                    .stroke(.white.opacity(0.15), lineWidth: 0.5)
                            )
                    }
                    .padding(.horizontal, 16)
                    
                    Spacer().frame(height: 24)
                    
                    // ── Stats Row ──────────────────────────────────────
                    HStack(spacing: 8) {
                        GlassStatChip(label: "Riffs", value: "12", accentColor: .orange)
                        GlassStatChip(label: "Práctica", value: "3.5h", accentColor: .green)
                        GlassStatChip(label: "Escala", value: "Am", accentColor: .yellow)
                    }
                    .padding(.horizontal, 16)
                    
                    Spacer().frame(height: 28)
                    
                    // ── Quick Actions ──────────────────────────────────
                    VStack(alignment: .leading, spacing: 12) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Acciones Rápidas")
                                .font(.title3)
                                .fontWeight(.semibold)
                                .foregroundColor(.primary)
                            Text("Crea y explora")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                        .padding(.horizontal, 16)
                        
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 12) {
                                ForEach(quickActions) { action in
                                    GlassCardElevated(cornerRadius: 20) {
                                        VStack(spacing: 8) {
                                            Image(systemName: action.systemImage)
                                                .font(.title3)
                                                .foregroundColor(action.accentColor)
                                            Text(action.title)
                                                .font(.subheadline)
                                                .fontWeight(.semibold)
                                                .foregroundColor(.primary)
                                        }
                                        .frame(width: 140, height: 100)
                                    }
                                }
                            }
                            .padding(.horizontal, 16)
                        }
                    }
                    
                    Spacer().frame(height: 28)
                    
                    // ── Featured / Hero Card ───────────────────────────
                    GlassCardElevated(cornerRadius: 20) {
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Continuar donde lo dejaste")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                Text("Sunset Vibes")
                                    .font(.title3)
                                    .fontWeight(.bold)
                                    .foregroundColor(.primary)
                                Text("Dm · 120 BPM · Hace 2 horas")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                            
                            Spacer()
                            
                            GlassAccentButton(title: "Jugar", icon: "play.fill") {
                                // Play action
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                    }
                    .padding(.horizontal, 16)
                    
                    Spacer().frame(height: 28)
                    
                    // ── Recent Riffs ───────────────────────────────────
                    VStack(alignment: .leading, spacing: 12) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Riffs Recientes")
                                .font(.title3)
                                .fontWeight(.semibold)
                                .foregroundColor(.primary)
                            Text("Tu colección")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                        .padding(.horizontal, 16)
                        
                        VStack(spacing: 8) {
                            ForEach(recentRiffs) { riff in
                                GlassCard(cornerRadius: 16) {
                                    HStack {
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(riff.title)
                                                .font(.subheadline)
                                                .fontWeight(.bold)
                                                .foregroundColor(.primary)
                                            Text("\(riff.key)  ·  \(riff.tempo) BPM")
                                                .font(.caption)
                                                .foregroundColor(.secondary)
                                        }
                                        
                                        Spacer()
                                        
                                        VStack(alignment: .trailing, spacing: 4) {
                                            Text(riff.lastPlayed)
                                                .font(.caption2)
                                                .foregroundColor(.tertiary)
                                            Image(systemName: "play.circle.fill")
                                                .font(.caption)
                                                .foregroundColor(.orange)
                                        }
                                    }
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 8)
                                }
                                .padding(.horizontal, 16)
                            }
                        }
                    }
                    
                    Spacer().frame(height: 32)
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

// MARK: - Helper Extension

extension View {
    /// Simulates a click action for views that don't natively support taps.
    func simulatedClick(action: (() -> Void)?) -> some View {
        modifier(SimulatedClickModifier(action: action))
    }
}

struct SimulatedClickModifier: ViewModifier {
    let action: (() -> Void)?
    
    func body(content: Content) -> some View {
        if let action = action {
            content.onTapGesture(perform: action)
        } else {
            content
        }
    }
}
