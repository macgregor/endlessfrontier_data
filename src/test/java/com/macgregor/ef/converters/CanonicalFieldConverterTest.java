package com.macgregor.ef.converters;

import com.macgregor.ef.exceptions.CanonicalConversionException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CanonicalFieldConverterTest {
    private static final Logger logger = LoggerFactory.getLogger(CanonicalModelConverter.class);

    private static final CanonicalFieldConverter converter = new CanonicalFieldConverter();

    @Test
    public void testIntegerConversion() throws CanonicalConversionException {
        assertEquals(1, converter.convert("1", Integer.class));
    }

    @Test
    public void testIntegerConversionDefaultsNull() throws CanonicalConversionException {
        assertEquals(null, converter.convert("", Integer.class));
    }

    @Test
    public void testIntegerArrayConversion() throws CanonicalConversionException {
        Integer[] expected = new Integer[]{1,2,3};
        testArrayConversion(expected, Integer[].class);
    }

    @Test
    public void testIntegerArrayConversionPrimitiveArray() throws CanonicalConversionException {
        int[] expected = new int[]{1,2,3};
        for(char c : CanonicalFieldConverter.LIST_FIELD_DELIMINATORS){
            String stringList = StringUtils.join(expected, c);
            assertArrayEquals(expected, (int[]) converter.convert(stringList, int[].class));
        }
    }

    @Test
    public void testIntegerArrayConversionList() throws CanonicalConversionException {
        List<Integer> expected = Arrays.asList(1,2,3);
        for(char c : CanonicalFieldConverter.LIST_FIELD_DELIMINATORS){
            String stringList = StringUtils.join(expected, c);
            assertEquals(expected, converter.convertCollection(stringList, Integer.class));
        }
    }

    @Test
    public void testIntegerArrayConversionDefaultsNull() throws CanonicalConversionException {
        assertArrayEquals(new Integer[]{}, (Integer[])converter.convert("", Integer[].class));
    }

    @Test
    public void testBooleanConversionTrueValues() throws CanonicalConversionException {
        for(String s : CanonicalFieldConverter.TRUE_STRINGS){
            assertTrue((Boolean)converter.convert(s, Boolean.class));
        }
    }

    @Test
    public void testBooleanConversionTrueValuesPrimitives() throws CanonicalConversionException {
        for(String s : CanonicalFieldConverter.TRUE_STRINGS){
            assertTrue((boolean)converter.convert(s, boolean.class));
        }
    }

    @Test
    public void testBooleanConversionFalseValues() throws CanonicalConversionException {
        for(String s : CanonicalFieldConverter.FALSE_STRINGS){
            assertFalse((Boolean)converter.convert(s, Boolean.class));
        }
    }

    @Test
    public void testBooleanConversionFalseValuesPrimitives() throws CanonicalConversionException {
        for(String s : CanonicalFieldConverter.FALSE_STRINGS){
            assertFalse((boolean)converter.convert(s, boolean.class));
        }
    }
    @Test
    public void testBooleanConversionDefaultsNull() throws CanonicalConversionException {
        assertEquals(null, converter.convert("", Boolean.class));
    }
    @Test
    public void testBooleanArrayConversion() throws CanonicalConversionException {
        Boolean[] expected = new Boolean[]{true, false, true};
        testArrayConversion(expected, Boolean[].class);
    }

    @Test
    public void testStringListsOnlySplitOnDelim() throws CanonicalConversionException {
        String[] expected = new String[]{"questGoldGain_A", "attackSpeed_A"};
        assertArrayEquals(expected, (String[]) converter.convert("questGoldGain_A#attackSpeed_A", String[].class));
    }

    private void testArrayConversion(Object[] expected, Class<?> clazz) throws CanonicalConversionException{
        for(char c : CanonicalFieldConverter.LIST_FIELD_DELIMINATORS){
            String stringList = StringUtils.join(expected, c);
            assertArrayEquals(expected, (Object[])converter.convert(stringList, clazz));
        }
    }
}
