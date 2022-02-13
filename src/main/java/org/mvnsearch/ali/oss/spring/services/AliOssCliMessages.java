package org.mvnsearch.ali.oss.spring.services;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * aliyun oss CLI messages
 *
 * @author linux_china
 */
public class AliOssCliMessages {
    /**
     * bundle FQN
     */
    @NonNls
    private static final String BUNDLE_FQN = "i18n.Messages";

    /**
     * resource bundle
     */
    private static final ResourceBundle ourBundle = ResourceBundle.getBundle(BUNDLE_FQN);

    /**
     * 私有构造函数，禁止进行创建对象操作
     */
    private AliOssCliMessages() {
    }

    /**
     * 获取resource bundle中对应的bundle值
     *
     * @param key    bundle key
     * @param params 参数，bundle的值采用MessageFormat的格式化方式
     * @return bundle值，如果bundle key不存在，返回特定key丢失格式
     */
    public static String message(@PropertyKey(resourceBundle = BUNDLE_FQN) String key, Object... params) {
        String value;
        try {
            value = ourBundle.getString(key);
        } catch (MissingResourceException ignore) {
            value = "!!!" + key + "!!!";
        }
        if (params.length > 0 && value.indexOf('{') >= 0) {
            value = MessageFormat.format(value, params);
        }
        return value;
    }

}
