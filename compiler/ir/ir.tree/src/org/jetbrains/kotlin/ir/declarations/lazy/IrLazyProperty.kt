/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.declarations.lazy

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.util.DeclarationStubGenerator
import org.jetbrains.kotlin.ir.util.TypeTranslator
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.hasBackingField

@OptIn(ObsoleteDescriptorBasedAPI::class)
class IrLazyProperty(
    startOffset: Int,
    endOffset: Int,
    origin: IrDeclarationOrigin,
    override val symbol: IrPropertySymbol,
    override val descriptor: PropertyDescriptor,
    override val name: Name,
    override var visibility: Visibility,
    override val modality: Modality,
    override val isVar: Boolean,
    override val isConst: Boolean,
    override val isLateinit: Boolean,
    override val isDelegated: Boolean,
    override val isExternal: Boolean,
    override val isExpect: Boolean,
    override val isFakeOverride: Boolean,
    stubGenerator: DeclarationStubGenerator,
    typeTranslator: TypeTranslator,
    bindingContext: BindingContext? = null
) :
    IrLazyDeclarationBase(startOffset, endOffset, origin, stubGenerator, typeTranslator),
    IrProperty {

    init {
        symbol.bind(this)
    }

    private val hasBackingField: Boolean =
        descriptor.hasBackingField(bindingContext) || stubGenerator.extensions.isPropertyWithPlatformField(descriptor)

    override var backingField: IrField? by lazyVar {
        if (hasBackingField) {
            stubGenerator.generateFieldStub(descriptor).apply {
                correspondingPropertySymbol = this@IrLazyProperty.symbol
            }
        } else null
    }

    override var getter: IrSimpleFunction? by lazyVar {
        descriptor.getter?.let { stubGenerator.generateFunctionStub(it, createPropertyIfNeeded = false) }?.apply {
            correspondingPropertySymbol = this@IrLazyProperty.symbol
        }
    }

    override var setter: IrSimpleFunction? by lazyVar {
        descriptor.setter?.let { stubGenerator.generateFunctionStub(it, createPropertyIfNeeded = false) }?.apply {
            correspondingPropertySymbol = this@IrLazyProperty.symbol
        }
    }

    override var metadata: MetadataSource?
        get() = null
        set(_) = error("We should never need to store metadata of external declarations.")
}
