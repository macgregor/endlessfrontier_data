package com.macgregor.ef.converters;

import com.macgregor.ef.annotations.CanonicalField;
import com.macgregor.ef.annotations.CanonicalModel;
import com.macgregor.ef.annotations.Translate;
import com.macgregor.ef.converters.TranslationFieldConverter.Translator;
import com.macgregor.ef.exceptions.CanonicalConversionException;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

public class CanonicalModelConverter {
    private static final Logger logger = LoggerFactory.getLogger(CanonicalModelConverter.class);

    private CanonicalFieldConverter fieldConverter;
    private TranslationFieldConverter fieldTranslator;

    public CanonicalModelConverter(TranslationFieldConverter fieldTranslator){
        this.fieldConverter = new CanonicalFieldConverter();
        this.fieldTranslator = fieldTranslator;
    }

    public CanonicalModelConverter(Translator translator){
        this.fieldConverter = new CanonicalFieldConverter();
        this.fieldTranslator = new TranslationFieldConverter(translator);
    }

    private static void debug(Object source, String debugMessage){
        logger.debug(String.format("[%s %010d] - %s", source.getClass().getSimpleName(), System.identityHashCode(source), debugMessage));
    }

    public Object convert(Object source) throws CanonicalConversionException {
        CanonicalModelConverter.debug(source, "Beginning conversion to canonical model");

        Object dest = constructEmptyCanonicalModel(source);

        List<Field> fieldsToTranslate = FieldUtils.getFieldsListWithAnnotation(source.getClass(), CanonicalField.class);
        for(Field f : fieldsToTranslate) {
            Field destField = getCanonicalFieldMapping(source, f, dest);

            Class<?> conversionHint = getConversionHint(f, destField);
            logger.debug(String.format("Determined conversion hint to be %s", conversionHint.getSimpleName()));

            Object sourceFieldValue;
            try {
                sourceFieldValue = FieldUtils.readField(f, source, true);
            } catch (IllegalAccessException e) {
                throw new CanonicalConversionException(String.format("Fatal error reading field %s from source model %s field", f.getName(), source.getClass().getSimpleName()), e);
            }

            Object convertedFieldValue;
            if(f.getAnnotation(Translate.class) == null){
                if(List.class.isAssignableFrom(conversionHint)){
                    convertedFieldValue = this.fieldConverter.convertCollection(sourceFieldValue, getGenericListType(destField));
                } else if (Collection.class.isAssignableFrom(conversionHint)){
                    throw new CanonicalConversionException(String.format("Unsupported Collection type %s. Currently only List is supported.", conversionHint));
                } else {
                    convertedFieldValue = this.fieldConverter.convert(sourceFieldValue, conversionHint);
                }
            } else{
                convertedFieldValue = this.fieldTranslator.convert(source, f);
            }

            try {
                FieldUtils.writeField(destField, dest, convertedFieldValue, true );
            } catch (IllegalAccessException e) {
                throw new CanonicalConversionException(String.format("Fatal error writing field %s to canonical model %s field %s", f.getName(), dest.getClass().getSimpleName(), destField.getName()), e);
            }
        }

        logger.debug(String.format("[%s %010d] - Finished conversion to canonical model", source.getClass().getSimpleName(), System.identityHashCode(source)));

        return dest;
    }

    /**
     * Cosntructs an empty canonical model object indicated on the source object with the @CanonicalModel annotation. For example:
     *
     * @CanonicalModel(type=Bar.class)
     * class Foo{
     *     ...
     * }
     *
     * Will instantiate a Bar object. The destination object must have a default, no argument, constructor or an exception
     * will be thrown.
     *
     * @param source Non-canonical model we are mapping from
     * @return
     * @throws CanonicalConversionException See ConstructorUtils.invokeConstructor(), the only specific example I can find is when there
     *  is no default constructor.
     */
    public Object constructEmptyCanonicalModel(Object source) throws CanonicalConversionException {
        CanonicalModel annotation = source.getClass().getAnnotation(CanonicalModel.class);
        try {
            CanonicalModelConverter.debug(source, String.format("Instantiating %s", annotation.type()));
            return ConstructorUtils.invokeConstructor(annotation.type());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            String destModelName = annotation.getClass().getSimpleName();
            throw new CanonicalConversionException(String.format("Fatal error instantiating canonical model %s. Be sure default constructor exists for %s", destModelName, destModelName), e);
        }
    }

    /**
     * Returns the Field of the destination Object we will write the converted value to. By default it will map to a field
     * with the same name in the destination object. This can be overriden with the CanonicalField.mapsTo() field on the
     * annotation.
     *
     * @param source Non-canonical model we are mapping from
     * @param sourceField Field on the non-canonical model we are mapping from
     * @param dest Canonical model we are mapping to
     * @return
     * @throws CanonicalConversionException if we fail to find a field on the Canonical model to map to
     */
    public Field getCanonicalFieldMapping(Object source, Field sourceField, Object dest) throws CanonicalConversionException {
        CanonicalField canonicalFieldAnnotation = sourceField.getAnnotation(CanonicalField.class);
        String destFieldName;
        if(canonicalFieldAnnotation.mapsTo().equals(CanonicalField.MAPS_TO_DEFAULT)){
            destFieldName = sourceField.getName();
        } else{
            destFieldName = canonicalFieldAnnotation.mapsTo();
        }

        try {
            Field destField = FieldUtils.getField(dest.getClass(), destFieldName, true);
            CanonicalModelConverter.debug(source, String.format("Source field %s maps to canonical field %s", sourceField.getName(), destField.getName()));
            return destField;
        } catch(IllegalArgumentException e){
            throw new CanonicalConversionException(String.format("Fatal error trying to find field %s in model %s", destFieldName, dest.getClass().getSimpleName()), e);
        }
    }

    /**
     * Determine Object type of the canonical model field we are converting to. For example:
     * class Foo{
     *     @CanonicalField
     *     String id;
     * }
     *
     * class CanonicalFoo{
     *     Integer id;
     * }
     *
     * This method should determin the hint to be Integer.class. You can also manually specify the hint in the canonical
     * field annotation like @CanonicalField(hint=Integer.class)
     *
     * @param sourceField Field on the non-canonical source model, used to get the @CanonicalField annotation which may override the hint
     * @param destField Field on the canonical destination model, used to get the field type if not specified in the annotation
     * @return
     */
    public Class<?> getConversionHint(Field sourceField, Field destField){
        CanonicalField canonicalFieldAnnotation = sourceField.getAnnotation(CanonicalField.class);
        Class<?> hint;

        //we can specify a default value of null on annotations so the workaround is to return Void.class and do a check like below
        if(Void.class.isAssignableFrom(canonicalFieldAnnotation.hint())){
            hint = destField.getType();
        } else{
            hint = canonicalFieldAnnotation.hint();
        }

        return hint;
    }

    public Class<?> getGenericListType(Field destField){
        ParameterizedType listType = (ParameterizedType) destField.getGenericType();
        return (Class<?>) listType.getActualTypeArguments()[0];
    }
}
