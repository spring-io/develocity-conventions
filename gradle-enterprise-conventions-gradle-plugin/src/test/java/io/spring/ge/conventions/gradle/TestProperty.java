/*
 * Copyright 2020-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.ge.conventions.gradle;

import org.gradle.api.Transformer;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

/**
 * {@link Property} implementation for use in unit tests.
 *
 * @param <T> type of the value of the property
 * @author Andy Wilkinson
 */
public class TestProperty<T> implements Property<T> {

	private T value;

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T get() {
		if (this.value != null) {
			return this.value;
		}
		throw new IllegalStateException();
	}

	@Override
	public T getOrElse(T fallback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T getOrNull() {
		return this.value;
	}

	@Override
	public boolean isPresent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Provider<T> orElse(T fallback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Provider<T> orElse(Provider<? extends T> fallback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void disallowChanges() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Property<T> convention(T convention) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Property<T> convention(Provider<? extends T> convention) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void finalizeValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(T value) {
		this.value = value;
	}

	@Override
	public void set(Provider<? extends T> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Property<T> value(T value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Property<T> value(Provider<? extends T> value) {
		throw new UnsupportedOperationException();
	}

}
