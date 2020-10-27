/*
 * Copyright 2017-2020 Aljoscha Grebe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gamesdk.impl

import com.almightyalpaca.jetbrains.plugins.discord.gamesdk.api.*
import gamesdk.api.ActivityManager
import gamesdk.api.ClientId
import gamesdk.api.Core
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

internal class NativeCoreImpl private constructor(pointer: NativePointer) : NativeObjectImpl.Closeable(pointer), Core {
    override val activityManager: ActivityManager by nativeLazy { pointer -> NativeActivityManagerImpl(getActivityManager(pointer), this@NativeCoreImpl) }

    private var runner: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    init {
        runner.scheduleAtFixedRate({
            val result = runCallbacks()
            if (result != DiscordResult.Ok) {
                println("Error running callbacks: $result")
            }
        }, 0L, 1L, TimeUnit.SECONDS)
    }

    private fun runCallbacks(): DiscordResult = native { corePointer -> runCallbacks(corePointer).toDiscordResult() }

    override val destructor: NativeMethod<Unit> = Native::destroy

    @Synchronized
    override fun close() {
        runner.shutdown()
        runner.awaitTermination(10, TimeUnit.SECONDS)
        runner.shutdownNow()

        super.close()
    }

    companion object : NativeObjectCreator() {
        fun create(clientId: ClientId, createFlags: DiscordCreateFlags): Result<Core, DiscordResult> {
            return when (val result = native { create(clientId, createFlags.toNativeDiscordCreateFlags()) }) {
                is NativePointer -> Success(NativeCoreImpl(result))
                is NativeDiscordResult -> Failure(result.toDiscordResult())
                else -> throw IllegalStateException() // This should never happen unless the native method returns garbage
            }
        }
    }
}

/**
 * This one can't have Native as receiver because it's creating the object
 *
 * @return Either a [NativeDiscordResult] or a [NativePointer]
 */
private external fun Native.create(clientId: ClientId, createFlags: NativeDiscordCreateFlags): Any

private external fun Native.destroy(core: NativePointer)

private external fun Native.runCallbacks(core: NativePointer): Int

private external fun Native.getActivityManager(core: NativePointer): NativePointer