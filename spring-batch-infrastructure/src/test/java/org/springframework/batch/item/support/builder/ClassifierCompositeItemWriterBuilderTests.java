/*
 * Copyright 2017-2023 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.batch.item.support.builder;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.classify.PatternMatchingClassifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Glenn Renfro
 * @author Mahmoud Ben Hassine
 */
class ClassifierCompositeItemWriterBuilderTests {

	private final Chunk defaults = new Chunk();

	private final Chunk foos = new Chunk();

	@Test
	void testWrite() throws Exception {
		Map<String, ItemWriter<? super String>> map = new HashMap<>();
		ItemWriter<String> fooWriter = new ItemWriter<>() {
			@Override
			public void write(Chunk<? extends String> chunk) throws Exception {
				foos.addAll(chunk.getItems());
			}
		};
		ItemWriter<String> defaultWriter = new ItemWriter<>() {
			@Override
			public void write(Chunk<? extends String> chunk) throws Exception {
				defaults.addAll(chunk.getItems());
			}
		};
		map.put("foo", fooWriter);
		map.put("*", defaultWriter);
		ClassifierCompositeItemWriter<String> writer = new ClassifierCompositeItemWriterBuilder<String>()
			.classifier(new PatternMatchingClassifier<>(map))
			.build();

		writer.write(Chunk.of("foo", "foo", "one", "two", "three"));
		assertIterableEquals(Chunk.of("foo", "foo"), foos);
		assertIterableEquals(Chunk.of("one", "two", "three"), defaults);
	}

	@Test
	void testSetNullClassifier() {
		Exception exception = assertThrows(IllegalArgumentException.class,
				() -> new ClassifierCompositeItemWriterBuilder<>().build());
		assertEquals("A classifier is required.", exception.getMessage());
	}

}
