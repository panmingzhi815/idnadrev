/**
 * <pre>
 * Provides basic access to i18n facilities.
 *
 * The {@link de.ks.i18n.Localized} class is your main entry point here.
 *
 * The {@link java.util.Locale#getDefault()} is always used as the current language.
 * Language can be changed via {@link de.ks.i18n.Localized#changeLocale(java.util.Locale)}
 *
 * With changing the language the event {@link de.ks.i18n.event.LanguageChangedEvent }
 * is thrown in order to notify possible listeners (eg. labels)
 *
 * Property files must be in the following package:
 * "de.ks.i18n"
 * Naming convention is: Translation_en.properties
 *
 * Basic usage: {@link de.ks.i18n.Localized#get(String, Object...)}
 * The key "hello.world" is stored like that:
 *  hello.world=Hello {0}{1}
 * And the corresponding method call will be:
 *  "hello.world", "world", "!"
 * Which will result in:
 *  Hello world!
 * If you add a colon ":" to the end of the string it is ignored.
 * This is quite useful for input fields.
 * </pre>
 */

package de.ks.i18n;
