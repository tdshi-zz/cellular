package de.study.app.view;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import java.text.MessageFormat;
import java.util.*;

/**
 * I18N utility class
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 * Inspired by the tutorial from P.J. Meisch on
 * @See https://www.sothawo.com/2016/09/how-to-implement-a-javafx-ui-where-the-language-can-be-changed-dynamically/
 * <p>
 * I modified the String get() and getBundle() method with the correct resourcebundle name and added a few new methods based on the tutorial.
 */
public final class I18N {
    /**
     * the current selected Locale.
     */
    private static final ObjectProperty<Locale> locale;

    static {
        locale = new SimpleObjectProperty<>(getDefaultLocale());
        locale.addListener((observable, oldValue, newValue) -> Locale.setDefault(newValue));
    }

    private I18N() {
    }

    /**
     * get the supported Locales.
     *
     * @return List of Locale objects.
     */
    public static List<Locale> getSupportedLocales() {
        return new ArrayList<>(Arrays.asList(Locale.ENGLISH, Locale.GERMAN));
    }

    /**
     * get the default locale. This is the systems default if contained in the supported locales, english otherwise.
     *
     * @return the default locale
     */
    public static Locale getDefaultLocale() {
        Locale sysDefault = Locale.getDefault();
        return getSupportedLocales().contains(sysDefault) ? sysDefault : Locale.ENGLISH;
    }

    public static Locale getLocale() {
        return locale.get();
    }

    public static void setLocale(Locale locale) {
        localeProperty().set(locale);
        Locale.setDefault(locale);
    }

    public static ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    /**
     * gets the string with the given key from the resource bundle for the current locale and uses it as first argument
     * to MessageFormat.format, passing in the optional args and returning the result.
     *
     * @param key  message key
     * @param args optional arguments for the message
     * @return localized formatted string
     */
    public static String get(final String key, final Object... args) {
        ResourceBundle bundle = ResourceBundle.getBundle("casimulator", getLocale());
        return MessageFormat.format(bundle.getString(key), args);
    }

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle("casimulator", getLocale());
    }

    /**
     * creates a String binding to a localized String for the given message bundle key
     *
     * @param key key
     * @return String binding
     */
    public static StringBinding createStringBinding(final String key, Object... args) {
        return Bindings.createStringBinding(() -> get(key, args), locale);
    }

    /* Following implementation were added during the project */

    /**
     * creates a tooltip binding to a localized String for the given message bundle key
     *
     * @param key key
     * @return Tooltip binding
     */
    public static Tooltip tooltipForKey(final String key, final Object... args) {
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(createStringBinding(key, args));
        return tooltip;
    }

    /**
     * creates a TextField binding to a localized String for the given message bundle key
     *
     * @param key key
     * @return TextField binding
     */
    public static TextField textFieldForKey(final String key, final Object... args) {
        TextField textField = new TextField();
        textField.textProperty().bind(createStringBinding(key, args));
        return textField;
    }

    /**
     * creates a Label binding to a localized String for the given message bundle key
     *
     * @param key key
     * @return Label binding
     */
    public static Label labelForKey(final String key, final Object... args) {
        Label label = new Label();
        label.textProperty().bind(createStringBinding(key, args));
        return label;
    }
}