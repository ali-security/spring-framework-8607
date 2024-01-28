/*
 * Copyright 2002-2024 the original author or authors.
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

package org.springframework.expression.spel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.ThrowableTypeAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.testresources.PlaceOfBirth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for assignment, setValue(), and isWritable() expressions.
 *
 * @author Keith Donald
 * @author Andy Clement
 * @author Sam Brannen
 */
class SetValueTests extends AbstractExpressionTests {

	private static final boolean DEBUG = false;


	@Test
	void assignmentOperator() {
		Expression e = parse("publicName='Andy'");
		assertThat(e.isWritable(context)).isFalse();
		assertThat(e.getValue(context)).isEqualTo("Andy");
	}

	@Test
	void setValueFailsWhenLeftOperandIsNotAssignable() {
		setValueAndExpectError("3=4", "enigma");
	}

	@Test
	void setValueFailsWhenLeftOperandCannotBeIndexed() {
		setValueAndExpectError("'hello'[3]", 'p');
	}

	@Test
	void setArrayElementFailsWhenIndexIsOutOfBounds() {
		setValueAndExpectError("placesLived[23]", "Wien");
	}

	@Test
	void setListElementFailsWhenIndexIsOutOfBounds() {
		setValueAndExpectError("placesLivedList[23]", "Wien");
	}

	@Test
	void setProperty() {
		setValue("wonNobelPrize", true);
	}

	@Test
	void setPropertyWithTypeConversion() {
		// Relies on StringToBooleanConverter to convert "yes" to true.
		setValue("publicBoolean", "yes", true);
	}

	@Test
	void setPropertyWithTypeConversionViaSetterMethod() {
		setValue("SomeProperty", "true", true);
	}

	@Test
	void setNestedProperty() {
		setValue("placeOfBirth.city", "Wien");
	}

	@Test
	void setArrayElement() {
		setValue("inventions[0]", "Just the telephone");
	}

	@Test
	void setNestedPropertyInArrayElement() {
		setValue("placesLived[0].city", "Wien");
	}

	@Test
	void setArrayElementToPrimitiveFromWrapper() {
		// All primitive values below are auto-boxed into their wrapper types.
		setValue("arrayContainer.booleans[1]", false);
		setValue("arrayContainer.chars[1]", (char) 3);
		setValue("arrayContainer.shorts[1]", (short) 3);
		setValue("arrayContainer.bytes[1]", (byte) 3);
		setValue("arrayContainer.ints[1]", 3);
		setValue("arrayContainer.longs[1]", 3L);
		setValue("arrayContainer.floats[1]", 3.0f);
		setValue("arrayContainer.doubles[1]", 3.4d);
	}

	@Test
	void setArrayElementToPrimitiveFromStringFails() {
		String notPrimitiveOrWrapper = "not primitive or wrapper";
		setValueAndExpectError("arrayContainer.booleans[1]", notPrimitiveOrWrapper);
		setValueAndExpectError("arrayContainer.chars[1]", notPrimitiveOrWrapper);
		setValueAndExpectError("arrayContainer.shorts[1]", notPrimitiveOrWrapper);
		setValueAndExpectError("arrayContainer.bytes[1]", notPrimitiveOrWrapper);
		setValueAndExpectError("arrayContainer.ints[1]", notPrimitiveOrWrapper);
		setValueAndExpectError("arrayContainer.longs[1]", notPrimitiveOrWrapper);
		setValueAndExpectError("arrayContainer.floats[1]", notPrimitiveOrWrapper);
		setValueAndExpectError("arrayContainer.doubles[1]", notPrimitiveOrWrapper);
	}

	@Test
	void setArrayElementToPrimitiveFromSingleElementPrimitiveArray() {
		setValue("arrayContainer.booleans[1]", new boolean[] { false }, false);
		setValue("arrayContainer.chars[1]", new char[] { 'a' }, 'a');
		setValue("arrayContainer.shorts[1]", new short[] { (short) 3 }, (short) 3);
		setValue("arrayContainer.bytes[1]", new byte[] { (byte) 3 }, (byte) 3);
		setValue("arrayContainer.ints[1]", new int[] { 42 }, 42);
		setValue("arrayContainer.longs[1]", new long[] { 42L }, 42L);
		setValue("arrayContainer.floats[1]", new float[] { 42F }, 42F);
		setValue("arrayContainer.doubles[1]", new double[] { 42D }, 42D);
	}

	@Test
	void setArrayElementToPrimitiveFromSingleElementWrapperArray() {
		setValue("arrayContainer.booleans[1]", new Boolean[] { false }, false);
		setValue("arrayContainer.chars[1]", new Character[] { 'a' }, 'a');
		setValue("arrayContainer.shorts[1]", new Short[] { (short) 3 }, (short) 3);
		setValue("arrayContainer.bytes[1]", new Byte[] { (byte) 3 }, (byte) 3);
		setValue("arrayContainer.ints[1]", new Integer[] { 42 }, 42);
		setValue("arrayContainer.longs[1]", new Long[] { 42L }, 42L);
		setValue("arrayContainer.floats[1]", new Float[] { 42F }, 42F);
		setValue("arrayContainer.doubles[1]", new Double[] { 42D }, 42D);
	}

	@Test
	void setArrayElementToPrimitiveFromSingleElementWrapperList() {
		setValue("arrayContainer.booleans[1]", List.of(false), false);
		setValue("arrayContainer.chars[1]", List.of('a'), 'a');
		setValue("arrayContainer.shorts[1]", List.of((short) 3), (short) 3);
		setValue("arrayContainer.bytes[1]", List.of((byte) 3), (byte) 3);
		setValue("arrayContainer.ints[1]", List.of(42), 42);
		setValue("arrayContainer.longs[1]", List.of(42L), 42L);
		setValue("arrayContainer.floats[1]", List.of(42F), 42F);
		setValue("arrayContainer.doubles[1]", List.of(42D), 42D);
	}

	@Disabled("Disabled due to bug in Indexer.setArrayElement() regarding primitive/wrapper types")
	@Test
	void setArrayElementToPrimitiveFromEmptyCollectionFails() {
		List<Object> emptyList = List.of();
		// TODO These fail because CollectionToObjectConverter returns null.
		// It currently throws: java.lang.IllegalStateException: Null conversion result for index [[]].
		// Whereas, it should throw a SpelEvaluationException.
		setValueAndExpectError("arrayContainer.booleans[1]", emptyList);
		setValueAndExpectError("arrayContainer.chars[1]", emptyList);
		setValueAndExpectError("arrayContainer.shorts[1]", emptyList);
		setValueAndExpectError("arrayContainer.bytes[1]", emptyList);
		setValueAndExpectError("arrayContainer.ints[1]", emptyList);
		setValueAndExpectError("arrayContainer.longs[1]", emptyList);
		setValueAndExpectError("arrayContainer.floats[1]", emptyList);
		setValueAndExpectError("arrayContainer.doubles[1]", emptyList);
	}

	@Test
	void setListElement() {
		setValue("placesLivedList[0]", new PlaceOfBirth("Wien"));
	}

	@Test
	void setGenericListElementWithTypeConversion() {
		// Relies on StringToBooleanConverter to convert "yes" to true.
		setValue("booleanList[0]", "yes", true);
		// Relies on ObjectToObjectConverter to convert a String to a PlaceOfBirth.
		setValue("placesLivedList[0]", "Wien");
	}

	@Test
	void setNestedPropertyInListElement() {
		setValue("placesLivedList[0].city", "Wien");
	}

	@Test
	void setMapElement() {
		setValue("testMap['montag']", "lundi");
	}

	/**
	 * Tests the conversion of both the keys and the values to the correct types.
	 */
	@Test
	void setGenericMapElementWithTypeConversion() {
		// Key should be converted to string representation of 42
		Expression e = parse("mapOfStringToBoolean[42]");
		assertThat(e.getValue(context)).isNull();

		e.setValue(context, "true"); // 42 -> true

		// All keys should be strings
		Set<?> keys = parse("mapOfStringToBoolean.keySet()").getValue(context, Set.class);
		assertThat(keys).allSatisfy(key -> assertThat(key).isExactlyInstanceOf(String.class));

		// All values should be booleans
		Collection<?> values = parse("mapOfStringToBoolean.values()").getValue(context, Collection.class);
		assertThat(values).allSatisfy(key -> assertThat(key).isExactlyInstanceOf(Boolean.class));

		// One final test to check conversion on the key for a map lookup
		assertThat(e.getValue(context, boolean.class)).isTrue();
	}

	@Test  // gh-15239
	void isWritableForInvalidExpressions() {
		// PROPERTYORFIELDREFERENCE
		// Non-existent field (or property):
		Expression e1 = parser.parseExpression("arrayContainer.wibble");
		assertThat(e1.isWritable(context)).as("Should not be writable!").isFalse();

		Expression e2 = parser.parseExpression("arrayContainer.wibble.foo");
		assertThatSpelEvaluationException().isThrownBy(() -> e2.isWritable(context));

		// VARIABLE
		// the variable does not exist (but that is OK, we should be writable)
		Expression e3 = parser.parseExpression("#madeup1");
		assertThat(e3.isWritable(context)).as("Should be writable!").isTrue();

		Expression e4 = parser.parseExpression("#madeup2.bar"); // compound expression
		assertThat(e4.isWritable(context)).as("Should not be writable!").isFalse();

		// INDEXER
		// non-existent indexer (wibble made up)
		Expression e5 = parser.parseExpression("arrayContainer.wibble[99]");
		assertThatSpelEvaluationException().isThrownBy(() -> e5.isWritable(context));

		// non-existent indexer (index via a string)
		Expression e6 = parser.parseExpression("arrayContainer.ints['abc']");
		assertThatSpelEvaluationException().isThrownBy(() -> e6.isWritable(context));
	}


	private Expression parse(String expressionString) {
		return parser.parseExpression(expressionString);
	}

	/**
	 * Call setValue() but expect it to fail.
	 */
	private void setValueAndExpectError(String expression, Object value) {
		Expression e = parser.parseExpression(expression);
		assertThat(e).isNotNull();
		if (DEBUG) {
			SpelUtilities.printAbstractSyntaxTree(System.out, e);
		}
		assertThatSpelEvaluationException().isThrownBy(() -> e.setValue(context, value));
	}

	private void setValue(String expression, Object value) {
		Class<?> expectedType = value.getClass();
		try {
			Expression e = parser.parseExpression(expression);
			assertThat(e).isNotNull();
			if (DEBUG) {
				SpelUtilities.printAbstractSyntaxTree(System.out, e);
			}
			assertThat(e.isWritable(context)).as("Expression is not writeable but should be").isTrue();
			e.setValue(context, value);
			assertThat(e.getValue(context, expectedType)).as("Retrieved value was not equal to set value").isEqualTo(value);
		}
		catch (EvaluationException | ParseException ex) {
			throw new AssertionError("Unexpected Exception: " + ex.getMessage(), ex);
		}
	}

	/**
	 * For use when conversion is happening during setValue(). The expectedValue should be
	 * the converted form of the value.
	 */
	private void setValue(String expression, Object value, Object expectedValue) {
		try {
			Expression e = parser.parseExpression(expression);
			assertThat(e).isNotNull();
			if (DEBUG) {
				SpelUtilities.printAbstractSyntaxTree(System.out, e);
			}
			assertThat(e.isWritable(context)).as("Expression is not writeable but should be").isTrue();
			e.setValue(context, value);
			assertThat(expectedValue).isEqualTo(e.getValue(context));
		}
		catch (EvaluationException | ParseException ex) {
			throw new AssertionError("Unexpected Exception: " + ex.getMessage(), ex);
		}
	}

	private static ThrowableTypeAssert<SpelEvaluationException> assertThatSpelEvaluationException() {
		return assertThatExceptionOfType(SpelEvaluationException.class);
	}

}
