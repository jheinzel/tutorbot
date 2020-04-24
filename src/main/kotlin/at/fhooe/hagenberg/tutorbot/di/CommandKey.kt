package at.fhooe.hagenberg.tutorbot.di

import at.fhooe.hagenberg.tutorbot.commands.BaseCommand
import dagger.MapKey
import kotlin.reflect.KClass

@MapKey
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandKey(val value: KClass<out BaseCommand>)
