import SwiftUI
import shared

@Observable
final class CalculatorViewModel {
    private let bridge = CalculatorBridge()
    let keypads: [Keypad]
    private(set) var state: CalculatorState
    private(set) var tapCount = 0

    init() {
        keypads = bridge.keypads
        state = bridge.currentState
        bridge.watch { [weak self] state in
            self?.state = state
        }
    }

    deinit {
        bridge.close()
    }

    func tap(_ keypad: Keypad) {
        tapCount += 1
        bridge.onKeypadClicked(keypad: keypad)
    }

    func isActive(_ keypad: Keypad) -> Bool {
        state.operation != Operation.none && keypad.operation == state.operation
    }
}

/// Native iOS 26 calculator: Liquid Glass keys over the brand mesh gradient,
/// driven by the shared Kotlin engine.
struct CalculatorGlassView: View {
    private static let keySpacing: CGFloat = 14

    @State private var viewModel = CalculatorViewModel()
    @State private var keySize: CGFloat = 76

    var body: some View {
        ZStack {
            CalculatorBackground()
                .ignoresSafeArea()
            VStack(spacing: 0) {
                display
                keypad
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 8)
        }
        .preferredColorScheme(.dark)
        .sensoryFeedback(.impact(weight: .light), trigger: viewModel.tapCount)
    }

    private var display: some View {
        let state = viewModel.state
        let hasResult = !state.displayedResult.isEmpty
        let mainText = hasResult
            ? state.displayedResult
            : (state.displayedCalculation.isEmpty ? "0" : state.displayedCalculation)
        return VStack(alignment: .trailing, spacing: 4) {
            Spacer(minLength: 0)
            Text(state.lastCalculation)
                .font(.title3)
                .foregroundStyle(.white.opacity(0.5))
            if hasResult {
                Text(state.displayedCalculation)
                    .font(.system(size: 34))
                    .foregroundStyle(.white.opacity(0.65))
            }
            Text(mainText)
                .font(.system(size: 96, weight: .light))
                // Long results such as 888888881201872 need to shrink further than the
                // rest of the display to fit every digit.
                .minimumScaleFactor(0.3)
                .foregroundStyle(.white)
                .contentTransition(.numericText())
                .animation(.snappy, value: mainText)
        }
        .lineLimit(1)
        .minimumScaleFactor(0.4)
        .frame(maxWidth: .infinity, alignment: .trailing)
        .padding(.horizontal, 12)
        .padding(.bottom, 20)
    }

    private var keypad: some View {
        let rows = stride(from: 0, to: viewModel.keypads.count, by: 4).map {
            Array(viewModel.keypads[$0..<min($0 + 4, viewModel.keypads.count)])
        }
        return GlassEffectContainer(spacing: Self.keySpacing) {
            Grid(horizontalSpacing: Self.keySpacing, verticalSpacing: Self.keySpacing) {
                ForEach(rows, id: \.self) { row in
                    GridRow {
                        ForEach(row, id: \.self) { keypad in
                            GlassKey(
                                keypad: keypad,
                                size: keySize,
                                isActive: viewModel.isActive(keypad),
                                action: { viewModel.tap(keypad) }
                            )
                        }
                    }
                }
            }
        }
        .frame(maxWidth: .infinity)
        .onGeometryChange(for: CGFloat.self) { proxy in
            proxy.size.width
        } action: { width in
            keySize = max(44, (width - Self.keySpacing * 3) / 4)
        }
    }
}

private struct GlassKey: View {
    let keypad: Keypad
    let size: CGFloat
    let isActive: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            label
                .font(.system(size: size * (keypad.label.count > 1 ? 0.32 : 0.42), weight: isActive ? .semibold : .regular))
                .foregroundStyle(isActive ? Color.brandViolet : .white)
                .frame(width: size, height: size)
                .contentShape(.circle)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(keypad == .undo ? "Delete" : keypad.label)
        .glassEffect(.regular.tint(tint).interactive(), in: .circle)
        .animation(.smooth, value: isActive)
    }

    @ViewBuilder
    private var label: some View {
        if keypad == .undo {
            Image(systemName: "delete.left")
                .font(.system(size: size * 0.32))
        } else {
            Text(keypad.label)
        }
    }

    private var tint: Color? {
        if isActive {
            return .white
        }
        switch keypad.type {
        case .equals:
            return .brandMagenta.opacity(0.75)
        case .operator_ where keypad.operation != Operation.none:
            return .brandViolet.opacity(0.55)
        case .clear, .operator_:
            return .white.opacity(0.25)
        default:
            return nil
        }
    }
}

private struct CalculatorBackground: View {
    var body: some View {
        MeshGradient(
            width: 3,
            height: 3,
            points: [
                [0, 0], [0.5, 0], [1, 0],
                [0, 0.5], [0.6, 0.55], [1, 0.5],
                [0, 1], [0.5, 1], [1, 1],
            ],
            colors: [
                .brandInk, Color(hex: 0x2A0F4A), .brandInk,
                Color(hex: 0x4B1A8C), Color(hex: 0x7A0F6E), Color(hex: 0x2A0F4A),
                .brandInk, Color(hex: 0x8C1450), .brandInk,
            ]
        )
    }
}

private extension Color {
    static let brandInk = Color(hex: 0x120D1C)
    static let brandViolet = Color(hex: 0x8A4DFF)
    static let brandMagenta = Color(hex: 0xED158A)

    init(hex: UInt32) {
        self.init(
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255
        )
    }
}

#Preview {
    CalculatorGlassView()
}
