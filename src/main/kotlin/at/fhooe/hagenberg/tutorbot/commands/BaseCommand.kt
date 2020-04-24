package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.util.ProgramExitError
import java.util.concurrent.Callable

abstract class BaseCommand : Callable<Int> {

    override fun call(): Int {
        try {
            execute()
        } catch (error: ProgramExitError) {
            return -1
        }
        return 0
    }

    abstract fun execute()
}
