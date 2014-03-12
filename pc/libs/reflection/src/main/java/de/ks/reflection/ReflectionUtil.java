package de.ks.reflection;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.util.Predicates;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 */
public class ReflectionUtil {
  private static final Logger log = LoggerFactory.getLogger(ReflectionUtil.class);

  /**
   * Returns a List of all methods of this class and its supertypes without the methods of Object.
   * The list is ordered by hierarchy level (clazz first, then clazz.getSuperClass etc.)
   * In a hierarchy level the methods are ordered by their name.
   *
   * @param clazz
   * @param predicates combined with AND
   * @return
   */
  @SafeVarargs
  @SuppressWarnings("unchecked")
  public static List<Method> getAllMethods(Class<?> clazz, Predicate<Method>... predicates) {
    ArrayList<Method> methods = new ArrayList<>(100);
    Map<Pair<String, List<Class<?>>>, Method> collected = new HashMap<>();

    List<Class<?>> hierarchy = getClassHierarchy(clazz);
    for (Class<?> current : hierarchy) {
      List<Method> declaredMethods = Arrays.asList(current.getDeclaredMethods());
      Collections.sort(declaredMethods, getMethodComparator());

      for (Method declaredMethod : declaredMethods) {
        String methodName = declaredMethod.getName();
        Pair<String, List<Class<?>>> key = new Pair<String, List<Class<?>>>(methodName, Arrays.asList(declaredMethod.getParameterTypes()));

        if (!collected.containsKey(key)) {
          collected.put(new Pair<String, List<Class<?>>>(methodName, Arrays.asList(declaredMethod.getParameterTypes())), declaredMethod);
          methods.add(declaredMethod);
        }
      }
    }

    Predicate<Method> combinedPredicate = Predicates.combineAnd(predicates);
    if (combinedPredicate != null) {
      return methods.stream().distinct().filter(combinedPredicate).collect(Collectors.toList());
    } else {
      return methods.stream().distinct().collect(Collectors.toList());
    }
  }

  @SafeVarargs
  @SuppressWarnings("unchecked")
  public static List<Field> getAllFields(Class<?> clazz, Predicate<Field>... predicates) {
    ArrayList<Field> fields = new ArrayList<>(100);

    List<Class<?>> hierarchy = getClassHierarchy(clazz);
    Collections.reverse(hierarchy);
    for (Class<?> current : hierarchy) {
      List<Field> declaredFields = Arrays.asList(current.getDeclaredFields());
      Collections.sort(declaredFields, getFieldComparator());
      fields.addAll(declaredFields);
    }

    Predicate<Field> combinedPredicate = Predicates.combineAnd(predicates);
    if (combinedPredicate != null) {
      return fields.stream().filter(combinedPredicate).collect(Collectors.toList());
    } else {
      return fields;
    }
  }

  public static List<Class<?>> getClassHierarchy(Class<?> clazz) {
    ArrayList<Class<?>> classes = new ArrayList<>(25);
    for (Class<?> current = clazz; !Object.class.equals(current); current = current.getSuperclass()) {
      classes.add(current);
    }
    return classes;
  }

  public static Method getMethod(Class<?> clazz, String methodName) {
    try {
      Method method = clazz.getDeclaredMethod(methodName);
      method.setAccessible(true);
      return method;
    } catch (NoSuchMethodException e) {
      List<Method> allMethods = getAllMethods(clazz);
      Optional<Method> method = allMethods.parallelStream().filter((Method curr) -> curr.getName().equals(methodName)).findFirst();
      if (method.isPresent()) {
        method.get().setAccessible(true);
        return method.get();
      } else {
        log.error("Could not find method {}  in {}", methodName, clazz.getName());
        throw new RuntimeException(e);
      }
    }
  }

  protected static Comparator<Method> getMethodComparator() {
    return (o1, o2) -> o1.getName().compareTo(o2.getName());
  }

  protected static Comparator<Field> getFieldComparator() {
    return (o1, o2) -> o1.getName().compareTo(o2.getName());
  }

  public static Object invokeMethod(Method method, Object target, Object... parameters) {
    try {
      method.setAccessible(true);
      return method.invoke(target, parameters);
    } catch (IllegalAccessException | InvocationTargetException e) {
      log.error("Could not invoke method " + method.getName() + " of " + target.getClass().getName(), e);
      throw new RuntimeException(e);
    }
  }

  public static void setField(Field field, Object instance, Object value) {
    try {
      field.setAccessible(true);
      field.set(instance, value);
    } catch (IllegalAccessException e) {
      log.error("Could not set field " + field, e);
      throw new RuntimeException(e);
    }
  }
}
