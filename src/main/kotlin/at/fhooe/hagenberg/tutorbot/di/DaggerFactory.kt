package at.fhooe.hagenberg.tutorbot.di

import at.fhooe.hagenberg.tutorbot.commands.BaseCommand
import picocli.CommandLine
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class DaggerFactory @Inject constructor(
    private val providers: MutableMap<Class<out BaseCommand>, Provider<BaseCommand>>
) : CommandLine.IFactory {

    @Suppress("UNCHECKED_CAST")
    override fun <K : Any?> create(clazz: Class<K>?): K = try {
        val typedClazz = clazz as Class<out BaseCommand>
        providers.getValue(typedClazz).get() as K
    } catch (exception: Exception) {
        CommandLine.defaultFactory().create(clazz) // Fallback to default factory
    }
}
