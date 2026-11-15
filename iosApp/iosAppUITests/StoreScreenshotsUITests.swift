import XCTest

/// App Store screenshots, captured by `fastlane ios screenshots`. Each test types a
/// calculation and attaches one screenshot to the test result, which the lane exports.
final class StoreScreenshotsUITests: XCTestCase {
    private let app = XCUIApplication()

    override func setUp() {
        continueAfterFailure = false
        app.launch()
    }

    func testScreenshot1Division() {
        capture("1_division", keys: ["1", "2", "8", "0", "÷", "1", "6", "+", "4", "="])
    }

    func testScreenshot2ActiveOperator() {
        capture("2_active_operator", keys: ["2", "4", "5", "0", "×", "1", "2", "−"])
    }

    func testScreenshot3ExactDecimals() {
        capture("3_exact_decimals", keys: ["0", ".", "1", "+", "0", ".", "2", "="])
    }

    func testScreenshot4LargeNumbers() {
        capture("4_large_numbers", keys: Array("888888881201868+4").map(String.init))
    }

    private func capture(_ name: String, keys: [String]) {
        for key in keys {
            app.buttons[key].tap()
        }
        // Let the glass and the numeric text transitions settle.
        sleep(2)
        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}

/// Walks through the review script on a physical iPhone while QuickTime records the screen.
/// Starts from the Home Screen, as App Review asked for.
final class ReviewVideoUITests: XCTestCase {
    private let app = XCUIApplication()

    func testReviewScript() {
        continueAfterFailure = false
        XCUIDevice.shared.press(.home)
        sleep(3)
        app.activate()
        sleep(2)

        type(["1", "2", "+", "3", "4", "="])
        type(["0", ".", "1", "+", "0", ".", "2", "="])
        type(["AC"])
        type(["5", "0", "×", "2", "0", "%", "="])
        type(["AC"])
        type(["( )", "5", "+", "3", "( )", "×", "2", "="])
        type(["AC"])
        type(["1", "2", "3", "Delete"])
        sleep(3)
        type(["AC"])
        sleep(2)
    }

    private func type(_ keys: [String]) {
        for key in keys {
            app.buttons[key].tap()
            sleep(1)
        }
        sleep(2)
    }
}
