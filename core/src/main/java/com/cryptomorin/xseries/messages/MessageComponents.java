/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.cryptomorin.xseries.messages;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;

import static com.cryptomorin.xseries.reflection.XReflection.ofMinecraft;

/**
 * Utility class for sharing methods relating to {@link BaseComponent}.
 */
@ApiStatus.Experimental
public final class MessageComponents {
    private static final MethodHandle CraftChatMessage_fromJson;

    static {
        MethodHandle fromJson;

        MinecraftClassHandle IChatBaseComponentClass = ofMinecraft()
                .inPackage(MinecraftPackage.NMS, "network.chat").named("IChatBaseComponent");

        try {
            fromJson = XReflection.ofMinecraft()
                    .inPackage(MinecraftPackage.CB, "util")
                    .named("CraftChatMessage")
                    .method("public static IChatBaseComponent fromJSON(String jsonMessage)").returns(IChatBaseComponentClass)
                    .reflect();
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }

        CraftChatMessage_fromJson = fromJson;
    }

    /**
     * This method is only officially available on <a href="https://github.com/PaperMC/Paper/blob/c98cd65802fcecfd3db613819e6053e2b8cbdf4f/paper-server/src/main/java/org/bukkit/craftbukkit/util/CraftChatMessage.java#L350-L352">Paper</a>.
     */
    public static Object bungeeToVanilla(BaseComponent component) throws Throwable {
        String json = ComponentSerializer.toString(component);
        return CraftChatMessage_fromJson.invoke(json);
    }
}
