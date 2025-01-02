/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Crypto Morin
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

package com.cryptomorin.xseries.reflection.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * A simple class that generates Java bytecode instructions for creating arrays.
 * Doesn't properly work with multidimensional arrays.
 * It's kind of weird that ASM doesn't have a helper class for this.
 *
 * @since 14.0.0
 */
final class ArrayInsnGenerator {
    private final GeneratorAdapter mv;
    private final int length;
    private int index = 0;
    private final int storeInsn;

    /**
     * @param mv     the method to generate this array to.
     * @param type   the type of the array elements (not the array type itself, i.e. not {@code Type[]}, but {@code Type})
     * @param length the length of the array.
     */
    public ArrayInsnGenerator(GeneratorAdapter mv, Class<?> type, int length) {
        if (type.getComponentType() != null) {
            throw new IllegalArgumentException("The raw array element type must be given, not the array type itself: " + type);
        }

        this.mv = mv;
        this.length = length;
        Type asmType = Type.getType(type);
        this.storeInsn = type == Object.class ? -1 : asmType.getOpcode(Opcodes.IASTORE);

        mv.push(length);
        mv.newArray(asmType);
    }

    /**
     * Used for {@code Object[]} arrays.
     */
    private boolean isDynamicStoreInsn() {
        return storeInsn == -1;
    }

    public void add(Runnable instruction) {
        if (isDynamicStoreInsn()) {
            throw new IllegalStateException("Must provide the type of stored object since this is a dynamic type array");
        }
        add(instruction, this.storeInsn);
    }

    public void add(Type elementType, Runnable instruction) {
        add(instruction, elementType.getOpcode(Opcodes.IASTORE));
    }

    private void add(Runnable instruction, int storeInsn) {
        if (index >= length) {
            throw new IllegalStateException("Array is already full, at index " + index);
        }

        // store instruction:
        //   Operand Stack:
        //     ..., arrayref, index, value →
        //     ...

        // Prepare the next index
        mv.visitInsn(Opcodes.DUP); // arrayref
        mv.push(index++); // index

        instruction.run(); // value
        mv.visitInsn(storeInsn); // store instruction
    }
}
