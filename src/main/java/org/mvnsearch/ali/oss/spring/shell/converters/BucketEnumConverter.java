package org.mvnsearch.ali.oss.spring.shell.converters;

import org.springframework.shell.core.Completion;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.MethodTarget;

import java.util.List;

/**
 * bucket enum converter
 *
 * @author linux_china
 */
public class BucketEnumConverter implements Converter<BucketEnum> {
    /**
     * Indicates whether this converter supports the given type in the given option context
     *
     * @param type          the type being checked
     * @param optionContext a non-<code>null</code> string that customises the
     *                      behaviour of this converter for a given {@link org.springframework.shell.core.annotation.CliOption} of a given
     *                      {@link org.springframework.shell.core.annotation.CliCommand}; the contents will have special meaning to this
     *                      converter (e.g. be a comma-separated list of keywords known to this
     *                      converter)
     * @return see above
     */
    public boolean supports(Class<?> type, String optionContext) {
        return type.getCanonicalName().equals(BucketEnum.class.getCanonicalName());
    }

    /**
     * Converts from the given String value to type T
     *
     * @param value         the value to convert
     * @param targetType    the type being converted to; can't be <code>null</code>
     * @param optionContext a non-<code>null</code> string that customises the
     *                      behaviour of this converter for a given {@link org.springframework.shell.core.annotation.CliOption} of a given
     *                      {@link org.springframework.shell.core.annotation.CliCommand}; the contents will have special meaning to this
     *                      converter (e.g. be a comma-separated list of keywords known to this
     *                      converter)
     * @return see above
     * @throws RuntimeException if the given value could not be converted
     */
    @SuppressWarnings("unchecked")
    public BucketEnum convertFromText(String value, Class<?> targetType, String optionContext) {
        Class<BucketEnum> enumClass = (Class<BucketEnum>) targetType;
        try {
            BucketEnum bucket = enumClass.newInstance();
            bucket.setName(value);
            return bucket;
        } catch (Exception ignore) {

        }
        return null;
    }

    /**
     * Populates the given list with the possible completions
     *
     * @param completions   the list to populate; can't be <code>null</code>
     * @param targetType    the type of parameter for which a string is being entered
     * @param existingData  what the user has typed so far
     * @param optionContext a non-<code>null</code> string that customises the
     *                      behaviour of this converter for a given {@link org.springframework.shell.core.annotation.CliOption} of a given
     *                      {@link org.springframework.shell.core.annotation.CliCommand}; the contents will have special meaning to this
     *                      converter (e.g. be a comma-separated list of keywords known to this
     *                      converter)
     * @param target
     * @return <code>true</code> if all the added completions are complete
     *         values, or <code>false</code> if the user can press TAB to add further
     *         information to some or all of them
     */
    public boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData, String optionContext, MethodTarget target) {
        for (String candidate : BucketEnum.getBucketNames()) {
            if ("".equals(existingData) || candidate.startsWith(existingData) || existingData.startsWith(candidate) || candidate.toUpperCase().startsWith(existingData.toUpperCase()) || existingData.toUpperCase().startsWith(candidate.toUpperCase())) {
                completions.add(new Completion(candidate));
            }
        }
        return true;
    }
}
