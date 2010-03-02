/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.wave.api.impl;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.wave.api.Annotation;
import com.google.wave.api.Element;
import com.google.wave.api.JsonRpcResponse;
import com.google.wave.api.NonJsonSerializable;
import com.google.wave.api.OperationRequest;
import com.google.wave.api.Range;

import java.lang.reflect.Type;

/**
 * A factory to instantiate a {@link Gson} instance, with pre-registered type
 * adapters for serializing and deserializing Wave API classes that are used
 * as data transfer objects.
 */
public class GsonFactory {

  /**
   * Creates a {@link Gson} instance, with additional type adapters for these
   * types:
   * <ul>
   *   <li>{@link EventMessageBundle}</li>
   *   <li>{@link OperationRequest}</li>
   *   <li>{@link Element}</li>
   * </ul>
   *
   * @return an instance of {@link Gson} with pre-registered type adapters.
   */
  public Gson create() {
    return create("");
  }

  /**
   * Creates a {@link Gson} instance, with additional type adapters for these
   * types:
   * <ul>
   *   <li>{@link EventMessageBundle}</li>
   *   <li>{@link OperationRequest}</li>
   *   <li>{@link Element}</li>
   *   <li>{@link JsonRpcResponse}</li>
   * </ul>
   *
   * @param opNamespace prefix that should be prepended to the operation during
   *     serialization.
   * @return an instance of {@link Gson} with pre-registered type adapters.
   */
  public Gson create(String opNamespace) {
    return new GsonBuilder()
        .setExclusionStrategies(new NonSerializableExclusionStrategy())
        .registerTypeAdapter(EventMessageBundle.class, new EventMessageBundleGsonAdaptor())
        .registerTypeAdapter(OperationRequest.class, new OperationRequestGsonAdaptor(opNamespace))
        .registerTypeAdapter(Element.class, new ElementGsonAdaptor())
        .registerTypeAdapter(JsonRpcResponse.class, new JsonRpcResponseGsonAdaptor())
        .registerTypeAdapter(Annotation.class, new AnnotationInstanceCreator())
        .registerTypeAdapter(Range.class, new RangeInstanceCreator())
        .create();
  }

  /**
   * A strategy definition that excludes all fields that are annotated with
   * {@link NonJsonSerializable}.
   */
  private static class NonSerializableExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
      return false;
    }

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
      return f.getAnnotation(NonJsonSerializable.class) != null;
    }
  }

  /**
   * An instance creator that creates an empty {@link Annotation}.
   */
  private static class AnnotationInstanceCreator implements InstanceCreator<Annotation> {
    @Override
    public Annotation createInstance(Type type) {
      return new Annotation("", "", -1, -1);
    }
  }

  /**
   * An instance creator that creates an empty {@link Annotation}.
   */
  private static class RangeInstanceCreator implements InstanceCreator<Range> {
    @Override
    public Range createInstance(Type type) {
      return new Range(-1, -1);
    }
  }
}
