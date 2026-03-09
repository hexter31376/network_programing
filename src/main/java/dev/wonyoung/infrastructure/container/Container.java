package dev.wonyoung.infrastructure.container;

import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Container {

    private static final String CLASS_EXTENSION = ".class";

    private final Map<Class<?>, Object> registry = new HashMap<>(); // 타입과 인스턴스를 저장하는 레지스트리
    private final Set<Class<?>> candidates = new HashSet<>(); // @Component로 표시된 후보 클래스들을 저장하는 집합
    private final Set<Class<?>> inProgress = new HashSet<>(); // 현재 생성 중인 클래스들을 추적하여 순환 의존성 감지

    public <T> void register(Class<T> type, T instance) {
        registry.put(type, instance);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T get(Class<T> type) throws Exception {
        Object existing = findInRegistry(type);
        if (existing != null) {
            return (T) existing;
        }

        Class<?> candidate = findInCandidates(type);
        if (candidate == null) {
            throw new RuntimeException("Bean을 찾을 수 없습니다: " + type.getSimpleName());
        }

        if (inProgress.contains(candidate)) {
            throw new RuntimeException("순환 의존성 감지: " + candidate.getSimpleName());
        }

        inProgress.add(candidate);
        Object instance = createInstance(candidate);
        inProgress.remove(candidate);

        registry.put(candidate, instance);
        return (T) instance;
    }

    public void scan(String packageName) throws Exception {
        findClasses(packageName).stream()
                .filter(c -> c.isAnnotationPresent(Component.class))
                .forEach(candidates::add);
    }

    private Object createInstance(Class<?> clazz) throws Exception {
        Optional<Constructor<?>> injectConstructor = Arrays.stream(clazz.getConstructors())
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .findFirst();

        if (injectConstructor.isPresent()) {
            Constructor<?> constructor = injectConstructor.get();
            return constructor.newInstance(resolveArgs(constructor.getParameterTypes()));
        }
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object[] resolveArgs(Class<?>[] paramTypes) throws Exception {
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = get(paramTypes[i]);
        }
        return args;
    }

    private Object findInRegistry(Class<?> type) {
        return registry.values().stream()
                .filter(obj -> type.isAssignableFrom(obj.getClass()))
                .findFirst()
                .orElse(null);
    }

    private Class<?> findInCandidates(Class<?> type) {
        return candidates.stream()
                .filter(type::isAssignableFrom)
                .findFirst()
                .orElse(null);
    }

    private List<Class<?>> findClasses(String packageName) throws Exception {
        String path = packageName.replace('.', '/');
        URL url = getClass().getClassLoader().getResource(path);
        if (url == null) {
            throw new IllegalArgumentException("패키지를 찾을 수 없습니다: " + packageName);
        }
        List<Class<?>> classes = new ArrayList<>();
        collectClasses(new File(url.toURI()), packageName, classes);
        return classes;
    }

    private void collectClasses(File dir, String packageName, List<Class<?>> classes) throws Exception {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                collectClasses(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(CLASS_EXTENSION)) {
                String className = packageName + "." + file.getName().replace(CLASS_EXTENSION, "");
                classes.add(Class.forName(className));
            }
        }
    }
}