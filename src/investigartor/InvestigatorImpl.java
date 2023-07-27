package investigartor;

import reflection.api.Investigator;

import java.lang.reflect.*;
import java.util.*;

public class InvestigatorImpl implements Investigator {

    private Object objectToInvestigate;
    final String NO_PARENT_MSG = "No direct parent class";

    @Override
    public void load(Object anInstanceOfSomething) {
        objectToInvestigate = anInstanceOfSomething;
    }

    @Override
    public int getTotalNumberOfMethods() {
        return objectToInvestigate.getClass().getDeclaredMethods().length;
    }

    @Override
    public int getTotalNumberOfConstructors() {
        return objectToInvestigate.getClass().getConstructors().length;
    }

    @Override
    public int getTotalNumberOfFields() {
        return objectToInvestigate.getClass().getDeclaredFields().length;
    }

    @Override
    public Set<String> getAllImplementedInterfaces() {
        Set<String> ret = new HashSet<>();
        Class<?>[] interfacesImplementedByClass = objectToInvestigate.getClass().getInterfaces();

        for (Class<?> i : interfacesImplementedByClass) {
            ret.add(i.getSimpleName());
        }

        return ret;
    }

    @Override
    public int getCountOfConstantFields() {
        int ret = 0;

        Field[] allFields = objectToInvestigate.getClass().getDeclaredFields();
        for (Field f : allFields) {
            if (Modifier.isFinal(f.getModifiers())) {
                ret++;
            }
        }

        return ret;
    }

    @Override
    public int getCountOfStaticMethods() {
        int ret = 0;

        Method[] allMethods = objectToInvestigate.getClass().getDeclaredMethods();
        for (Method m : allMethods) {
            if (Modifier.isStatic(m.getModifiers())) {
                ret++;
            }
        }

        return ret;
    }

    @Override
    public boolean isExtending() {
        Class<?> superClass = objectToInvestigate.getClass().getSuperclass();

        return superClass != null;
    }

    @Override
    public String getParentClassSimpleName() {
        try {
            return objectToInvestigate.getClass().getSuperclass().getSimpleName();
        } catch (NullPointerException e) {
            return NO_PARENT_MSG;
        }
    }

    @Override
    public boolean isParentClassAbstract() {
        try {
            return Modifier.isAbstract(objectToInvestigate.getClass().getSuperclass().getModifiers());
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public Set<String> getNamesOfAllFieldsIncludingInheritanceChain() {
        Set<String> ret = new HashSet<>();
        Class<?> curr = objectToInvestigate.getClass();
        do {
            Field[] allClassFields = curr.getDeclaredFields();
            for (Field f : allClassFields) {
                ret.add(f.getName());
            }
            curr = curr.getSuperclass();
        } while (curr != null);

        return ret;
    }

    @Override
    public int invokeMethodThatReturnsInt(String methodName, Object... args) {
        try {
            // Get the Method we need to invoke
            Method toInvoke = objectToInvestigate.getClass().getMethod(methodName, getParameterTypesOfObjects(args));
            // Since we know the returned value of the method is int, we can cast it
            return (int) toInvoke.invoke(objectToInvestigate, args);
        } catch (NoSuchMethodException | ClassCastException | InvocationTargetException | IllegalAccessException e) {
            return 0;
        }

    }

    @Override
    public Object createInstance(int numberOfArgs, Object... args) {
        try {
            Constructor<?>[] ctors = objectToInvestigate.getClass().getConstructors();
            for (Constructor<?> c : ctors) {
                if (c.getParameterCount() == numberOfArgs) {
                    return c.newInstance(args);
                }
            }
            return null;
        } catch (InstantiationException | InvocationTargetException |
                 IllegalAccessException e) {
            return null;
        }
    }

    @Override
    public Object elevateMethodAndInvoke(String name, Class<?>[] parametersTypes, Object... args) {
        try {
            Method[] methods = objectToInvestigate.getClass().getDeclaredMethods();
            for(Method m: methods)
            {
                if(m.getName().contentEquals(name) && Arrays.equals(m.getParameterTypes(), parametersTypes))
                {
                    m.setAccessible(true);
                    return m.invoke(objectToInvestigate, args);
                }
            }
            return null;
        } catch (InvocationTargetException | IllegalAccessException e) {
            return null;
        }
    }

    @Override
    public String getInheritanceChain(String delimiter) {
        Stack<String> classHierarchy = new Stack<>();
        StringBuilder ret = new StringBuilder();
        Class<?> curr = objectToInvestigate.getClass();
        do {
            classHierarchy.push(curr.getSimpleName());
            curr = curr.getSuperclass();
        } while (curr != null);

        while (!classHierarchy.empty()) {
            ret.append(classHierarchy.pop());
            if (!classHierarchy.empty()) {
                ret.append(delimiter);
            }
        }

        return ret.toString();
    }

    private Class<?>[] getParameterTypesOfObjects(Object... args) {
        Class<?>[] parameterTypes = new Class[args.length];

        // Get parameter type list of the given args
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }

        // Convert ArrayList of args to array
        return parameterTypes;
    }
}
