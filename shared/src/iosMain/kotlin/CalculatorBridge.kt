import com.jesusdmedinac.compose.calculator.ui.model.CalculatorEngine
import com.jesusdmedinac.compose.calculator.ui.model.CalculatorState
import com.jesusdmedinac.compose.calculator.ui.model.Keypad
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Lets Swift drive [CalculatorEngine] without consuming a Kotlin Flow directly:
 * every state change is delivered on the main thread through [watch].
 */
class CalculatorBridge {
    private val engine = CalculatorEngine()
    private val scope = MainScope()

    val keypads: List<Keypad> = Keypad.values().toList()

    val currentState: CalculatorState
        get() = engine.state.value

    fun watch(onChange: (CalculatorState) -> Unit) {
        scope.launch {
            engine.state.collect { onChange(it) }
        }
    }

    fun onKeypadClicked(keypad: Keypad) = engine.onKeypadClicked(keypad)

    fun close() = scope.cancel()
}
