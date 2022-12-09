package at.fhooe.hagenberg.tutorbot.components

import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Named
import javax.inject.Singleton
import kotlin.random.Random

@Module
object ComponentsModule {

    @Provides
    @Named("config")
    fun provideConfigFile(): File {
        val jarUrl = javaClass.protectionDomain.codeSource.location // Determine location of the executing JAR file
        return File(File(jarUrl.toURI().path).parentFile, "tutorbot.properties")
    }

    @Provides
    @Singleton
    fun provideRandom(): Random = Random.Default
}
