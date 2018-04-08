package com.macgregor.ef.converters;

import com.macgregor.ef.exceptions.CanonicalConversionException;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.*;

import java.util.Arrays;
import java.util.List;

/**
 * Makes use of the Apache Commons BeanUtils conversion tools. See
 * https://commons.apache.org/proper/commons-beanutils/apidocs/org/apache/commons/beanutils/converters/AbstractConverter.html
 */
public class CanonicalFieldConverter {
    public static final String[] TRUE_STRINGS = new String[]{"yes", "y", "true", "on", "1", "t"};
    public static final String[] FALSE_STRINGS = new String[]{"no", "n", "false", "off", "0", "f"};
    public static final char[] LIST_FIELD_DELIMINATORS = new char[]{',', '#', '|'};
    public static final char[] LIST_FIELD_ALLOWED_CHARS = new char[]{'_'};

    static{
        configureBooleanConverter();
        configureShortConverter();
        configureIntegerConverter();
        configureLongConverter();
        configureFloatConverter();
        configureDoubleConverter();
        configureStringConverter();
    }

    public Object convert(Object field, Class<?> hint) throws CanonicalConversionException{
        try {
            return ConvertUtils.convert(field, hint);
        } catch(ConversionException e){
            throw new CanonicalConversionException(String.format("Could not convert %s to hinted type %s", field, hint.getSimpleName()));
        }
    }

    public List<Object> convertCollection(Object field, Class<?> hint) throws CanonicalConversionException {
        try {
            // this is a hack to get the array type of an element, which we need because CoversionUtils doesnt work with Collections
            // https://stackoverflow.com/questions/13392160/about-java-get-string-class-from-string-class-what-if-string-class-is
            return Arrays.asList((Object[])ConvertUtils.convert(field, Class.forName("[L" + hint.getName() + ";")));
        } catch (ClassNotFoundException e) {
            throw new CanonicalConversionException(String.format("Unable to determine array class from %s", hint.getSimpleName()), e);
        }

    }

    protected static void configureArrayConvertersFor(Class<?> clazz, AbstractConverter converter){
        for(char delim : LIST_FIELD_DELIMINATORS){
            ArrayConverter arrayConverter = new ArrayConverter(clazz, converter, 0);
            arrayConverter.setDelimiter(delim);
            arrayConverter.setAllowedChars(LIST_FIELD_ALLOWED_CHARS);
            ConvertUtils.register(arrayConverter, clazz);
        }
    }

    protected static void configureBooleanConverter(){
        BooleanConverter booleanConverter = new BooleanConverter(TRUE_STRINGS, FALSE_STRINGS,null);
        ConvertUtils.register(booleanConverter, Boolean.class);
        ConvertUtils.register(booleanConverter, Boolean.TYPE);
        configureArrayConvertersFor(Boolean[].class, booleanConverter);
    }

    protected static void configureShortConverter(){
        ShortConverter shortConverter = new ShortConverter(null);
        ConvertUtils.register(shortConverter, Short.class);
        ConvertUtils.register(shortConverter, Short.TYPE);
        configureArrayConvertersFor(Short[].class, shortConverter);
    }

    protected static void configureIntegerConverter(){
        IntegerConverter integerConverter = new IntegerConverter(null);
        ConvertUtils.register(integerConverter, Integer.class);
        ConvertUtils.register(integerConverter, Integer.TYPE);
        configureArrayConvertersFor(Integer[].class, integerConverter);
    }

    protected static void configureLongConverter(){
        LongConverter longConverter = new LongConverter(null);
        ConvertUtils.register(longConverter, Long.class);
        ConvertUtils.register(longConverter, Long.TYPE);
        configureArrayConvertersFor(Long[].class, longConverter);
    }

    protected static void configureFloatConverter(){
        FloatConverter floatConverter = new FloatConverter(null);
        ConvertUtils.register(floatConverter, Float.class);
        ConvertUtils.register(floatConverter, Float.TYPE);
        configureArrayConvertersFor(Float[].class, floatConverter);
    }

    protected static void configureDoubleConverter(){
        DoubleConverter doubleConverter = new DoubleConverter(null);
        ConvertUtils.register(doubleConverter, Double.class);
        ConvertUtils.register(doubleConverter, Double.TYPE);
        configureArrayConvertersFor(Double[].class, doubleConverter);
    }

    protected static void configureStringConverter(){
        StringConverter stringConverter = new StringConverter(null);
        ConvertUtils.register(stringConverter, Double.class);
        ConvertUtils.register(stringConverter, Double.TYPE);
        configureArrayConvertersFor(String[].class, stringConverter);

    }
}