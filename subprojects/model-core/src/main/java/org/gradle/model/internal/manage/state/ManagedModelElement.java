/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.model.internal.manage.state;

import com.google.common.collect.ImmutableSortedMap;
import net.jcip.annotations.ThreadSafe;
import org.gradle.internal.Cast;
import org.gradle.model.internal.core.ModelType;
import org.gradle.model.internal.manage.schema.ModelProperty;
import org.gradle.model.internal.manage.schema.ModelSchema;

@ThreadSafe
public class ManagedModelElement<T> {

    private final ImmutableSortedMap<String, ModelPropertyInstance<?>> properties;
    private final ModelSchema<T> schema;

    public ManagedModelElement(ModelSchema<T> schema) {
        this.schema = schema;
        ImmutableSortedMap.Builder<String, ModelPropertyInstance<?>> builder = ImmutableSortedMap.naturalOrder();
        for (ModelProperty<?> property : schema.getProperties().values()) {
            builder.put(property.getName(), ModelPropertyInstance.of(property));
        }
        this.properties = builder.build();
    }

    public ModelType<T> getType() {
        return schema.getType();
    }

    public <U> ModelPropertyInstance<U> get(ModelType<U> propertyType, String propertyName) {
        ModelPropertyInstance<?> modelPropertyInstance = properties.get(propertyName);
        ModelType<?> modelPropertyType = modelPropertyInstance.getMeta().getType();
        if (!modelPropertyType.equals(propertyType)) {
            throw new UnexpectedModelPropertyTypeException(propertyName, schema.getType(), propertyType, modelPropertyType);
        }
        return Cast.uncheckedCast(modelPropertyInstance);
    }

    ImmutableSortedMap<String, ModelPropertyInstance<?>> getProperties() {
        return properties;
    }
}