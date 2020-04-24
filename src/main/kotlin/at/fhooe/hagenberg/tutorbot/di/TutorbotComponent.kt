package at.fhooe.hagenberg.tutorbot.di

import at.fhooe.hagenberg.tutorbot.commands.CommandsModule
import at.fhooe.hagenberg.tutorbot.components.ComponentsModule
import at.fhooe.hagenberg.tutorbot.network.NetworkModule
import dagger.Component
import picocli.CommandLine
import javax.inject.Singleton

@Singleton
@Component(modules = [
    CommandsModule::class,
    ComponentsModule::class,
    NetworkModule::class
])
interface TutorbotComponent {
    fun commandFactory(): CommandLine.IFactory
}
