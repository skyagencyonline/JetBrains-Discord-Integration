/**
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

#ifndef TYPES_H
#define TYPES_H

#include "discord_game_sdk.h"

#include <jni.h>
#include <type_traits>

namespace types {
    jobject createIntegerObject(JNIEnv &env, jint value);

    jobject createLongObject(JNIEnv &env, jlong value);

    jobject createBooleanObject(JNIEnv &env, jboolean value);

    jobject createPair(JNIEnv &env, jobject first, jobject second);

    /**
     * Activity is an jobject of type NativeDiscordActivity
     */
    DiscordActivity createDiscordActivity(JNIEnv &env, const jobject &jActivity);

    jobject createJavaActivity(JNIEnv &env, const DiscordActivity &activity);

    jobject createJavaUser(JNIEnv &env, const DiscordUser &user);

    jobject createJavaPresence(JNIEnv &env, const DiscordPresence &presence);

    jobject createJavaRelationship(JNIEnv &env, const DiscordRelationship &relationship);

    jobject createJavaOAuth2Token(JNIEnv &env, jstring access_token, jstring scopes, jlong expires);

    jobject createNativeDiscordObjectResultSuccess(JNIEnv &env, jobject object);

    jobject createNativeDiscordObjectResultFailure(JNIEnv &env, EDiscordResult result);

    template<typename T, typename = std::enable_if_t<std::is_invocable_r_v<jobject, T, JNIEnv &>>>
    jobject createNativeDiscordObjectResult(JNIEnv &env, enum EDiscordResult result, T &&call) {
        if (result == DiscordResult_Ok) {
            return createNativeDiscordObjectResultSuccess(env, call(env));
        } else {
            return createNativeDiscordObjectResultFailure(env, result);
        }
    }
} // namespace types

#endif // TYPES_H